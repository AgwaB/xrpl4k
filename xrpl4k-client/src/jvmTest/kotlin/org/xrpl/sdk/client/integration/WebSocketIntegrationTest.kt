@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.model.LedgerEvent
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.client.signing.signTransaction
import org.xrpl.sdk.client.subscription.subscribeToAccount
import org.xrpl.sdk.client.subscription.subscribeToLedger
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

private const val TEST_TIMEOUT_MS = 60_000L
private const val SUBSCRIPTION_SETTLE_MS = 1000L

/**
 * WebSocket integration tests against a local rippled standalone node.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class WebSocketIntegrationTest : IntegrationTestBase({

    val provider = platformCryptoProvider()

    // -----------------------------------------------------------------------
    // 1. Basic WS connect -> subscribe -> response
    // -----------------------------------------------------------------------
    test("basic WS subscribe to ledger stream returns success") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    // subscribeToLedger issues a subscribe(streams=["ledger"]) under the hood.
                    // Collecting one event proves the WS round-trip works.
                    val advancer =
                        launch {
                            repeat(5) {
                                delay(1000)
                                ledgerAccept(c)
                            }
                        }

                    val events = c.subscribeToLedger().take(1).toList()
                    advancer.cancelAndJoin()

                    events shouldHaveSize 1
                    events.first().ledgerIndex.shouldNotBeNull()
                    events.first().ledgerHash.shouldNotBeNull()
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 2. subscribeToLedger receives events
    // -----------------------------------------------------------------------
    test("subscribeToLedger receives 3 ledger close events") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    val collector =
                        async {
                            c.subscribeToLedger().take(3).toList()
                        }

                    // Give the subscription time to establish
                    delay(SUBSCRIPTION_SETTLE_MS)

                    repeat(5) {
                        ledgerAccept(c)
                        delay(500)
                    }

                    val events = collector.await()
                    events shouldHaveSize 3

                    events.forEach { event ->
                        event.ledgerIndex.shouldNotBeNull()
                        event.ledgerHash.shouldNotBeNull()
                    }

                    // Ledger indices should be strictly increasing
                    val indices = events.map { it.ledgerIndex.value }
                    indices shouldBe indices.sorted()
                    indices.distinct() shouldHaveSize 3
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 3. subscribeToAccount receives payment events
    // -----------------------------------------------------------------------
    test("subscribeToAccount receives payment events with tesSUCCESS") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    val recipient = fundNewWallet(c)

                    recipient.use { r ->
                        val collector =
                            async {
                                c.subscribeToAccount(r.address)
                                    .take(2)
                                    .toList()
                            }

                        // Give the subscription time to establish
                        delay(SUBSCRIPTION_SETTLE_MS)

                        // Send 2 payments to the recipient
                        repeat(2) {
                            sendPayment(c, r.address, XrpDrops(1_000_000L))
                            ledgerAccept(c)
                            delay(500)
                        }

                        val events = collector.await()
                        events shouldHaveSize 2

                        events.forEach { event ->
                            event.engineResult shouldBe "tesSUCCESS"
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 4. Concurrent WS — multiple subscriptions collected in parallel
    // -----------------------------------------------------------------------
    test("concurrent ledger event collection from multiple coroutines") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    // Start 5 coroutines each collecting 2 ledger events
                    val collectors =
                        (1..5).map {
                            async {
                                c.subscribeToLedger().take(2).toList()
                            }
                        }

                    // Give subscriptions time to establish
                    delay(2000)

                    // Advance ledgers enough for all collectors
                    repeat(5) {
                        ledgerAccept(c)
                        delay(500)
                    }

                    // All collectors should complete with 2 events each
                    collectors.forEach { deferred ->
                        val events = deferred.await()
                        events shouldHaveSize 2
                        events.forEach { it.ledgerIndex.shouldNotBeNull() }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 5. Subscription survives idle period then receives events
    // -----------------------------------------------------------------------
    test("subscription works after idle period without ledger advances") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    val collector =
                        async {
                            c.subscribeToLedger().take(2).toList()
                        }

                    // Give subscription time to establish
                    delay(SUBSCRIPTION_SETTLE_MS)

                    // Idle period — no ledger_accept for 3 seconds
                    delay(3000)

                    // Now advance ledgers — subscription should still work
                    repeat(3) {
                        ledgerAccept(c)
                        delay(500)
                    }

                    val events = collector.await()
                    events shouldHaveSize 2
                    events.forEach { it.ledgerIndex.shouldNotBeNull() }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 6. Close then request returns Failure
    // -----------------------------------------------------------------------
    test("subscribing after client close throws or returns failure") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()

                client.use { c ->
                    // Establish a WS connection via one event
                    val advancer =
                        launch {
                            repeat(3) {
                                delay(500)
                                ledgerAccept(c)
                            }
                        }
                    c.subscribeToLedger().take(1).toList()
                    advancer.cancelAndJoin()
                }

                // Client is now closed — attempting another subscribe should fail
                val result =
                    runCatching {
                        withTimeout(5000) {
                            client.subscribeToLedger().take(1).toList()
                        }
                    }
                result.isFailure shouldBe true
            }
        }
    }

    // -----------------------------------------------------------------------
    // 7. Multiple subscriptions simultaneously (ledger + account)
    // -----------------------------------------------------------------------
    test("simultaneous ledger and account subscriptions both receive events") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    val recipient = fundNewWallet(c)

                    recipient.use { r ->
                        val ledgerCollector =
                            async {
                                c.subscribeToLedger().take(2).toList()
                            }

                        val accountCollector =
                            async {
                                c.subscribeToAccount(r.address).take(1).toList()
                            }

                        // Give subscriptions time to establish
                        delay(SUBSCRIPTION_SETTLE_MS)

                        // Send a payment (triggers both ledger close and account event)
                        sendPayment(c, r.address, XrpDrops(1_000_000L))
                        ledgerAccept(c)
                        delay(500)
                        ledgerAccept(c)
                        delay(500)

                        val ledgerEvents = ledgerCollector.await()
                        val accountEvents = accountCollector.await()

                        ledgerEvents shouldHaveSize 2
                        ledgerEvents.forEach { it.ledgerIndex.shouldNotBeNull() }

                        accountEvents shouldHaveSize 1
                        accountEvents.first().engineResult shouldBe "tesSUCCESS"
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 8. Subscription cancellation with take(N)
    // -----------------------------------------------------------------------
    test("take(2) collects exactly 2 events even when more are available") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    val events = mutableListOf<LedgerEvent>()

                    val collector =
                        async {
                            c.subscribeToLedger().take(2).toList().also { events.addAll(it) }
                        }

                    // Give subscription time to establish
                    delay(SUBSCRIPTION_SETTLE_MS)

                    // Advance ledger 5 times (more than take(2) needs)
                    repeat(5) {
                        ledgerAccept(c)
                        delay(500)
                    }

                    collector.await()

                    // Should have exactly 2 despite 5 advances
                    events shouldHaveSize 2
                    events[0].ledgerIndex.shouldNotBeNull()
                    events[1].ledgerIndex.shouldNotBeNull()
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // 9. Rapid-fire events (burst) — simplified to 3 payments
    // -----------------------------------------------------------------------
    test("rapid-fire payments generate burst of account events") {
        kotlinx.coroutines.runBlocking {
            withTimeout(TEST_TIMEOUT_MS) {
                val client = createClient()
                client.use { c ->
                    val recipient = fundNewWallet(c)

                    recipient.use { r ->
                        val collector =
                            async {
                                c.subscribeToAccount(r.address).take(3).toList()
                            }

                        // Give subscription time to establish
                        delay(SUBSCRIPTION_SETTLE_MS)

                        // Send 3 payments in rapid succession
                        repeat(3) {
                            sendPayment(c, r.address, XrpDrops(100_000L))
                            ledgerAccept(c)
                            delay(200)
                        }

                        val events = collector.await()
                        events.size shouldBeGreaterThanOrEqualTo 3

                        // All events should be successful
                        events.forEach { event ->
                            event.engineResult shouldBe "tesSUCCESS"
                        }
                    }
                }
            }
        }
    }
})

// ---------------------------------------------------------------------------
// Helper: send a simple XRP payment from genesis to a destination
// ---------------------------------------------------------------------------
private suspend fun sendPayment(
    client: XrplClient,
    destination: org.xrpl.sdk.core.type.Address,
    amount: XrpDrops,
) {
    val genesis = Wallet.fromSeed(GENESIS_SEED, platformCryptoProvider())
    genesis.use { g ->
        val accountInfo = client.accountInfo(GENESIS_ADDRESS).getOrThrow()

        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.Payment,
                account = GENESIS_ADDRESS,
                fields =
                    PaymentFields(
                        destination = destination,
                        amount = XrpAmount(amount),
                    ),
                fee = XrpDrops(12),
                sequence = accountInfo.sequence,
                lastLedgerSequence = UInt.MAX_VALUE,
            )

        val signed = g.signTransaction(filled, platformCryptoProvider())
        val result = client.submit(signed)
        check(result is XrplResult.Success) { "Payment submission failed: $result" }
    }
}
