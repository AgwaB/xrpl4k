@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.type.Address

private val TEST_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

class PaginatedMethodsTest : FunSpec({

    // ── allAccountObjects ─────────────────────────────────────────

    test("allAccountObjects collects all pages with marker") {
        runTest {
            var requestCount = 0
            val engine =
                MockEngine { _ ->
                    requestCount++
                    when (requestCount) {
                        1 ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"account_objects":[{"LedgerEntryType":"Offer","index":"AAA"}],""" +
                                            """"marker":"page2"}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                        else ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"account_objects":[{"LedgerEntryType":"Escrow","index":"BBB"}]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                    }
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val objects = c.allAccountObjects(TEST_ADDRESS).toList()
                objects shouldHaveSize 2
                requestCount shouldBe 2
            }
        }
    }

    test("allAccountObjects single page with no marker returns all items") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"account_objects":[{"LedgerEntryType":"Offer","index":"AAA"},""" +
                            """{"LedgerEntryType":"Offer","index":"BBB"}]""",
                    ),
                )
            client.use { c ->
                val objects = c.allAccountObjects(TEST_ADDRESS).toList()
                objects shouldHaveSize 2
            }
        }
    }

    // ── allAccountOffers ──────────────────────────────────────────

    test("allAccountOffers collects offers across pages") {
        runTest {
            var requestCount = 0
            val engine =
                MockEngine { _ ->
                    requestCount++
                    when (requestCount) {
                        1 ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"offers":[{"flags":0,"seq":1,""" +
                                            """"taker_gets":"1000000","taker_pays":"500000"}],""" +
                                            """"marker":"next"}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                        else ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"offers":[{"flags":0,"seq":2,""" +
                                            """"taker_gets":"2000000","taker_pays":"800000"}]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                    }
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val offers = c.allAccountOffers(TEST_ADDRESS).toList()
                offers shouldHaveSize 2
                offers[0].seq shouldBe 1u
                offers[1].seq shouldBe 2u
                requestCount shouldBe 2
            }
        }
    }

    // ── allAccountChannels ────────────────────────────────────────

    test("allAccountChannels collects channels across pages") {
        runTest {
            var requestCount = 0
            val engine =
                MockEngine { _ ->
                    requestCount++
                    when (requestCount) {
                        1 ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"channels":[{"channel_id":"CH1",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                                            """"amount":"1000000","balance":"500000","settle_delay":86400}],""" +
                                            """"marker":"page2"}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                        else ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"channels":[{"channel_id":"CH2",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"destination_account":"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn",""" +
                                            """"amount":"2000000","balance":"1000000","settle_delay":3600}]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                    }
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val channels = c.allAccountChannels(TEST_ADDRESS).toList()
                channels shouldHaveSize 2
                channels[0].channelId shouldBe "CH1"
                channels[0].settleDelay shouldBe 86400u
                channels[1].channelId shouldBe "CH2"
                channels[1].destinationAccount shouldBe Address("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn")
                requestCount shouldBe 2
            }
        }
    }

    test("allAccountChannels single page returns all channels") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"channels":[{"channel_id":"ONLY",""" +
                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"amount":"5000000","balance":"0","settle_delay":600}]""",
                    ),
                )
            client.use { c ->
                val channels = c.allAccountChannels(TEST_ADDRESS).toList()
                channels shouldHaveSize 1
                channels.first().channelId shouldBe "ONLY"
            }
        }
    }
})
