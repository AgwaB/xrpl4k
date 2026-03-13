@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.errorResponse
import org.xrpl.sdk.client.TestHelper.jsonHeaders
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.TxHash

private val GENESIS_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
private val SAMPLE_TX_HASH = TxHash("E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879")

class RpcErrorScenariosTest : FunSpec({

    // ── RPC error response ────────────────────────────────────────

    test("unknown RPC command returns Failure with RpcError") {
        runTest {
            val client = clientWithMockEngine(errorResponse("unknownCmd", -1, "Unknown method."))
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.RpcError>()
                val rpcError = error as XrplFailure.RpcError
                rpcError.errorCode shouldBe -1
                rpcError.errorMessage shouldBe "Unknown method."
            }
        }
    }

    test("RPC error with non-zero error_code preserves code in RpcError") {
        runTest {
            val client = clientWithMockEngine(errorResponse("slowDown", 56, "Too many requests."))
            client.use { c ->
                val result = c.fee()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.RpcError>()
                (error as XrplFailure.RpcError).errorCode shouldBe 56
            }
        }
    }

    // ── Account not found → NotFound ─────────────────────────────

    test("actNotFound error code 19 returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("actNotFound", 19, "Account not found."))
            client.use { c ->
                val result = c.accountInfo(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    test("txnNotFound error code 29 returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("txnNotFound", 29, "Transaction not found."))
            client.use { c ->
                val result = c.tx(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    test("lgrNotFound error code 21 returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("lgrNotFound", 21, "Ledger not found."))
            client.use { c ->
                val result = c.ledger()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    // ── Network timeout / connectivity error ──────────────────────

    test("network exception returns Failure with NetworkError") {
        runTest {
            val client =
                clientWithMockEngine { _ ->
                    throw RuntimeException("Connection refused")
                }
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.NetworkError>()
            }
        }
    }

    test("network exception message is preserved in NetworkError") {
        runTest {
            val client =
                clientWithMockEngine { _ ->
                    throw RuntimeException("Connection timed out")
                }
            client.use { c ->
                val result = c.accountInfo(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.NetworkError>()
                (error as XrplFailure.NetworkError).message shouldBe "Connection timed out"
            }
        }
    }

    // ── Invalid JSON response ─────────────────────────────────────

    test("invalid JSON body returns Failure with NetworkError") {
        runTest {
            val client =
                clientWithMockEngine { _ ->
                    respond(
                        content = ByteReadChannel("this is not json"),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error.shouldBeInstanceOf<XrplFailure.NetworkError>()
            }
        }
    }

    test("response missing result field returns Failure with NetworkError") {
        runTest {
            val client =
                clientWithMockEngine { _ ->
                    respond(
                        content = ByteReadChannel("""{"no_result_here":true}"""),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.NetworkError>()
                (error as XrplFailure.NetworkError).message shouldBe "Missing 'result' field in response"
            }
        }
    }

    // ── HTTP 5xx error ────────────────────────────────────────────

    test("HTTP 500 response returns Failure with NetworkError") {
        runTest {
            val client =
                clientWithMockEngine { _ ->
                    respondError(HttpStatusCode.InternalServerError)
                }
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.NetworkError>()
                (error as XrplFailure.NetworkError).message shouldBe "HTTP 500: Internal Server Error"
            }
        }
    }

    test("HTTP 503 response returns Failure with NetworkError") {
        runTest {
            val client =
                clientWithMockEngine { _ ->
                    respondError(HttpStatusCode.ServiceUnavailable)
                }
            client.use { c ->
                val result = c.fee()
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.NetworkError>()
                (error as XrplFailure.NetworkError).message shouldBe "HTTP 503: Service Unavailable"
            }
        }
    }

    // ── TEC error classification ──────────────────────────────────

    test("RPC error with code in 100-199 range is classified as TecError") {
        runTest {
            val client = clientWithMockEngine(errorResponse("tecUNFUNDED_PAYMENT", 104, "Insufficient XRP balance."))
            client.use { c ->
                val result = c.accountInfo(GENESIS_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val error = (result as XrplResult.Failure).error
                error.shouldBeInstanceOf<XrplFailure.TecError>()
                val tecError = error as XrplFailure.TecError
                tecError.code shouldBe 104
                tecError.message shouldBe "Insufficient XRP balance."
            }
        }
    }
})
