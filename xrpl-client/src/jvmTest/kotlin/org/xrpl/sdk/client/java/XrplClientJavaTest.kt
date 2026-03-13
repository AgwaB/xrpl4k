@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.java

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.AccountInfo
import org.xrpl.sdk.client.model.AccountLinesResult
import org.xrpl.sdk.client.model.FeeResult
import org.xrpl.sdk.client.model.ServerInfo
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops
import java.util.concurrent.TimeUnit

private val GENESIS_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

private fun javaClientWithMockEngine(resultFields: String): XrplClientJava {
    val client = clientWithMockEngine(successResponse(resultFields))
    return XrplClientJava(client)
}

class XrplClientJavaTest : FunSpec({

    // ── create ───────────────────────────────────────────────────

    test("create with config DSL creates client successfully") {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = ByteReadChannel("""{"result":{"status":"success"}}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
            }
        val javaClient = XrplClientJava.create { engine = mockEngine }
        javaClient shouldNotBe null
        javaClient.close()
    }

    // ── accountInfo ──────────────────────────────────────────────

    test("accountInfo returns CompletableFuture that resolves") {
        val javaClient =
            javaClientWithMockEngine(
                """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"Balance":"100000000","Sequence":1,"OwnerCount":0,"Flags":0},""" +
                    """"ledger_index":1000,"validated":true""",
            )
        javaClient.use { c ->
            val future = c.accountInfo(GENESIS_ADDRESS)
            val result = future.get(10, TimeUnit.SECONDS)
            result.shouldBeInstanceOf<XrplResult.Success<AccountInfo>>()
            val info = (result as XrplResult.Success<AccountInfo>).value
            info.account shouldBe GENESIS_ADDRESS
            info.balance shouldBe XrpDrops(100_000_000)
            info.sequence shouldBe 1u
        }
    }

    // ── accountLines ─────────────────────────────────────────────

    test("accountLines returns CompletableFuture with AccountLinesResult") {
        val javaClient =
            javaClientWithMockEngine(
                """"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"lines":[{"account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                    """"balance":"10","currency":"USD","limit":"1000",""" +
                    """"limit_peer":"0","no_ripple":true,"no_ripple_peer":false}]""",
            )
        javaClient.use { c ->
            val result = c.accountLines(GENESIS_ADDRESS).get(10, TimeUnit.SECONDS)
            result.shouldBeInstanceOf<XrplResult.Success<AccountLinesResult>>()
            val lines = (result as XrplResult.Success<AccountLinesResult>).value
            lines.account shouldBe GENESIS_ADDRESS
            lines.lines.first().currency shouldBe "USD"
        }
    }

    // ── serverInfo ───────────────────────────────────────────────

    test("serverInfo returns CompletableFuture with ServerInfo") {
        val javaClient =
            javaClientWithMockEngine(
                """"info":{"build_version":"1.12.0",""" +
                    """"complete_ledgers":"32570-87000000","hostid":"XRPL",""" +
                    """"io_latency_ms":1,"server_state":"full","uptime":12345,""" +
                    """"peers":10,"load_factor":1.0,"validation_quorum":4}""",
            )
        javaClient.use { c ->
            val result = c.serverInfo().get(10, TimeUnit.SECONDS)
            result.shouldBeInstanceOf<XrplResult.Success<ServerInfo>>()
            val info = (result as XrplResult.Success<ServerInfo>).value
            info.buildVersion shouldBe "1.12.0"
            info.serverState shouldBe "full"
            info.peers shouldBe 10
        }
    }

    // ── fee ──────────────────────────────────────────────────────

    test("fee returns CompletableFuture with FeeResult") {
        val javaClient =
            javaClientWithMockEngine(
                """"current_ledger_size":"56","current_queue_size":"0",""" +
                    """"drops":{"base_fee":"10","median_fee":"11000",""" +
                    """"minimum_fee":"10","open_ledger_fee":"10"},""" +
                    """"expected_ledger_size":"55","ledger_current_index":100,""" +
                    """"max_queue_size":"1120"""",
            )
        javaClient.use { c ->
            val result = c.fee().get(10, TimeUnit.SECONDS)
            result.shouldBeInstanceOf<XrplResult.Success<FeeResult>>()
            val fee = (result as XrplResult.Success<FeeResult>).value
            fee.drops.baseFee shouldBe XrpDrops(10)
            fee.drops.minimumFee shouldBe XrpDrops(10)
            fee.currentLedgerSize shouldBe "56"
        }
    }

    // ── close ────────────────────────────────────────────────────

    test("close disposes resources without error") {
        val javaClient = javaClientWithMockEngine("")
        // Should complete without throwing
        javaClient.close()
    }

    // ── concurrency ──────────────────────────────────────────────

    test("multiple calls can run concurrently and both resolve") {
        val javaClient =
            javaClientWithMockEngine(
                """"info":{"build_version":"1.12.0","server_state":"full"}""",
            )
        javaClient.use { c ->
            val future1 = c.serverInfo()
            val future2 = c.serverInfo()
            val result1 = future1.get(10, TimeUnit.SECONDS)
            val result2 = future2.get(10, TimeUnit.SECONDS)
            result1.shouldBeInstanceOf<XrplResult.Success<ServerInfo>>()
            result2.shouldBeInstanceOf<XrplResult.Success<ServerInfo>>()
            (result1 as XrplResult.Success<ServerInfo>).value.buildVersion shouldBe "1.12.0"
            (result2 as XrplResult.Success<ServerInfo>).value.buildVersion shouldBe "1.12.0"
        }
    }

    // ── getXrpBalance ────────────────────────────────────────────

    test("getXrpBalance returns CompletableFuture with XrpDrops balance") {
        val javaClient =
            javaClientWithMockEngine(
                """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                    """"Balance":"200000000","Sequence":1,"OwnerCount":0,"Flags":0},""" +
                    """"ledger_index":2000,"validated":true""",
            )
        javaClient.use { c ->
            val result = c.getXrpBalance(GENESIS_ADDRESS).get(10, TimeUnit.SECONDS)
            result.shouldBeInstanceOf<XrplResult.Success<XrpDrops>>()
            (result as XrplResult.Success<XrpDrops>).value shouldBe XrpDrops(200_000_000)
        }
    }
})
