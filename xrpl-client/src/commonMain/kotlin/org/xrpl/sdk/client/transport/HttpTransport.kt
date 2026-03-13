package org.xrpl.sdk.client.transport

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonObject
import org.xrpl.sdk.client.internal.HttpJsonRpcRequest
import org.xrpl.sdk.client.internal.XrplJson
import org.xrpl.sdk.client.internal.extractHttpRpcError
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import kotlin.time.Duration

/**
 * JSON-RPC over HTTP transport using Ktor.
 *
 * @param engine the HTTP client engine (e.g., CIO, Darwin, MockEngine).
 * @param url the RPC endpoint URL.
 * @param timeout the request timeout duration.
 */
internal class HttpTransport(
    engine: HttpClientEngine,
    private val url: String,
    timeout: Duration,
) : XrplTransport {
    private val client: HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(XrplJson)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = timeout.inWholeMilliseconds
                connectTimeoutMillis = timeout.inWholeMilliseconds
                socketTimeoutMillis = timeout.inWholeMilliseconds
            }
        }

    override suspend fun <T> request(
        method: String,
        params: JsonObject,
        deserializer: DeserializationStrategy<T>,
    ): XrplResult<T> {
        return try {
            val envelope =
                HttpJsonRpcRequest(
                    method = method,
                    params = listOf(params),
                )
            val requestBody = XrplJson.encodeToString(HttpJsonRpcRequest.serializer(), envelope)

            val response =
                client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            val statusCode = response.status.value
            if (statusCode !in 200..299) {
                return XrplResult.Failure(
                    XrplFailure.NetworkError("HTTP $statusCode: ${response.status.description}"),
                )
            }

            val responseBody = response.bodyAsText()
            val responseJson = XrplJson.decodeFromString<JsonObject>(responseBody)

            val resultObj =
                responseJson["result"]?.let {
                    XrplJson.decodeFromJsonElement(JsonObject.serializer(), it)
                } ?: return XrplResult.Failure(
                    XrplFailure.NetworkError("Missing 'result' field in response"),
                )

            val rpcError = extractHttpRpcError(resultObj)
            if (rpcError != null) {
                return XrplResult.Failure(rpcError)
            }

            val decoded = XrplJson.decodeFromJsonElement(deserializer, resultObj)
            XrplResult.Success(decoded)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            XrplResult.Failure(XrplFailure.NetworkError(e.message ?: "Unknown network error", e))
        }
    }

    override fun close() {
        client.close()
    }
}
