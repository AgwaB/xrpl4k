@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.sugar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.model.amount.CurrencySpec
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

class GetOrderbookTest : FunSpec({

    // ── getOrderbook ──────────────────────────────────────────────

    test("getOrderbook returns OrderBook with buy and sell lists") {
        runTest {
            var requestCount = 0
            val engine =
                MockEngine { _ ->
                    requestCount++
                    when (requestCount) {
                        // First call: sell side (takerGets -> takerPays)
                        1 ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"offers":[{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"taker_gets":"1000000","taker_pays":"500000",""" +
                                            """"quality":"0.5","Flags":0,"Sequence":10}]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                        // Second call: buy side (takerPays -> takerGets, reversed)
                        else ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"offers":[{"Account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                                            """"taker_gets":"800000","taker_pays":"400000",""" +
                                            """"quality":"0.5","Flags":0,"Sequence":20}]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                    }
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result =
                    c.getOrderbook(
                        takerGets = CurrencySpec.Xrp,
                        takerPays = CurrencySpec.Xrp,
                    )
                result.shouldBeInstanceOf<XrplResult.Success<OrderBook>>()
                val book = (result as XrplResult.Success<OrderBook>).value

                book.sell shouldHaveSize 1
                book.sell.first().account shouldBe Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
                book.sell.first().sequence shouldBe 10L

                book.buy shouldHaveSize 1
                book.buy.first().account shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
                book.buy.first().sequence shouldBe 20L

                requestCount shouldBe 2
            }
        }
    }

    test("getOrderbook returns empty order book when no offers on either side") {
        runTest {
            var requestCount = 0
            val engine =
                MockEngine { _ ->
                    requestCount++
                    respond(
                        content =
                            io.ktor.utils.io.ByteReadChannel(
                                """{"result":{"status":"success","offers":[]}}""",
                            ),
                        status = HttpStatusCode.OK,
                        headers = TestHelper.jsonHeaders(),
                    )
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result =
                    c.getOrderbook(
                        takerGets = CurrencySpec.Xrp,
                        takerPays = CurrencySpec.Xrp,
                    )
                result.shouldBeInstanceOf<XrplResult.Success<OrderBook>>()
                val book = (result as XrplResult.Success<OrderBook>).value

                book.sell shouldHaveSize 0
                book.buy shouldHaveSize 0
                requestCount shouldBe 2
            }
        }
    }
})
