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
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.errorResponse
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.XrpDrops

private val TEST_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

class ConvenienceTest : FunSpec({

    // ── getXrpBalance ────────────────────────────────────────────

    test("getXrpBalance returns XRP balance from accountInfo") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"Balance":"25000000","Sequence":1,"OwnerCount":0,"Flags":0},""" +
                            """"validated":true""",
                    ),
                )
            client.use { c ->
                val result = c.getXrpBalance(TEST_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<XrpDrops>>()
                (result as XrplResult.Success<XrpDrops>).value shouldBe XrpDrops(25_000_000)
            }
        }
    }

    test("getXrpBalance with actNotFound error returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("actNotFound", 19, "Account not found."))
            client.use { c ->
                val result = c.getXrpBalance(TEST_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    // ── getBalances ──────────────────────────────────────────────

    test("getBalances returns XRP balance when no trust lines exist") {
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
                                            """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"Balance":"10000000","Sequence":1,""" +
                                            """"OwnerCount":0,"Flags":0},"validated":true}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                        else ->
                            respond(
                                content =
                                    io.ktor.utils.io.ByteReadChannel(
                                        """{"result":{"status":"success",""" +
                                            """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","lines":[]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                    }
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.getBalances(TEST_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<List<Balance>>>()
                val balances = (result as XrplResult.Success<List<Balance>>).value
                balances shouldHaveSize 1
                val xrp = balances.first()
                xrp.currency shouldBe "XRP"
                xrp.value shouldBe "10"
                xrp.issuer shouldBe null
            }
        }
    }

    test("getBalances returns XRP plus issued currency balances") {
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
                                            """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                                            """"Balance":"5000000","Sequence":3,""" +
                                            """"OwnerCount":1,"Flags":0},"validated":true}}""",
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
                                            """"lines":[{"account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                                            """"balance":"100","currency":"USD",""" +
                                            """"limit":"1000","limit_peer":"0"}]}}""",
                                    ),
                                status = HttpStatusCode.OK,
                                headers = TestHelper.jsonHeaders(),
                            )
                    }
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.getBalances(TEST_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Success<List<Balance>>>()
                val balances = (result as XrplResult.Success<List<Balance>>).value
                balances shouldHaveSize 2
                val xrp = balances[0]
                xrp.currency shouldBe "XRP"
                xrp.value shouldBe "5"
                val usd = balances[1]
                usd.currency shouldBe "USD"
                usd.value shouldBe "100"
                usd.issuer shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
            }
        }
    }

    test("getBalances propagates accountInfo failure") {
        runTest {
            val client = clientWithMockEngine(errorResponse("actNotFound", 19, "Account not found."))
            client.use { c ->
                val result = c.getBalances(TEST_ADDRESS)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    // ── getLedgerIndex ───────────────────────────────────────────

    test("getLedgerIndex returns current ledger index") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""ledger_current_index":87000042"""),
                )
            client.use { c ->
                val result = c.getLedgerIndex()
                result.shouldBeInstanceOf<XrplResult.Success<LedgerIndex>>()
                (result as XrplResult.Success<LedgerIndex>).value shouldBe LedgerIndex(87_000_042u)
            }
        }
    }
})
