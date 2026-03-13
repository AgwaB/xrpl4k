@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.xrpl.sdk.core.result.XrplFailure

class RpcExecutorTest : FunSpec({

    // ── classifyError ──────────────────────────────────────────────────────────

    test("classifyError: RpcError with code 19 (actNotFound) returns NotFound") {
        val error = XrplFailure.RpcError(errorCode = 19, errorMessage = "actNotFound")
        val result = classifyError(error)
        result shouldBe XrplFailure.NotFound
    }

    test("classifyError: RpcError with code 29 (txnNotFound) returns NotFound") {
        val error = XrplFailure.RpcError(errorCode = 29, errorMessage = "txnNotFound")
        val result = classifyError(error)
        result shouldBe XrplFailure.NotFound
    }

    test("classifyError: RpcError with message containing 'notfound' returns NotFound") {
        val error = XrplFailure.RpcError(errorCode = 0, errorMessage = "lgrNotFound")
        val result = classifyError(error)
        result shouldBe XrplFailure.NotFound
    }

    test("classifyError: RpcError with code 100 returns TecError") {
        val error = XrplFailure.RpcError(errorCode = 100, errorMessage = "tecCLAIM")
        val result = classifyError(error)
        result.shouldBeInstanceOf<XrplFailure.TecError>()
        val tec = result as XrplFailure.TecError
        tec.code shouldBe 100
        tec.message shouldBe "tecCLAIM"
    }

    test("classifyError: RpcError with code 199 returns TecError") {
        val error = XrplFailure.RpcError(errorCode = 199, errorMessage = "tecSOMETHING")
        val result = classifyError(error)
        result.shouldBeInstanceOf<XrplFailure.TecError>()
    }

    test("classifyError: RpcError with message starting with 'tec' returns TecError") {
        val error = XrplFailure.RpcError(errorCode = 0, errorMessage = "tecNO_TARGET")
        val result = classifyError(error)
        result.shouldBeInstanceOf<XrplFailure.TecError>()
    }

    test("classifyError: non-RpcError passes through unchanged") {
        val error = XrplFailure.NetworkError("connection reset")
        val result = classifyError(error)
        result shouldBe error
    }

    test("classifyError: regular RpcError passes through unchanged") {
        val error = XrplFailure.RpcError(errorCode = 35, errorMessage = "actMalformed")
        val result = classifyError(error)
        result shouldBe error
    }

    // ── isTransientRpcError ────────────────────────────────────────────────────

    test("isTransientRpcError: code 56 (slowDown) returns true") {
        val error = XrplFailure.RpcError(errorCode = 56, errorMessage = "slowDown")
        isTransientRpcError(error) shouldBe true
    }

    test("isTransientRpcError: code 14 (tooBusy) returns true") {
        val error = XrplFailure.RpcError(errorCode = 14, errorMessage = "tooBusy")
        isTransientRpcError(error) shouldBe true
    }

    test("isTransientRpcError: message containing 'slowdown' returns true") {
        val error = XrplFailure.RpcError(errorCode = 0, errorMessage = "slowdown please retry")
        isTransientRpcError(error) shouldBe true
    }

    test("isTransientRpcError: regular RpcError returns false") {
        val error = XrplFailure.RpcError(errorCode = 35, errorMessage = "actMalformed")
        isTransientRpcError(error) shouldBe false
    }
})
