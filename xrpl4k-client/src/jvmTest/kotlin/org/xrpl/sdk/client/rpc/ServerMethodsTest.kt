@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.FeeResult
import org.xrpl.sdk.client.model.ServerInfo
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.XrpDrops

class ServerMethodsTest : FunSpec({

    // ── fee ──────────────────────────────────────────────────────

    test("fee returns FeeResult with correct fee drops") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"current_ledger_size":"56","current_queue_size":"0",""" +
                            """"drops":{"base_fee":"10","median_fee":"11000",""" +
                            """"minimum_fee":"10","open_ledger_fee":"10"},""" +
                            """"expected_ledger_size":"55","ledger_current_index":100,""" +
                            """"max_queue_size":"1120"""",
                    ),
                )
            client.use { c ->
                val result = c.fee()
                result.shouldBeInstanceOf<XrplResult.Success<FeeResult>>()
                val fee = (result as XrplResult.Success<FeeResult>).value
                fee.drops.baseFee shouldBe XrpDrops(10)
                fee.drops.minimumFee shouldBe XrpDrops(10)
                fee.drops.medianFee shouldBe XrpDrops(11_000)
                fee.drops.openLedgerFee shouldBe XrpDrops(10)
                fee.openLedgerFee shouldBe XrpDrops(10)
                fee.currentLedgerSize shouldBe "56"
                fee.currentQueueSize shouldBe "0"
                fee.ledgerCurrentIndex?.value shouldBe 100u
                fee.maxQueueSize shouldBe "1120"
            }
        }
    }

    test("fee with missing drops returns FeeResult with null fee fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""current_ledger_size":"0","current_queue_size":"0""""),
                )
            client.use { c ->
                val result = c.fee()
                result.shouldBeInstanceOf<XrplResult.Success<FeeResult>>()
                val fee = (result as XrplResult.Success<FeeResult>).value
                fee.drops.baseFee shouldBe null
                fee.openLedgerFee shouldBe XrpDrops(0)
            }
        }
    }

    // ── serverInfo ───────────────────────────────────────────────

    test("serverInfo returns ServerInfo with build version and state") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"info":{"build_version":"1.12.0",""" +
                            """"complete_ledgers":"32570-87000000","hostid":"XRPL",""" +
                            """"io_latency_ms":1,"server_state":"full","uptime":12345,""" +
                            """"peers":10,"load_factor":1.0,"validation_quorum":4}""",
                    ),
                )
            client.use { c ->
                val result = c.serverInfo()
                result.shouldBeInstanceOf<XrplResult.Success<ServerInfo>>()
                val info = (result as XrplResult.Success<ServerInfo>).value
                info.buildVersion shouldBe "1.12.0"
                info.completeLedgers shouldBe "32570-87000000"
                info.serverState shouldBe "full"
                info.uptime shouldBe 12345L
                info.peers shouldBe 10
                info.validationQuorum shouldBe 4
            }
        }
    }

    test("serverInfo with validated ledger info maps ValidatedLedgerInfo") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"info":{"build_version":"1.12.0","server_state":"full",""" +
                            """"validated_ledger":{"age":3,"base_fee_xrp":0.00001,""" +
                            """"hash":"ABC123DEF456ABC123DEF456ABC123DEF456ABC123DEF456ABC123DEF456ABC1",""" +
                            """"reserve_base_xrp":10.0,"reserve_inc_xrp":2.0,"seq":87000000}}""",
                    ),
                )
            client.use { c ->
                val result = c.serverInfo()
                result.shouldBeInstanceOf<XrplResult.Success<ServerInfo>>()
                val info = (result as XrplResult.Success<ServerInfo>).value
                info.validatedLedger shouldNotBe null
                info.validatedLedger!!.seq?.value shouldBe 87_000_000u
                info.validatedLedger!!.baseFeeXrp shouldBe 0.00001
                info.validatedLedger!!.reserveBaseXrp shouldBe 10.0
                info.validatedLedger!!.age shouldBe 3L
            }
        }
    }

    test("serverInfo with null info returns ServerInfo with all null fields") {
        runTest {
            val client = clientWithMockEngine(successResponse(""""info":null"""))
            client.use { c ->
                val result = c.serverInfo()
                result.shouldBeInstanceOf<XrplResult.Success<ServerInfo>>()
                val info = (result as XrplResult.Success<ServerInfo>).value
                info.buildVersion shouldBe null
                info.serverState shouldBe null
            }
        }
    }

    // ── version ──────────────────────────────────────────────────

    test("version returns version string") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""version":{"first":"1.12.0","good":"1.12.0","last":"1.12.0"}"""),
                )
            client.use { c ->
                val result = c.version()
                result.shouldBeInstanceOf<XrplResult.Success<String?>>()
                (result as XrplResult.Success<String?>).value shouldBe "1.12.0"
            }
        }
    }

    test("version with missing version field returns null") {
        runTest {
            val client = clientWithMockEngine(successResponse(""""version":null"""))
            client.use { c ->
                val result = c.version()
                result.shouldBeInstanceOf<XrplResult.Success<String?>>()
                (result as XrplResult.Success<String?>).value shouldBe null
            }
        }
    }
})
