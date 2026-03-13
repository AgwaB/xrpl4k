package org.xrpl.sdk.core.result

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class XrplResultTest : FunSpec({

    context("Success path") {
        test("getOrNull returns value") {
            val result: XrplResult<String> = XrplResult.Success("hello")
            result.getOrNull() shouldBe "hello"
        }

        test("getOrThrow returns value") {
            val result: XrplResult<Int> = XrplResult.Success(42)
            result.getOrThrow() shouldBe 42
        }

        test("map transforms success value") {
            val result: XrplResult<Int> = XrplResult.Success(5)
            val mapped = result.map { it * 2 }
            mapped.getOrNull() shouldBe 10
        }
    }

    context("Failure path") {
        test("getOrNull returns null") {
            val result: XrplResult<String> = XrplResult.Failure(XrplFailure.NotFound)
            result.getOrNull() shouldBe null
        }

        test("getOrThrow throws XrplException with correct failure") {
            val failure = XrplFailure.ValidationError("bad field")
            val result: XrplResult<String> = XrplResult.Failure(failure)
            val ex = shouldThrow<XrplException> { result.getOrThrow() }
            ex.failure shouldBe failure
        }
    }

    context("Exhaustive when — XrplResult") {
        test("when over XrplResult compiles without else") {
            val result: XrplResult<String> = XrplResult.Success("ok")
            // If a new subtype were added the compiler would force us to add a branch.
            val label =
                when (result) {
                    is XrplResult.Success -> "success"
                    is XrplResult.Failure -> "failure"
                }
            label shouldBe "success"
        }
    }

    context("Exhaustive when — XrplFailure") {
        test("when over XrplFailure covers all 5 subtypes without else") {
            val failures: List<XrplFailure> =
                listOf(
                    XrplFailure.RpcError(1, "rpc"),
                    XrplFailure.NetworkError("net"),
                    XrplFailure.ValidationError("val"),
                    XrplFailure.TecError(100, "tec"),
                    XrplFailure.NotFound,
                )
            val labels =
                failures.map { f ->
                    when (f) {
                        is XrplFailure.RpcError -> "rpc"
                        is XrplFailure.NetworkError -> "network"
                        is XrplFailure.ValidationError -> "validation"
                        is XrplFailure.TecError -> "tec"
                        is XrplFailure.NotFound -> "notfound"
                    }
                }
            labels shouldBe listOf("rpc", "network", "validation", "tec", "notfound")
        }
    }

    context("map") {
        test("transforms success") {
            val result: XrplResult<Int> = XrplResult.Success(3)
            result.map { it.toString() }.getOrNull() shouldBe "3"
        }

        test("passes through failure unchanged") {
            val failure = XrplFailure.NetworkError("timeout")
            val result: XrplResult<Int> = XrplResult.Failure(failure)
            val mapped = result.map { it * 10 }
            mapped.shouldBeInstanceOf<XrplResult.Failure>()
            (mapped as XrplResult.Failure).error shouldBe failure
        }
    }

    context("flatMap") {
        test("chains successes") {
            val result: XrplResult<Int> = XrplResult.Success(4)
            val chained = result.flatMap { XrplResult.Success(it * 2) }
            chained.getOrNull() shouldBe 8
        }

        test("short-circuits on failure") {
            val failure = XrplFailure.RpcError(23, "err")
            val result: XrplResult<Int> = XrplResult.Failure(failure)
            var sideEffect = false
            val chained =
                result.flatMap {
                    sideEffect = true
                    XrplResult.Success(it * 2)
                }
            sideEffect shouldBe false
            chained.shouldBeInstanceOf<XrplResult.Failure>()
            (chained as XrplResult.Failure).error shouldBe failure
        }
    }

    context("onSuccess") {
        test("executes action on success") {
            var called = false
            XrplResult.Success("x").onSuccess { called = true }
            called shouldBe true
        }

        test("skips action on failure") {
            var called = false
            XrplResult.Failure(XrplFailure.NotFound).onSuccess { called = true }
            called shouldBe false
        }
    }

    context("onFailure") {
        test("executes action on failure") {
            var called = false
            XrplResult.Failure(XrplFailure.NotFound).onFailure { called = true }
            called shouldBe true
        }

        test("skips action on success") {
            var called = false
            XrplResult.Success("x").onFailure { called = true }
            called shouldBe false
        }
    }

    context("fold") {
        test("handles success branch") {
            val result: XrplResult<Int> = XrplResult.Success(7)
            val out = result.fold(onSuccess = { "ok:$it" }, onFailure = { "fail" })
            out shouldBe "ok:7"
        }

        test("handles failure branch") {
            val result: XrplResult<Int> = XrplResult.Failure(XrplFailure.NotFound)
            val out = result.fold(onSuccess = { "ok" }, onFailure = { "fail:${it::class.simpleName}" })
            out shouldBe "fail:NotFound"
        }
    }

    context("chaining") {
        test("onSuccess then onFailure then getOrNull") {
            var successCalled = false
            var failureCalled = false
            val value =
                XrplResult.Success("chain")
                    .onSuccess { successCalled = true }
                    .onFailure { failureCalled = true }
                    .getOrNull()
            value shouldBe "chain"
            successCalled shouldBe true
            failureCalled shouldBe false
        }
    }

    context("XrplException") {
        test("contains the original failure") {
            val failure = XrplFailure.TecError(105, "tecPATH_DRY")
            val ex = XrplException(failure)
            ex.failure shouldBe failure
        }

        test("message includes failure description") {
            val failure = XrplFailure.ValidationError("missing Fee")
            val ex = XrplException(failure)
            ex.message?.contains("XRPL operation failed") shouldBe true
        }
    }
})
