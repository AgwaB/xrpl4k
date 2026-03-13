package org.xrpl.sdk.client.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonObject
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult

/**
 * Known XRPL string error names mapped to their numeric error codes.
 * Used to normalize mixed error formats from the XRPL node.
 */
private val knownErrorCodes: Map<String, Int> =
    mapOf(
        "actNotFound" to 19,
        "actMalformed" to 35,
        "lgrNotFound" to 21,
        "txnNotFound" to 29,
        "slowDown" to 56,
        "tooBusy" to 14,
        "noCurrent" to 23,
        "noNetwork" to 17,
        "amendmentBlocked" to 1,
    )

/**
 * Central RPC execution method that all public RPC methods delegate to.
 *
 * Handles serialization, transport dispatch, and error mapping.
 *
 * @param method the RPC method name (e.g., "account_info").
 * @param request the request DTO.
 * @param requestSerializer serializer for the request.
 * @param responseDeserializer deserializer for the raw response.
 * @param mapper maps the raw response DTO to the public domain model.
 * @return [XrplResult] with the domain model or a categorized failure.
 */
internal suspend fun <Req, Resp, T> XrplClient.executeRpc(
    method: String,
    request: Req,
    requestSerializer: SerializationStrategy<Req>,
    responseDeserializer: DeserializationStrategy<Resp>,
    mapper: (Resp) -> T,
): XrplResult<T> {
    val paramsJson = XrplJson.encodeToJsonElement(requestSerializer, request)
    val params =
        when (paramsJson) {
            is JsonObject -> paramsJson
            else -> JsonObject(emptyMap())
        }

    val result = httpTransport.request(method, params, responseDeserializer)

    return when (result) {
        is XrplResult.Success -> {
            try {
                XrplResult.Success(mapper(result.value))
            } catch (e: Exception) {
                XrplResult.Failure(
                    XrplFailure.NetworkError("Failed to map response: ${e.message}", e),
                )
            }
        }
        is XrplResult.Failure -> {
            // Re-classify certain RPC errors
            val classified = classifyError(result.error)
            XrplResult.Failure(classified)
        }
    }
}

/**
 * Executes an RPC call over the WebSocket transport.
 * Used for subscribe/unsubscribe which are WebSocket-only commands.
 */
internal suspend fun <Req, Resp, T> XrplClient.executeWsRpc(
    method: String,
    request: Req,
    requestSerializer: SerializationStrategy<Req>,
    responseDeserializer: DeserializationStrategy<Resp>,
    mapper: (Resp) -> T,
): XrplResult<T> {
    val paramsJson = XrplJson.encodeToJsonElement(requestSerializer, request)
    val params =
        when (paramsJson) {
            is JsonObject -> paramsJson
            else -> JsonObject(emptyMap())
        }

    val result = webSocketTransport.request(method, params, responseDeserializer)

    return when (result) {
        is XrplResult.Success -> {
            try {
                XrplResult.Success(mapper(result.value))
            } catch (e: Exception) {
                XrplResult.Failure(
                    XrplFailure.NetworkError(
                        "Failed to map response: ${e.message}",
                        e,
                    ),
                )
            }
        }
        is XrplResult.Failure -> XrplResult.Failure(classifyError(result.error))
    }
}

/**
 * Overload for methods with no request parameters (e.g., `ping`, `fee`).
 */
internal suspend fun <Resp, T> XrplClient.executeRpc(
    method: String,
    responseDeserializer: DeserializationStrategy<Resp>,
    mapper: (Resp) -> T,
): XrplResult<T> {
    val params = JsonObject(emptyMap())
    val result = httpTransport.request(method, params, responseDeserializer)

    return when (result) {
        is XrplResult.Success -> {
            try {
                XrplResult.Success(mapper(result.value))
            } catch (e: Exception) {
                XrplResult.Failure(
                    XrplFailure.NetworkError("Failed to map response: ${e.message}", e),
                )
            }
        }
        is XrplResult.Failure -> XrplResult.Failure(classifyError(result.error))
    }
}

/**
 * Re-classifies RPC errors into more specific failure types:
 * - `actNotFound` / `txnNotFound` / `lgrNotFound` → [XrplFailure.NotFound]
 * - `tec*` codes → [XrplFailure.TecError]
 */
internal fun classifyError(error: XrplFailure): XrplFailure {
    if (error !is XrplFailure.RpcError) return error

    val message = error.errorMessage.lowercase()

    // NotFound classification
    if (message.contains("notfound") || message.contains("not_found") ||
        error.errorCode == 19 || error.errorCode == 29 || error.errorCode == 21
    ) {
        return XrplFailure.NotFound
    }

    // TecError classification (engine result codes 100-199)
    if (error.errorCode in 100..199 || message.startsWith("tec")) {
        return XrplFailure.TecError(code = error.errorCode, message = error.errorMessage)
    }

    return error
}

/**
 * Checks whether an RPC error is transient and eligible for retry.
 *
 * Transient errors include:
 * - `slowDown` (numeric code 56)
 * - `tooBusy` (numeric code 14)
 * - Matching string patterns in the error message
 */
internal fun isTransientRpcError(error: XrplFailure.RpcError): Boolean {
    if (error.errorCode == 56 || error.errorCode == 14) return true
    val msg = error.errorMessage.lowercase()
    return msg.contains("slowdown") || msg.contains("slow_down") ||
        msg.contains("toobusy") || msg.contains("too_busy")
}
