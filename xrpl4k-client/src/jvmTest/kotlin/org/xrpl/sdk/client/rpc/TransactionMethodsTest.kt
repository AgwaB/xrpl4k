@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.errorResponse
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.SimulateResult
import org.xrpl.sdk.client.model.SubmitResult
import org.xrpl.sdk.client.model.TransactionResult
import org.xrpl.sdk.client.model.ValidatedTransaction
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

private val SAMPLE_TX_HASH = TxHash("E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879")
private val SAMPLE_LEDGER_HASH = "4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651"

/** Minimal stub signed transaction for use in submit/simulate calls. */
private fun stubSignedTx(): XrplTransaction.Signed =
    XrplTransaction.Signed.create(
        transactionType = TransactionType.Payment,
        txBlob = "1200002200000000240000000161D4838D7EA4C6000000000000000000000000000055534400000000",
        hash = SAMPLE_TX_HASH,
    )

@OptIn(ExperimentalXrplApi::class)
class TransactionMethodsTest : FunSpec({

    // ── submit ───────────────────────────────────────────────────

    test("submit returns SubmitResult with tesSUCCESS engine result") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"engine_result":"tesSUCCESS","engine_result_code":0,""" +
                            """"engine_result_message":"The transaction was applied.",""" +
                            """"accepted":true,"applied":true,"broadcast":true,""" +
                            """"kept":false,"queued":false,""" +
                            """"tx_json":{"hash":"${SAMPLE_TX_HASH.value}","TransactionType":"Payment"}""",
                    ),
                )
            client.use { c ->
                val result = c.submit(stubSignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<SubmitResult>>()
                val submit = (result as XrplResult.Success<SubmitResult>).value
                submit.engineResult shouldBe "tesSUCCESS"
                submit.engineResultCode shouldBe 0
                submit.engineResultMessage shouldBe "The transaction was applied."
                submit.accepted shouldBe true
                submit.applied shouldBe true
                submit.broadcast shouldBe true
                submit.kept shouldBe false
                submit.queued shouldBe false
                submit.txHash shouldNotBe null
                submit.txHash!!.value shouldBe SAMPLE_TX_HASH.value
            }
        }
    }

    test("submit with queued transaction returns queued flag true") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"engine_result":"terQUEUED","engine_result_code":-89,""" +
                            """"engine_result_message":"The transaction was queued.",""" +
                            """"accepted":true,"applied":false,"broadcast":false,""" +
                            """"kept":false,"queued":true""",
                    ),
                )
            client.use { c ->
                val result = c.submit(stubSignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<SubmitResult>>()
                val submit = (result as XrplResult.Success<SubmitResult>).value
                submit.engineResult shouldBe "terQUEUED"
                submit.queued shouldBe true
                submit.applied shouldBe false
            }
        }
    }

    test("submit with open_ledger_cost maps to XrpDrops") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"engine_result":"tesSUCCESS","engine_result_code":0,""" +
                            """"engine_result_message":"The transaction was applied.",""" +
                            """"accepted":true,"applied":true,"broadcast":true,""" +
                            """"kept":false,"queued":false,""" +
                            """"open_ledger_cost":"15",""" +
                            """"validated_ledger_index":87000000""",
                    ),
                )
            client.use { c ->
                val result = c.submit(stubSignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<SubmitResult>>()
                val submit = (result as XrplResult.Success<SubmitResult>).value
                submit.openLedgerCost shouldBe XrpDrops(15)
                submit.validatedLedgerIndex?.value shouldBe 87_000_000u
            }
        }
    }

    // ── tx ───────────────────────────────────────────────────────

    test("tx returns TransactionResult with validated flag and hash") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"hash":"${SAMPLE_TX_HASH.value}","ledger_index":87000000,""" +
                            """"validated":true,""" +
                            """"close_time_iso":"2024-01-01T00:00:00Z",""" +
                            """"tx_json":{"TransactionType":"Payment",""" +
                            """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"},""" +
                            """"meta":{"TransactionResult":"tesSUCCESS"}""",
                    ),
                )
            client.use { c ->
                val result = c.tx(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Success<TransactionResult>>()
                val tx = (result as XrplResult.Success<TransactionResult>).value
                tx.hash shouldNotBe null
                tx.hash!!.value shouldBe SAMPLE_TX_HASH.value
                tx.validated shouldBe true
                tx.ledgerIndex?.value shouldBe 87_000_000u
                tx.closeTimeIso shouldBe "2024-01-01T00:00:00Z"
                tx.engineResult shouldBe "tesSUCCESS"
                tx.txJson shouldNotBe null
                tx.meta shouldNotBe null
            }
        }
    }

    test("tx with top-level engine_result field maps engineResult directly") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"hash":"${SAMPLE_TX_HASH.value}","ledger_index":1000,""" +
                            """"validated":true,""" +
                            """"engine_result":"tesSUCCESS","engine_result_code":0,""" +
                            """"tx_json":{"TransactionType":"Payment"}""",
                    ),
                )
            client.use { c ->
                val result = c.tx(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Success<TransactionResult>>()
                val tx = (result as XrplResult.Success<TransactionResult>).value
                tx.engineResult shouldBe "tesSUCCESS"
                tx.engineResultCode shouldBe 0
            }
        }
    }

    test("tx with hash shorter than 64 chars sets hash to null") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""hash":"TOOSHORT","validated":false"""),
                )
            client.use { c ->
                val result = c.tx(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Success<TransactionResult>>()
                val tx = (result as XrplResult.Success<TransactionResult>).value
                tx.hash shouldBe null
                tx.validated shouldBe false
            }
        }
    }

    test("tx with txnNotFound error returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("txnNotFound", 29, "Transaction not found."))
            client.use { c ->
                val result = c.tx(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    // ── transactionEntry ─────────────────────────────────────────

    test("transactionEntry returns ValidatedTransaction with ledger info") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"tx_json":{"TransactionType":"Payment",""" +
                            """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"hash":"${SAMPLE_TX_HASH.value}"},""" +
                            """"metadata":{"TransactionResult":"tesSUCCESS"},""" +
                            """"ledger_index":87000000,""" +
                            """"ledger_hash":"$SAMPLE_LEDGER_HASH"""",
                    ),
                )
            client.use { c ->
                val result = c.transactionEntry(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Success<ValidatedTransaction>>()
                val vtx = (result as XrplResult.Success<ValidatedTransaction>).value
                vtx.ledgerIndex?.value shouldBe 87_000_000u
                vtx.ledgerHash shouldNotBe null
                vtx.ledgerHash!!.value shouldBe SAMPLE_LEDGER_HASH
                vtx.txJson shouldNotBe null
                vtx.metadata shouldNotBe null
                vtx.hash shouldNotBe null
                vtx.hash!!.value shouldBe SAMPLE_TX_HASH.value
                vtx.engineResult shouldBe "tesSUCCESS"
                vtx.meta shouldNotBe null
            }
        }
    }

    test("transactionEntry with short ledger hash sets ledgerHash to null") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"tx_json":{"TransactionType":"Payment"},""" +
                            """"ledger_index":1000,""" +
                            """"ledger_hash":"TOOSHORT"""",
                    ),
                )
            client.use { c ->
                val result = c.transactionEntry(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Success<ValidatedTransaction>>()
                val vtx = (result as XrplResult.Success<ValidatedTransaction>).value
                vtx.ledgerHash shouldBe null
                vtx.ledgerIndex?.value shouldBe 1000u
            }
        }
    }

    test("transactionEntry with txnNotFound error returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("txnNotFound", 29, "Transaction not found."))
            client.use { c ->
                val result = c.transactionEntry(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    test("transactionEntry with lgrNotFound error returns Failure(NotFound)") {
        runTest {
            val client = clientWithMockEngine(errorResponse("lgrNotFound", 21, "Ledger not found."))
            client.use { c ->
                val result = c.transactionEntry(SAMPLE_TX_HASH)
                result.shouldBeInstanceOf<XrplResult.Failure>()
                (result as XrplResult.Failure).error shouldBe XrplFailure.NotFound
            }
        }
    }

    // ── simulate ─────────────────────────────────────────────────

    test("simulate returns SimulateResult with tesSUCCESS engine result") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"engine_result":"tesSUCCESS","engine_result_code":0,""" +
                            """"engine_result_message":"The transaction would succeed.",""" +
                            """"tx_json":{"TransactionType":"Payment","Fee":"12"},""" +
                            """"meta":{"TransactionResult":"tesSUCCESS"}""",
                    ),
                )
            client.use { c ->
                val result = c.simulate(stubSignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<SimulateResult>>()
                val sim = (result as XrplResult.Success<SimulateResult>).value
                sim.engineResult shouldBe "tesSUCCESS"
                sim.engineResultCode shouldBe 0
                sim.engineResultMessage shouldBe "The transaction would succeed."
                sim.txJson shouldNotBe null
                sim.meta shouldNotBe null
            }
        }
    }

    test("simulate with tec error returns SimulateResult with tec engine result") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"engine_result":"tecUNFUNDED_PAYMENT","engine_result_code":104,""" +
                            """"engine_result_message":"Insufficient XRP balance to send.",""" +
                            """"tx_json":{"TransactionType":"Payment"}""",
                    ),
                )
            client.use { c ->
                val result = c.simulate(stubSignedTx())
                result.shouldBeInstanceOf<XrplResult.Success<SimulateResult>>()
                val sim = (result as XrplResult.Success<SimulateResult>).value
                sim.engineResult shouldBe "tecUNFUNDED_PAYMENT"
                sim.engineResultCode shouldBe 104
            }
        }
    }
})
