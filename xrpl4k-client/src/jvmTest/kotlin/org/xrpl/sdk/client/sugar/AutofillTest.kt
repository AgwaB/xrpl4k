@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.sugar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops

private val TEST_ACCOUNT = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
private val TEST_DESTINATION = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")

/** Creates a minimal unsigned Payment transaction for autofill tests. */
private fun stubUnsignedTx(flags: UInt? = null): XrplTransaction.Unsigned =
    XrplTransaction.Unsigned(
        transactionType = TransactionType.Payment,
        account = TEST_ACCOUNT,
        fields =
            PaymentFields(
                destination = TEST_DESTINATION,
                amount = XrpAmount(XrpDrops(1_000_000)),
            ),
        flags = flags,
    )

/**
 * Creates a MockEngine that dispatches different responses based on the RPC method
 * in the request body. Autofill calls three RPCs concurrently: fee, account_info,
 * and ledger_current.
 *
 * @param openLedgerFee the open_ledger_fee drops value for the fee response
 * @param accountSequence the account sequence for the account_info response
 * @param currentLedgerIndex the current ledger index for the ledger_current response
 */
private fun autofillMockEngine(
    openLedgerFee: Long = 10,
    accountSequence: UInt = 5u,
    currentLedgerIndex: UInt = 87_000_000u,
): MockEngine =
    MockEngine { request ->
        val body = String(request.body.toByteArray())
        val response =
            when {
                """"method":"fee"""" in body ->
                    """
                    {"result":{"status":"success",
                    "drops":{"base_fee":"10","median_fee":"5000",
                    "minimum_fee":"10","open_ledger_fee":"$openLedgerFee"},
                    "current_ledger_size":"42","current_queue_size":"0",
                    "expected_ledger_size":"100","max_queue_size":"2000",
                    "ledger_current_index":$currentLedgerIndex}}
                    """.trimIndent()

                """"method":"account_info"""" in body ->
                    """
                    {"result":{"status":"success",
                    "account_data":{"Account":"${TEST_ACCOUNT.value}",
                    "Balance":"50000000","Sequence":$accountSequence,
                    "OwnerCount":0,"Flags":0},"validated":true}}
                    """.trimIndent()

                """"method":"ledger_current"""" in body ->
                    """
                    {"result":{"status":"success",
                    "ledger_current_index":$currentLedgerIndex}}
                    """.trimIndent()

                else ->
                    """{"result":{"status":"error",""" +
                        """"error":"unknownCmd","error_code":-1,""" +
                        """"error_message":"Unknown method."}}"""
            }
        respond(
            content = ByteReadChannel(response),
            status = HttpStatusCode.OK,
            headers = TestHelper.jsonHeaders(),
        )
    }

class AutofillTest : FunSpec({

    // ── basic autofill ──────────────────────────────────────────

    test("autofill fills fee, sequence, and lastLedgerSequence") {
        runTest {
            val engine =
                autofillMockEngine(
                    openLedgerFee = 10,
                    accountSequence = 5u,
                    currentLedgerIndex = 87_000_000u,
                )
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<XrplTransaction.Filled>>()
                val filled = (result as XrplResult.Success<XrplTransaction.Filled>).value

                // fee = openLedgerFee(10) * feeCushion(1.2) = 12
                filled.fee shouldBe XrpDrops(12)
                filled.sequence shouldBe 5u
                // lastLedgerSequence = currentLedger + 20
                filled.lastLedgerSequence shouldBe (87_000_000u + 20u)
                filled.transactionType shouldBe TransactionType.Payment
                filled.account shouldBe TEST_ACCOUNT
            }
        }
    }

    // ── multisig fee multiplier ─────────────────────────────────

    test("autofill with multisigSigners=2 multiplies fee by 3") {
        runTest {
            val engine = autofillMockEngine(openLedgerFee = 10)
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx(), multisigSigners = 2)
                result.shouldBeInstanceOf<XrplResult.Success<XrplTransaction.Filled>>()
                val filled = (result as XrplResult.Success<XrplTransaction.Filled>).value

                // fee = openLedgerFee(10) * feeCushion(1.2) * (1 + 2) = 12 * 3 = 36
                filled.fee shouldBe XrpDrops(36)
            }
        }
    }

    // ── ticket sequence ─────────────────────────────────────────

    test("autofill with ticketSequence sets sequence=0 and ticketSequence") {
        runTest {
            val engine = autofillMockEngine()
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx(), ticketSequence = 42u)
                result.shouldBeInstanceOf<XrplResult.Success<XrplTransaction.Filled>>()
                val filled = (result as XrplResult.Success<XrplTransaction.Filled>).value

                filled.sequence shouldBe 0u
                filled.ticketSequence shouldBe 42u
            }
        }
    }

    test("autofill with ticketSequence skips account_info call") {
        runTest {
            val requestMethods = mutableListOf<String>()
            val engine =
                MockEngine { request ->
                    val body = String(request.body.toByteArray())
                    val method =
                        when {
                            """"method":"fee"""" in body -> "fee"
                            """"method":"account_info"""" in body -> "account_info"
                            """"method":"ledger_current"""" in body -> "ledger_current"
                            else -> "unknown"
                        }
                    requestMethods.add(method)

                    val response =
                        when (method) {
                            "fee" ->
                                """
                                {"result":{"status":"success",
                                "drops":{"base_fee":"10","median_fee":"5000",
                                "minimum_fee":"10","open_ledger_fee":"10"},
                                "current_ledger_size":"42","current_queue_size":"0",
                                "expected_ledger_size":"100","max_queue_size":"2000",
                                "ledger_current_index":87000000}}
                                """.trimIndent()

                            "ledger_current" ->
                                """
                                {"result":{"status":"success","ledger_current_index":87000000}}
                                """.trimIndent()

                            else ->
                                """{"result":{"status":"error",""" +
                                    """"error":"unknownCmd","error_code":-1,""" +
                                    """"error_message":"Unknown method."}}"""
                        }
                    respond(
                        content = ByteReadChannel(response),
                        status = HttpStatusCode.OK,
                        headers = TestHelper.jsonHeaders(),
                    )
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx(), ticketSequence = 42u)
                result.shouldBeInstanceOf<XrplResult.Success<XrplTransaction.Filled>>()

                // account_info should NOT be among the requested methods
                requestMethods.sorted() shouldBe listOf("fee", "ledger_current")
            }
        }
    }

    // ── flags preserved ─────────────────────────────────────────

    test("autofill preserves flags from Unsigned to Filled") {
        runTest {
            val flags = 0x00010000u // tfPartialPayment
            val engine = autofillMockEngine()
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx(flags = flags))
                result.shouldBeInstanceOf<XrplResult.Success<XrplTransaction.Filled>>()
                val filled = (result as XrplResult.Success<XrplTransaction.Filled>).value

                filled.flags shouldBe flags
            }
        }
    }

    // ── fee cushion ─────────────────────────────────────────────

    test("autofill applies custom fee cushion") {
        runTest {
            val engine = autofillMockEngine(openLedgerFee = 100)
            val client =
                XrplClient {
                    this.engine = engine
                    feeCushion = 1.5
                }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<XrplTransaction.Filled>>()
                val filled = (result as XrplResult.Success<XrplTransaction.Filled>).value

                // fee = openLedgerFee(100) * feeCushion(1.5) = 150
                filled.fee shouldBe XrpDrops(150)
            }
        }
    }

    // ── fee exceeds maxFeeXrp ───────────────────────────────────

    test("autofill fails when calculated fee exceeds maxFeeXrp") {
        runTest {
            // openLedgerFee = 3_000_000 drops, cushion = 1.2 -> 3_600_000 drops = 3.6 XRP
            // default maxFeeXrp = 2.0 XRP = 2_000_000 drops -> should fail
            val engine = autofillMockEngine(openLedgerFee = 3_000_000)
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx())
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val failure = (result as XrplResult.Failure).error
                failure.shouldBeInstanceOf<XrplFailure.ValidationError>()
            }
        }
    }

    // ── fee RPC failure propagation ─────────────────────────────

    test("autofill propagates fee RPC failure") {
        runTest {
            val engine =
                MockEngine { request ->
                    val body = String(request.body.toByteArray())
                    val response =
                        when {
                            """"method":"fee"""" in body ->
                                """{"result":{"status":"error",""" +
                                    """"error":"noNetwork","error_code":17,""" +
                                    """"error_message":"Not synced to network."}}"""

                            """"method":"account_info"""" in body ->
                                """
                                {"result":{"status":"success",
                                "account_data":{"Account":"${TEST_ACCOUNT.value}",
                                "Balance":"50000000","Sequence":5,
                                "OwnerCount":0,"Flags":0},"validated":true}}
                                """.trimIndent()

                            """"method":"ledger_current"""" in body ->
                                """
                                {"result":{"status":"success","ledger_current_index":87000000}}
                                """.trimIndent()

                            else ->
                                """{"result":{"status":"error",""" +
                                    """"error":"unknownCmd","error_code":-1,""" +
                                    """"error_message":"Unknown."}}"""
                        }
                    respond(
                        content = ByteReadChannel(response),
                        status = HttpStatusCode.OK,
                        headers = TestHelper.jsonHeaders(),
                    )
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx())
                result.shouldBeInstanceOf<XrplResult.Failure>()
            }
        }
    }

    // ── account_info failure propagation ─────────────────────────

    test("autofill propagates account_info failure") {
        runTest {
            val engine =
                MockEngine { request ->
                    val body = String(request.body.toByteArray())
                    val response =
                        when {
                            """"method":"fee"""" in body ->
                                """
                                {"result":{"status":"success",
                                "drops":{"base_fee":"10","median_fee":"5000",
                                "minimum_fee":"10","open_ledger_fee":"10"},
                                "current_ledger_size":"42","current_queue_size":"0",
                                "expected_ledger_size":"100","max_queue_size":"2000",
                                "ledger_current_index":87000000}}
                                """.trimIndent()

                            """"method":"account_info"""" in body ->
                                """{"result":{"status":"error",""" +
                                    """"error":"actNotFound",""" +
                                    """"error_code":19,""" +
                                    """"error_message":"Account not found."}}"""

                            """"method":"ledger_current"""" in body ->
                                """
                                {"result":{"status":"success",
                                "ledger_current_index":87000000}}
                                """.trimIndent()

                            else ->
                                """{"result":{"status":"error",""" +
                                    """"error":"unknownCmd","error_code":-1,""" +
                                    """"error_message":"Unknown."}}"""
                        }
                    respond(
                        content = ByteReadChannel(response),
                        status = HttpStatusCode.OK,
                        headers = TestHelper.jsonHeaders(),
                    )
                }
            val client = XrplClient { this.engine = engine }
            client.use { c ->
                val result = c.autofill(stubUnsignedTx())
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }
})
