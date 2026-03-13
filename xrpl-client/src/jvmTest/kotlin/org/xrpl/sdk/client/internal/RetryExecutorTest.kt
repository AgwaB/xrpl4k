@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.RetryConfig
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import kotlin.time.Duration.Companion.milliseconds

class RetryExecutorTest : FunSpec({

    fun shortRetryConfig(maxAttempts: Int = 3) =
        RetryConfig().apply {
            this.maxAttempts = maxAttempts
            initialDelay = 1.milliseconds
            maxDelay = 10.milliseconds
        }

    test("success on first attempt returns success with no retries") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig()) {
                    callCount++
                    XrplResult.Success("ok")
                }
            result.shouldBeInstanceOf<XrplResult.Success<String>>()
            (result as XrplResult.Success).value shouldBe "ok"
            callCount shouldBe 1
        }
    }

    test("NetworkError retries up to maxAttempts") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig(maxAttempts = 3)) {
                    callCount++
                    XrplResult.Failure(XrplFailure.NetworkError("connection failed"))
                }
            result.shouldBeInstanceOf<XrplResult.Failure>()
            callCount shouldBe 3
        }
    }

    test("transient RpcError (code 56) retries") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig(maxAttempts = 3)) {
                    callCount++
                    XrplResult.Failure(XrplFailure.RpcError(errorCode = 56, errorMessage = "slowDown"))
                }
            result.shouldBeInstanceOf<XrplResult.Failure>()
            callCount shouldBe 3
        }
    }

    test("ValidationError does not retry") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig()) {
                    callCount++
                    XrplResult.Failure(XrplFailure.ValidationError("bad field"))
                }
            result.shouldBeInstanceOf<XrplResult.Failure>()
            callCount shouldBe 1
        }
    }

    test("NotFound does not retry") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig()) {
                    callCount++
                    XrplResult.Failure(XrplFailure.NotFound)
                }
            result.shouldBeInstanceOf<XrplResult.Failure>()
            callCount shouldBe 1
        }
    }

    test("TecError does not retry") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig()) {
                    callCount++
                    XrplResult.Failure(XrplFailure.TecError(code = 100, message = "tecCLAIM"))
                }
            result.shouldBeInstanceOf<XrplResult.Failure>()
            callCount shouldBe 1
        }
    }

    test("success after transient failures returns success") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig(maxAttempts = 3)) {
                    callCount++
                    if (callCount < 3) {
                        XrplResult.Failure(XrplFailure.NetworkError("transient"))
                    } else {
                        XrplResult.Success("recovered")
                    }
                }
            result.shouldBeInstanceOf<XrplResult.Success<String>>()
            (result as XrplResult.Success).value shouldBe "recovered"
            callCount shouldBe 3
        }
    }

    test("maxAttempts=1 means no retries on failure") {
        runTest {
            var callCount = 0
            val result =
                withRetry(shortRetryConfig(maxAttempts = 1)) {
                    callCount++
                    XrplResult.Failure(XrplFailure.NetworkError("error"))
                }
            result.shouldBeInstanceOf<XrplResult.Failure>()
            callCount shouldBe 1
        }
    }
})
