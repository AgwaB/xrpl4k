package org.xrpl.sdk.client.transport

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonObject
import org.xrpl.sdk.core.result.XrplResult

/**
 * Abstract transport contract for XRPL JSON-RPC communication.
 *
 * Both HTTP and WebSocket transports implement this interface.
 * This type is internal — it is never exposed in the public API.
 */
internal interface XrplTransport : AutoCloseable {
    /**
     * Sends an RPC request and deserializes the response.
     *
     * @param method the RPC method name (e.g., "account_info").
     * @param params the request parameters as a [JsonObject].
     * @param deserializer the deserialization strategy for the response type.
     * @return an [XrplResult] containing the deserialized response or a failure.
     */
    suspend fun <T> request(
        method: String,
        params: JsonObject,
        deserializer: DeserializationStrategy<T>,
    ): XrplResult<T>
}
