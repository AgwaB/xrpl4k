package org.xrpl.sdk.client.transport

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.client.internal.WsJsonRpcResponse
import org.xrpl.sdk.client.internal.XrplJson
import org.xrpl.sdk.client.internal.extractWsRpcError
import org.xrpl.sdk.core.result.XrplException
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

/**
 * Persistent WebSocket transport with message routing and proper disconnect handling.
 *
 * Phase 3 treats disconnect as terminal — there is no auto-reconnect.
 * Reconnect will be added in a future phase alongside auto-resubscribe.
 *
 * @param url the WebSocket endpoint URL.
 * @param engine the HTTP client engine.
 * @param scope the coroutine scope for background operations.
 * @param heartbeatInterval interval between heartbeat pings.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class WebSocketTransport(
    private val url: String,
    engine: HttpClientEngine,
    private val scope: CoroutineScope,
    private val heartbeatInterval: Duration,
) : XrplTransport {
    private val client: HttpClient =
        HttpClient(engine) {
            install(WebSockets)
        }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _subscriptionEvents = MutableSharedFlow<JsonObject>(extraBufferCapacity = 64)
    val subscriptionEvents: SharedFlow<JsonObject> = _subscriptionEvents.asSharedFlow()

    private val pendingMutex = Mutex()
    private val pendingRequests = HashMap<Int, CompletableDeferred<JsonObject>>()

    private val idCounter = AtomicInt(1)
    private var connectionJob: Job? = null
    private val sendChannel = Channel<String>(Channel.BUFFERED)

    /**
     * Establishes the WebSocket connection and starts the message loop.
     * Must be called before [request].
     */
    suspend fun connect() {
        if (_connectionState.value is ConnectionState.Connected) return
        _connectionState.value = ConnectionState.Connecting

        connectionJob =
            scope.launch {
                try {
                    client.webSocket(url) {
                        _connectionState.value = ConnectionState.Connected

                        // Heartbeat job
                        val heartbeatJob =
                            launch {
                                while (true) {
                                    delay(heartbeatInterval)
                                    try {
                                        send(Frame.Ping(ByteArray(0)))
                                    } catch (_: Exception) {
                                        break
                                    }
                                }
                            }

                        // Send job
                        val sendJob =
                            launch {
                                for (message in sendChannel) {
                                    try {
                                        send(Frame.Text(message))
                                    } catch (_: Exception) {
                                        break
                                    }
                                }
                            }

                        // Receive loop
                        try {
                            for (frame in incoming) {
                                when (frame) {
                                    is Frame.Text -> handleTextFrame(frame.readText())
                                    else -> { /* ignore */ }
                                }
                            }
                        } finally {
                            heartbeatJob.cancel()
                            sendJob.cancel()
                        }
                    }
                } catch (e: Exception) {
                    _connectionState.value = ConnectionState.Failed(e)
                } finally {
                    if (_connectionState.value is ConnectionState.Connected ||
                        _connectionState.value is ConnectionState.Connecting
                    ) {
                        _connectionState.value =
                            ConnectionState.Failed(
                                IllegalStateException("WebSocket disconnected"),
                            )
                    }
                    failAllPendingRequests()
                }
            }
    }

    private suspend fun handleTextFrame(text: String) {
        try {
            val json = XrplJson.decodeFromString<JsonObject>(text)
            val response = XrplJson.decodeFromJsonElement(WsJsonRpcResponse.serializer(), json)

            if (response.id != null) {
                // Request/response — route by id
                pendingMutex.withLock {
                    pendingRequests.remove(response.id)
                }?.complete(json)
            } else {
                // Subscription event — no id field
                _subscriptionEvents.emit(json)
            }
        } catch (_: Exception) {
            // Malformed frame — ignore
        }
    }

    private suspend fun failAllPendingRequests() {
        pendingMutex.withLock {
            val disconnectError =
                XrplException(
                    XrplFailure.NetworkError("WebSocket disconnected"),
                )
            pendingRequests.values.forEach { deferred ->
                deferred.completeExceptionally(disconnectError)
            }
            pendingRequests.clear()
        }
    }

    override suspend fun <T> request(
        method: String,
        params: JsonObject,
        deserializer: DeserializationStrategy<T>,
    ): XrplResult<T> {
        if (_connectionState.value !is ConnectionState.Connected) {
            return XrplResult.Failure(
                XrplFailure.NetworkError("WebSocket not connected"),
            )
        }

        val id = idCounter.fetchAndAdd(1)
        val deferred = CompletableDeferred<JsonObject>()

        pendingMutex.withLock {
            pendingRequests[id] = deferred
        }

        // Build WebSocket request: { "id": N, "command": "method", ...params }
        val requestJson =
            buildJsonObject {
                put("id", id)
                put("command", method)
                for ((key, value) in params) {
                    put(key, value)
                }
            }

        try {
            sendChannel.send(XrplJson.encodeToString(JsonObject.serializer(), requestJson))
        } catch (e: Exception) {
            pendingMutex.withLock { pendingRequests.remove(id) }
            return XrplResult.Failure(XrplFailure.NetworkError("Failed to send: ${e.message}", e))
        }

        return try {
            val responseJson = deferred.await()
            val wsResponse = XrplJson.decodeFromJsonElement(WsJsonRpcResponse.serializer(), responseJson)

            val rpcError = extractWsRpcError(wsResponse)
            if (rpcError != null) {
                return XrplResult.Failure(rpcError)
            }

            val resultObj = wsResponse.result ?: responseJson
            val decoded = XrplJson.decodeFromJsonElement(deserializer, resultObj)
            XrplResult.Success(decoded)
        } catch (e: kotlinx.coroutines.CancellationException) {
            pendingMutex.withLock { pendingRequests.remove(id) }
            throw e
        } catch (e: XrplException) {
            XrplResult.Failure(e.failure)
        } catch (e: Exception) {
            XrplResult.Failure(XrplFailure.NetworkError(e.message ?: "Unknown error", e))
        }
    }

    override fun close() {
        connectionJob?.cancel()
        sendChannel.close()
        client.close()
        if (_connectionState.value !is ConnectionState.Failed) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
