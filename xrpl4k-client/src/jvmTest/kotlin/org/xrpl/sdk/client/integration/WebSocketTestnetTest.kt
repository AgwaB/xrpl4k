@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.subscription.subscribeToAccount
import org.xrpl.sdk.client.subscription.subscribeToLedger
import org.xrpl.sdk.client.subscription.subscribeToTransactions
import org.xrpl.sdk.client.sugar.fundWallet
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops
import kotlin.time.Duration.Companion.seconds
import io.kotest.matchers.longs.shouldBeGreaterThan as longShouldBeGreaterThan

/**
 * Tag for testnet integration tests — excluded by default.
 * Run with: `./gradlew jvmTest -Ptestnet`
 */
object Testnet : Tag()

/**
 * WebSocket integration tests against the XRPL public testnet.
 *
 * These tests require network access to wss://s.altnet.rippletest.net:51233
 * and are NOT run in CI by default. They validate real network behavior
 * including ledger subscriptions, account events, and connection resilience.
 *
 * Run with: `./gradlew jvmTest -Ptestnet`
 *
 * Note: These tests may be flaky due to network conditions and testnet
 * availability. Generous timeouts are used to accommodate variable latency.
 */
class WebSocketTestnetTest : FunSpec({

    tags(Testnet)

    fun createTestnetClient(): XrplClient =
        XrplClient {
            network = Network.Testnet
            timeout = 60.seconds
        }

    test("connect to testnet and receive ledger close events") {
        kotlinx.coroutines.runBlocking {
            createTestnetClient().use { client ->
                val events =
                    withTimeout(60.seconds) {
                        client.subscribeToLedger()
                            .take(2)
                            .toList()
                    }

                events.size shouldBe 2

                // Verify ledger indices are increasing
                events[1].ledgerIndex.value shouldBeGreaterThan events[0].ledgerIndex.value

                // Verify ledger hashes are 64-char hex (Hash256 enforces this at construction)
                events.forEach { event ->
                    event.ledgerHash.value.length shouldBe 64
                    event.ledgerHash.value shouldMatch "[0-9A-F]{64}"
                }
            }
        }
    }

    test("fund wallet and subscribe to account events").config(enabled = false) {
        // Disabled by default: faucet may be unreliable
        kotlinx.coroutines.runBlocking {
            createTestnetClient().use { client ->
                val fundResult = client.fundWallet()
                val sender = fundResult.wallet
                fundResult.balance longShouldBeGreaterThan 0L

                sender.use { w ->
                    // Subscribe to the funded account
                    val accountFlow = client.subscribeToAccount(w.address)

                    // Send a small payment to a burn address
                    val burnAddress = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
                    val paymentTx =
                        XrplTransaction.Unsigned(
                            transactionType = TransactionType.Payment,
                            account = w.address,
                            fields =
                                PaymentFields(
                                    destination = burnAddress,
                                    // 1 XRP
                                    amount = XrpAmount(XrpDrops(1_000_000L)),
                                ),
                        )

                    // Launch subscription and submit concurrently
                    val eventDeferred =
                        async {
                            withTimeout(60.seconds) {
                                accountFlow.first()
                            }
                        }

                    // Submit and wait for validation
                    val txResult = client.submitAndWait(paymentTx, w)
                    txResult.getOrThrow()

                    // Wait for the account event
                    val accountEvent = eventDeferred.await()
                    accountEvent.hash shouldNotBe null
                    accountEvent.engineResult shouldNotBe null
                }
            }
        }
    }

    test("concurrent subscriptions - ledger and transactions streams") {
        kotlinx.coroutines.runBlocking {
            createTestnetClient().use { client ->
                val ledgerDeferred =
                    async {
                        withTimeout(60.seconds) {
                            client.subscribeToLedger().first()
                        }
                    }

                val txDeferred =
                    async {
                        withTimeout(60.seconds) {
                            client.subscribeToTransactions().first()
                        }
                    }

                val ledgerEvent = ledgerDeferred.await()
                val txEvent = txDeferred.await()

                // Both events should have valid data
                ledgerEvent.ledgerIndex.value shouldBeGreaterThan 0u
                ledgerEvent.ledgerHash.value.length shouldBe 64

                txEvent shouldNotBe null
                txEvent.validated shouldBe true
            }
        }
    }

    test("connection resilience - reconnect after close") {
        kotlinx.coroutines.runBlocking {
            // First connection
            val event1 =
                createTestnetClient().use { client ->
                    withTimeout(60.seconds) {
                        client.subscribeToLedger().first()
                    }
                }
            // client is closed after use {} block

            event1.ledgerIndex.value shouldBeGreaterThan 0u
            event1.ledgerHash.value.length shouldBe 64

            // Second connection — fresh client
            val event2 =
                createTestnetClient().use { client ->
                    withTimeout(60.seconds) {
                        client.subscribeToLedger().first()
                    }
                }

            event2.ledgerIndex.value shouldBeGreaterThan 0u
            event2.ledgerHash.value.length shouldBe 64

            // Second event should be same or later ledger
            event2.ledgerIndex.value shouldBeGreaterThan (event1.ledgerIndex.value - 1u)
        }
    }
})
