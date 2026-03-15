@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.LedgerClosedResult
import org.xrpl.sdk.client.model.LedgerEntryResult
import org.xrpl.sdk.client.model.LedgerResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.LedgerIndex

class LedgerMethodsTest : FunSpec({

    // ── ledger ───────────────────────────────────────────────────

    test("ledger returns LedgerResult with ledger info") {
        runTest {
            val ledgerHash = "4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651"
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"ledger":{"ledger_hash":"$ledgerHash","ledger_index":1000,""" +
                            """"account_hash":"AA126EAF5E3A8EA","transaction_hash":"BB2634A0",""" +
                            """"close_time":1000000,"close_time_human":"2024-Jan-01",""" +
                            """"total_coins":"99999000000000000","closed":true},""" +
                            """"ledger_hash":"$ledgerHash","ledger_index":1000,"validated":true""",
                    ),
                )
            client.use { c ->
                val result = c.ledger()
                result.shouldBeInstanceOf<XrplResult.Success<LedgerResult>>()
                val ledgerResult = (result as XrplResult.Success<LedgerResult>).value
                ledgerResult.validated shouldBe true
                ledgerResult.ledgerIndex?.value shouldBe 1000u
                ledgerResult.ledgerHash shouldNotBe null
                ledgerResult.ledgerHash!!.value shouldBe ledgerHash

                val ledger = ledgerResult.ledger
                ledger shouldNotBe null
                ledger!!.ledgerIndex?.value shouldBe 1000u
                ledger.closed shouldBe true
                ledger.closeTimeHuman shouldBe "2024-Jan-01"
                ledger.totalCoins?.value shouldBe 99_999_000_000_000_000L
            }
        }
    }

    test("ledger with invalid hash length does not set ledgerHash") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"ledger":{"closed":false},"ledger_hash":"TOOSHORT","ledger_index":1,"validated":false""",
                    ),
                )
            client.use { c ->
                val result = c.ledger()
                result.shouldBeInstanceOf<XrplResult.Success<LedgerResult>>()
                val ledgerResult = (result as XrplResult.Success<LedgerResult>).value
                ledgerResult.ledgerHash shouldBe null
            }
        }
    }

    // ── ledgerClosed ─────────────────────────────────────────────

    test("ledgerClosed returns hash and index") {
        runTest {
            val expectedHash = "4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651"
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"ledger_hash":"$expectedHash","ledger_index":86999999""",
                    ),
                )
            client.use { c ->
                val result = c.ledgerClosed()
                result.shouldBeInstanceOf<XrplResult.Success<LedgerClosedResult>>()
                val closed = (result as XrplResult.Success<LedgerClosedResult>).value
                closed.ledgerHash.value shouldBe expectedHash
                closed.ledgerIndex shouldBe LedgerIndex(86_999_999u)
            }
        }
    }

    // ── ledgerCurrent ────────────────────────────────────────────

    test("ledgerCurrent returns current ledger index") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""ledger_current_index":87000001"""),
                )
            client.use { c ->
                val result = c.ledgerCurrent()
                result.shouldBeInstanceOf<XrplResult.Success<LedgerIndex>>()
                (result as XrplResult.Success<LedgerIndex>).value shouldBe LedgerIndex(87_000_001u)
            }
        }
    }

    // ── ledgerEntry ───────────────────────────────────────────────

    test("ledgerEntry returns entry with node and index") {
        runTest {
            val entryIndex = "7DB0788C020F02780A673DC74757F23823FA3014C1866E72CC4CD8B226CD6EF4"
            val ledgerHash = "4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651"
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"index":"$entryIndex",""" +
                            """"ledger_index":87000000,""" +
                            """"ledger_hash":"$ledgerHash",""" +
                            """"node":{"LedgerEntryType":"AccountRoot",""" +
                            """"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"Balance":"100000000"},""" +
                            """"validated":true""",
                    ),
                )
            client.use { c ->
                val result = c.ledgerEntry(accountRoot = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
                result.shouldBeInstanceOf<XrplResult.Success<LedgerEntryResult>>()
                val entry = (result as XrplResult.Success<LedgerEntryResult>).value
                entry.index shouldBe entryIndex
                entry.ledgerIndex?.value shouldBe 87_000_000u
                entry.ledgerHash shouldNotBe null
                entry.ledgerHash!!.value shouldBe ledgerHash
                entry.node shouldNotBe null
                entry.nodeBinary shouldBe null
                entry.validated shouldBe true
            }
        }
    }

    test("ledgerEntry with binary returns nodeBinary string") {
        runTest {
            val entryIndex = "7DB0788C020F02780A673DC74757F23823FA3014C1866E72CC4CD8B226CD6EF4"
            val binaryData = "1100612200000000"
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"index":"$entryIndex",""" +
                            """"ledger_index":1000,""" +
                            """"node_binary":"$binaryData",""" +
                            """"validated":true""",
                    ),
                )
            client.use { c ->
                val result = c.ledgerEntry(binary = true, accountRoot = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
                result.shouldBeInstanceOf<XrplResult.Success<LedgerEntryResult>>()
                val entry = (result as XrplResult.Success<LedgerEntryResult>).value
                entry.index shouldBe entryIndex
                entry.nodeBinary shouldBe binaryData
                entry.node shouldBe null
                entry.validated shouldBe true
            }
        }
    }
})
