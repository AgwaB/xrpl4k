package org.xrpl.sdk.client.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.xrpl.sdk.core.result.XrplFailure

/**
 * JSON-RPC request envelope for HTTP transport.
 * Format: `{ "method": "<method>", "params": [{ ... }] }`
 */
@Serializable
internal data class HttpJsonRpcRequest(
    val method: String,
    val params: List<JsonObject> = listOf(JsonObject(emptyMap())),
)

/**
 * JSON-RPC response envelope for HTTP transport.
 * Format: `{ "result": { "status": "success|error", ... } }`
 */
@Serializable
internal data class HttpJsonRpcResponse(
    val result: JsonObject,
)

/**
 * WebSocket JSON-RPC request envelope.
 * Format: `{ "id": <int>, "command": "<method>", ...params }`
 */
@Serializable
internal data class WsJsonRpcRequest(
    val id: Int,
    val command: String,
)

/**
 * WebSocket JSON-RPC response envelope.
 * Format: `{ "id": <int>, "type": "response", "status": "success|error", "result": { ... } }`
 */
@Serializable
internal data class WsJsonRpcResponse(
    val id: Int? = null,
    val type: String? = null,
    val status: String? = null,
    val result: JsonObject? = null,
    val error: String? = null,
    @SerialName("error_code") val errorCode: Int? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("error_exception") val errorException: String? = null,
)

/**
 * Extracts an [XrplFailure.RpcError] from an HTTP JSON-RPC result object,
 * or `null` if the result represents a success.
 */
internal fun extractHttpRpcError(result: JsonObject): XrplFailure.RpcError? {
    val status = result["status"]?.jsonPrimitive?.contentOrNull
    if (status == "success") return null

    val errorStr = result["error"]?.jsonPrimitive?.contentOrNull ?: "unknown_error"
    val errorCode = result["error_code"]?.jsonPrimitive?.intOrNull ?: -1
    val errorMessage = result["error_message"]?.jsonPrimitive?.contentOrNull ?: errorStr

    return XrplFailure.RpcError(errorCode = errorCode, errorMessage = errorMessage)
}

/**
 * Extracts an [XrplFailure.RpcError] from a WebSocket JSON-RPC response,
 * or `null` if the response represents a success.
 */
internal fun extractWsRpcError(response: WsJsonRpcResponse): XrplFailure.RpcError? {
    if (response.status == "success") return null
    if (response.error == null && response.status == null) return null

    return XrplFailure.RpcError(
        errorCode = response.errorCode ?: -1,
        errorMessage = response.errorMessage ?: response.error ?: "unknown_error",
    )
}
