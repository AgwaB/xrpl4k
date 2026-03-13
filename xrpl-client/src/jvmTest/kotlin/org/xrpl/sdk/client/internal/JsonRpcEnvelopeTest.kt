@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.core.result.XrplFailure

class JsonRpcEnvelopeTest : FunSpec({

    // ── extractHttpRpcError ────────────────────────────────────────────────────

    test("extractHttpRpcError: status 'success' returns null") {
        val result = buildJsonObject { put("status", "success") }
        extractHttpRpcError(result) shouldBe null
    }

    test("extractHttpRpcError: status 'error' with error fields returns RpcError") {
        val result =
            buildJsonObject {
                put("status", "error")
                put("error", "actNotFound")
                put("error_code", 19)
                put("error_message", "Account not found")
            }
        val error = extractHttpRpcError(result)
        error.shouldBeInstanceOf<XrplFailure.RpcError>()
        error.errorCode shouldBe 19
        error.errorMessage shouldBe "Account not found"
    }

    test("extractHttpRpcError: missing status with error field returns RpcError") {
        val result =
            buildJsonObject {
                put("error", "txnNotFound")
                put("error_code", 29)
                put("error_message", "Transaction not found")
            }
        val error = extractHttpRpcError(result)
        error.shouldBeInstanceOf<XrplFailure.RpcError>()
        error.errorCode shouldBe 29
        error.errorMessage shouldBe "Transaction not found"
    }

    test("extractHttpRpcError: error without error_code defaults to -1") {
        val result =
            buildJsonObject {
                put("status", "error")
                put("error", "someError")
            }
        val error = extractHttpRpcError(result)
        error.shouldBeInstanceOf<XrplFailure.RpcError>()
        error shouldNotBe null
        error!!.errorCode shouldBe -1
        error.errorMessage shouldBe "someError"
    }

    // ── extractWsRpcError ──────────────────────────────────────────────────────

    test("extractWsRpcError: status 'success' returns null") {
        val response = WsJsonRpcResponse(id = 1, status = "success")
        extractWsRpcError(response) shouldBe null
    }

    test("extractWsRpcError: error response returns RpcError with code and message") {
        val response =
            WsJsonRpcResponse(
                id = 1,
                status = "error",
                error = "slowDown",
                errorCode = 56,
                errorMessage = "Too many requests",
            )
        val error = extractWsRpcError(response)
        error.shouldBeInstanceOf<XrplFailure.RpcError>()
        error.errorCode shouldBe 56
        error.errorMessage shouldBe "Too many requests"
    }

    test("extractWsRpcError: no status and no error returns null") {
        val response = WsJsonRpcResponse(id = 1, type = "response")
        extractWsRpcError(response) shouldBe null
    }

    test("extractWsRpcError: error without errorMessage falls back to error string") {
        val response =
            WsJsonRpcResponse(
                id = 2,
                status = "error",
                error = "actNotFound",
                errorCode = 19,
                errorMessage = null,
            )
        val error = extractWsRpcError(response)
        error.shouldBeInstanceOf<XrplFailure.RpcError>()
        error.errorMessage shouldBe "actNotFound"
    }

    test("extractWsRpcError: errorCode defaults to -1 when absent") {
        val response =
            WsJsonRpcResponse(
                id = 3,
                status = "error",
                error = "unknownError",
            )
        val error = extractWsRpcError(response)
        error.shouldBeInstanceOf<XrplFailure.RpcError>()
        error.errorCode shouldBe -1
    }
})
