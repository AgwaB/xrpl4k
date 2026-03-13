@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.transport

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import kotlin.time.Duration.Companion.seconds

class HttpTransportTest : FunSpec({

    fun jsonHeaders() = headersOf("Content-Type", ContentType.Application.Json.toString())

    fun makeTransport(engine: MockEngine) =
        HttpTransport(engine = engine, url = "http://localhost:5005", timeout = 30.seconds)

    test("successful JSON-RPC request returns XrplResult.Success") {
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content =
                            ByteReadChannel(
                                """{"result":{"status":"success","ledger_index":12345}}""",
                            ),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }
            val transport = makeTransport(engine)
            val result = transport.request("ledger", JsonObject(emptyMap()), JsonObject.serializer())

            result.shouldBeInstanceOf<XrplResult.Success<JsonObject>>()
        }
    }

    test("HTTP 500 returns XrplResult.Failure with NetworkError") {
        runTest {
            val engine =
                MockEngine { _ ->
                    respondError(HttpStatusCode.InternalServerError)
                }
            val transport = makeTransport(engine)
            val result = transport.request("ledger", JsonObject(emptyMap()), JsonObject.serializer())

            result.shouldBeInstanceOf<XrplResult.Failure>()
            val failure = (result as XrplResult.Failure).error
            failure.shouldBeInstanceOf<XrplFailure.NetworkError>()
            (failure as XrplFailure.NetworkError).message shouldBe "HTTP 500: Internal Server Error"
        }
    }

    test("missing result field returns XrplResult.Failure with NetworkError") {
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content = ByteReadChannel("""{"no_result_here":true}"""),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }
            val transport = makeTransport(engine)
            val result = transport.request("ledger", JsonObject(emptyMap()), JsonObject.serializer())

            result.shouldBeInstanceOf<XrplResult.Failure>()
            val failure = (result as XrplResult.Failure).error
            failure.shouldBeInstanceOf<XrplFailure.NetworkError>()
            (failure as XrplFailure.NetworkError).message shouldBe "Missing 'result' field in response"
        }
    }

    test("RPC error status in result returns XrplResult.Failure with RpcError") {
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content =
                            ByteReadChannel(
                                """{"result":{"status":"error",""" +
                                    """"error":"actNotFound",""" +
                                    """"error_code":19,""" +
                                    """"error_message":"Account not found"}}""",
                            ),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }
            val transport = makeTransport(engine)
            val result = transport.request("account_info", JsonObject(emptyMap()), JsonObject.serializer())

            result.shouldBeInstanceOf<XrplResult.Failure>()
            val failure = (result as XrplResult.Failure).error
            failure.shouldBeInstanceOf<XrplFailure.RpcError>()
            val rpcError = failure as XrplFailure.RpcError
            rpcError.errorCode shouldBe 19
            rpcError.errorMessage shouldBe "Account not found"
        }
    }

    test("network exception is caught and returns XrplResult.Failure with NetworkError") {
        runTest {
            val engine =
                MockEngine { _ ->
                    throw RuntimeException("Connection refused")
                }
            val transport = makeTransport(engine)
            val result = transport.request("ledger", JsonObject(emptyMap()), JsonObject.serializer())

            result.shouldBeInstanceOf<XrplResult.Failure>()
            val failure = (result as XrplResult.Failure).error
            failure.shouldBeInstanceOf<XrplFailure.NetworkError>()
        }
    }

    test("CancellationException is rethrown and not caught as NetworkError") {
        runTest {
            val engine =
                MockEngine { _ ->
                    throw kotlinx.coroutines.CancellationException("cancelled")
                }
            val transport = makeTransport(engine)
            var threw = false
            try {
                transport.request("ledger", JsonObject(emptyMap()), JsonObject.serializer())
            } catch (e: kotlinx.coroutines.CancellationException) {
                threw = true
            }
            threw shouldBe true
        }
    }
})
