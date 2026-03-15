package org.xrpl.sdk.client.transport

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
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
 * Persistent WebSocket transport with message routing, auto-reconnect,
 * and auto-resubscribe support.
 *
 * When [autoReconnect] is enabled and the connection drops unexpectedly,
 * the transport automatically reconnects with exponential backoff and
 * replays all tracked subscriptions.
 *
 * @param url the WebSocket endpoint URL.
 * @param engine the HTTP client engine.
 * @param scope the coroutine scope for background operations.
 * @param heartbeatInterval interval between heartbeat pings.
 * @param requestTimeout timeout for individual RPC requests.
 * @param autoReconnect whether to auto-reconnect on unexpected disconnect.
 * @param maxReconnectAttempts maximum number of reconnect attempts.
 * @param initialReconnectDelay initial delay before the first reconnect attempt.
 * @param maxReconnectDelay maximum delay between reconnect attempts.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class WebSocketTransport(
    private val url: String,
    engine: HttpClientEngine,
    private val scope: CoroutineScope,
    private val heartbeatInterval: Duration,
    private val requestTimeout: Duration,
    private val autoReconnect: Boolean = true,
    private val maxReconnectAttempts: Int = Int.MAX_VALUE,
    private val initialReconnectDelay: Duration = Duration.ZERO,
    private val maxReconnectDelay: Duration = Duration.ZERO,
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
    private val connectMutex = Mutex()
    private var connectionJob: Job? = null
    private var sendChannel = Channel<String>(Channel.BUFFERED)

    /** Whether close() has been explicitly called. Prevents reconnect after intentional shutdown. */
    private var closedExplicitly = false

    /** Whether a reconnect loop is currently active. Prevents cascading reconnect spawns. */
    private var reconnecting = false

    // ── Subscription Registry ───────────────────────────────────────────────────

    /**
     * Represents an active subscription that should be replayed after reconnect.
     */
    internal sealed class SubscriptionEntry {
        data class Streams(val streams: List<String>) : SubscriptionEntry()

        data class Accounts(val accounts: List<String>) : SubscriptionEntry()

        data class Books(val books: JsonObject) : SubscriptionEntry()
    }

    private val registryMutex = Mutex()
    private val subscriptionRegistry = mutableListOf<SubscriptionEntry>()

    /**
     * Tracks a subscription so it can be replayed after reconnect.
     * Called internally by the subscription layer.
     */
    suspend fun trackSubscription(entry: SubscriptionEntry) {
        registryMutex.withLock {
            subscriptionRegistry.add(entry)
        }
    }

    /**
     * Removes a tracked subscription (e.g., when a Flow is cancelled).
     */
    suspend fun untrackSubscription(entry: SubscriptionEntry) {
        registryMutex.withLock {
            subscriptionRegistry.remove(entry)
        }
    }

    // ── Connection ──────────────────────────────────────────────────────────────

    /**
     * Establishes the WebSocket connection and starts the message loop.
     * Must be called before [request].
     */
    suspend fun connect() {
        connectMutex.withLock {
            if (_connectionState.value is ConnectionState.Connected) return
            if (_connectionState.value is ConnectionState.Connecting) {
                // Already connecting — release lock and wait below
            } else {
                _connectionState.value = ConnectionState.Connecting

                // Create a fresh send channel for the new connection
                sendChannel = Channel(Channel.BUFFERED)

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
                                    sendChannel.close()
                                    sendJob.cancel()
                                }
                            }
                        } catch (e: Exception) {
                            _connectionState.value = ConnectionState.Failed(e)
                        } finally {
                            // Only transition to Failed if we're still Connected/Connecting
                            // (don't overwrite if close() already set Disconnected)
                            val current = _connectionState.value
                            if (current is ConnectionState.Connected ||
                                current is ConnectionState.Connecting
                            ) {
                                _connectionState.value =
                                    ConnectionState.Failed(
                                        IllegalStateException("WebSocket disconnected"),
                                    )
                            }
                            failAllPendingRequests()

                            // Trigger auto-reconnect if enabled, not explicitly closed,
                            // and not already inside a reconnect loop.
                            if (autoReconnect && !closedExplicitly && !reconnecting) {
                                val failCause =
                                    (_connectionState.value as? ConnectionState.Failed)?.cause
                                        ?: IllegalStateException("WebSocket disconnected")
                                scope.launch { reconnect(failCause) }
                            }
                        }
                    }
            }
        }

        // Wait OUTSIDE the lock so the launched job can update state
        _connectionState.first { it is ConnectionState.Connected || it is ConnectionState.Failed }
    }

    /**
     * Reconnects with exponential backoff after an unexpected disconnect.
     *
     * Transitions through [ConnectionState.Reconnecting] states.
     * On successful reconnect, replays all tracked subscriptions.
     */
    private suspend fun reconnect(cause: Throwable) {
        reconnecting = true
        try {
            var currentDelay = initialReconnectDelay
            var attempt = 0

            while (attempt < maxReconnectAttempts && !closedExplicitly) {
                attempt++
                _connectionState.value = ConnectionState.Reconnecting(attempt, cause)

                delay(currentDelay)

                if (closedExplicitly) break

                try {
                    connect()
                } catch (_: Exception) {
                    // connect() handles its own state transitions — just continue the loop
                }

                if (_connectionState.value is ConnectionState.Connected) {
                    // Reconnect succeeded — replay subscriptions
                    replaySubscriptions()
                    return
                }

                // Exponential backoff: double the delay, capped at maxReconnectDelay
                currentDelay = minDuration(currentDelay * 2, maxReconnectDelay)
            }

            // Exhausted all attempts — remain in Failed state
            if (!closedExplicitly) {
                _connectionState.value =
                    ConnectionState.Failed(
                        IllegalStateException(
                            "Auto-reconnect failed after $attempt attempts",
                            cause,
                        ),
                    )
            }
        } finally {
            reconnecting = false
        }
    }

    /**
     * Replays all tracked subscriptions after a successful reconnect.
     */
    private suspend fun replaySubscriptions() {
        val entries = registryMutex.withLock { subscriptionRegistry.toList() }
        for (entry in entries) {
            try {
                when (entry) {
                    is SubscriptionEntry.Streams -> {
                        val requestJson =
                            buildJsonObject {
                                put("command", "subscribe")
                                put(
                                    "streams",
                                    kotlinx.serialization.json.JsonArray(
                                        entry.streams.map { kotlinx.serialization.json.JsonPrimitive(it) },
                                    ),
                                )
                            }
                        sendRaw(XrplJson.encodeToString(JsonObject.serializer(), requestJson))
                    }
                    is SubscriptionEntry.Accounts -> {
                        val requestJson =
                            buildJsonObject {
                                put("command", "subscribe")
                                put(
                                    "accounts",
                                    kotlinx.serialization.json.JsonArray(
                                        entry.accounts.map { kotlinx.serialization.json.JsonPrimitive(it) },
                                    ),
                                )
                            }
                        sendRaw(XrplJson.encodeToString(JsonObject.serializer(), requestJson))
                    }
                    is SubscriptionEntry.Books -> {
                        val requestJson =
                            buildJsonObject {
                                put("command", "subscribe")
                                for ((key, value) in entry.books) {
                                    put(key, value)
                                }
                            }
                        sendRaw(XrplJson.encodeToString(JsonObject.serializer(), requestJson))
                    }
                }
            } catch (_: Exception) {
                // Best-effort replay — individual failures don't abort the entire replay
            }
        }
    }

    /**
     * Sends a raw message string through the send channel.
     */
    private suspend fun sendRaw(message: String) {
        sendChannel.send(message)
    }

    // ── Message Handling ────────────────────────────────────────────────────────

    private suspend fun handleTextFrame(text: String) {
        val json =
            try {
                XrplJson.decodeFromString<JsonObject>(text)
            } catch (_: Exception) {
                return // Unparseable frame — skip
            }

        val response =
            try {
                XrplJson.decodeFromJsonElement(WsJsonRpcResponse.serializer(), json)
            } catch (_: Exception) {
                // Valid JSON but not a recognizable RPC envelope — treat as subscription event
                _subscriptionEvents.tryEmit(json)
                return
            }

        if (response.id != null) {
            val deferred =
                pendingMutex.withLock {
                    pendingRequests.remove(response.id)
                }
            deferred?.complete(json)
            // If deferred is null, response arrived for an already-timed-out or cancelled request — ignore
        } else {
            _subscriptionEvents.tryEmit(json)
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

    // ── RPC Requests ────────────────────────────────────────────────────────────

    override suspend fun <T> request(
        method: String,
        params: JsonObject,
        deserializer: DeserializationStrategy<T>,
    ): XrplResult<T> {
        val state = _connectionState.value
        if (state !is ConnectionState.Connected) {
            // If reconnecting, wait for reconnect to complete (up to requestTimeout)
            if (state is ConnectionState.Reconnecting) {
                try {
                    withTimeout(requestTimeout) {
                        _connectionState.first {
                            it is ConnectionState.Connected || it is ConnectionState.Failed ||
                                it is ConnectionState.Disconnected
                        }
                    }
                } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                    return XrplResult.Failure(
                        XrplFailure.NetworkError("WebSocket reconnecting — timed out waiting"),
                    )
                }
                if (_connectionState.value !is ConnectionState.Connected) {
                    return XrplResult.Failure(
                        XrplFailure.NetworkError("WebSocket not connected"),
                    )
                }
            } else {
                return XrplResult.Failure(
                    XrplFailure.NetworkError("WebSocket not connected"),
                )
            }
        }

        var id = idCounter.fetchAndAdd(1)
        if (id < 0) {
            // Overflow — reset to positive range
            idCounter.store(1)
            id = 1
        }
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
            val responseJson =
                withTimeout(requestTimeout) {
                    deferred.await()
                }
            val wsResponse = XrplJson.decodeFromJsonElement(WsJsonRpcResponse.serializer(), responseJson)

            val rpcError = extractWsRpcError(wsResponse)
            if (rpcError != null) {
                return XrplResult.Failure(rpcError)
            }

            val resultObj = wsResponse.result ?: responseJson
            val decoded = XrplJson.decodeFromJsonElement(deserializer, resultObj)
            XrplResult.Success(decoded)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            pendingMutex.withLock { pendingRequests.remove(id) }
            XrplResult.Failure(XrplFailure.NetworkError("Request timed out after $requestTimeout"))
        } catch (e: kotlinx.coroutines.CancellationException) {
            pendingMutex.withLock { pendingRequests.remove(id) }
            throw e
        } catch (e: XrplException) {
            pendingMutex.withLock { pendingRequests.remove(id) }
            XrplResult.Failure(e.failure)
        } catch (e: Exception) {
            pendingMutex.withLock { pendingRequests.remove(id) }
            XrplResult.Failure(XrplFailure.NetworkError(e.message ?: "Unknown error", e))
        }
    }

    // ── Shutdown ─────────────────────────────────────────────────────────────────

    override fun close() {
        closedExplicitly = true
        val acquiredConnectLock = connectMutex.tryLock()
        try {
            connectionJob?.cancel()
            connectionJob = null
            sendChannel.close()

            // Fail all pending requests so callers don't hang forever.
            // CompletableDeferred.completeExceptionally is thread-safe; duplicate
            // calls are harmless (only the first wins). After cancelling the
            // connectionJob and closing the sendChannel, no new requests will be
            // enqueued, so iterating without the pendingMutex is safe here.
            val error = XrplException(XrplFailure.NetworkError("WebSocket closed"))
            pendingRequests.values.forEach { it.completeExceptionally(error) }
            pendingRequests.clear()

            client.close()
            if (_connectionState.value !is ConnectionState.Failed) {
                _connectionState.value = ConnectionState.Disconnected
            }
        } finally {
            if (acquiredConnectLock) {
                connectMutex.unlock()
            }
        }
    }
}

/**
 * Returns the smaller of two Durations without requiring `compareTo` on all KMP targets.
 */
private fun minDuration(
    a: Duration,
    b: Duration,
): Duration {
    return if (a.inWholeMilliseconds <= b.inWholeMilliseconds) a else b
}
