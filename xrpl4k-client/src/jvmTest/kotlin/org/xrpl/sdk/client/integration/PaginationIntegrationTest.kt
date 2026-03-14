@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.rpc.accountTransactions
import org.xrpl.sdk.client.rpc.accountTx
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for pagination: marker-based cursor flows.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class PaginationIntegrationTest : IntegrationTestBase({

    /**
     * Sends [count] payments from [sender] to [recipient] and advances the ledger after each.
     */
    suspend fun sendPayments(
        client: org.xrpl.sdk.client.XrplClient,
        sender: Wallet,
        recipient: Address,
        count: Int,
    ) = coroutineScope {
        val advancer =
            launch {
                repeat(count * 10) {
                    delay(2000)
                    ledgerAccept(client)
                }
            }
        repeat(count) {
            client.submitAndWait(
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = sender.address,
                    fields =
                        PaymentFields(
                            destination = recipient,
                            amount = XrpAmount(XrpDrops(1_000_000L)),
                        ),
                ),
                sender,
            )
        }
        advancer.cancelAndJoin()
    }

    test("accountTransactions flow collects all transactions across pages") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                val sender = fundNewWallet(c)
                val receiver = fundNewWallet(c)

                sender.use { s ->
                    receiver.use { r ->
                        // Send 5 payments
                        sendPayments(c, s, r.address, 5)

                        // Collect all with small page size to force pagination
                        val allTx =
                            c
                                .accountTransactions(account = s.address, pageSize = 2)
                                .toList()

                        // At minimum 5 payment txns
                        (allTx.size >= 5) shouldBe true
                        allTx.forEach { entry ->
                            entry.validated shouldBe true
                        }
                    }
                }
            }
        }
    }

    test("accountTx manual pagination with marker returns non-overlapping pages") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                val sender = fundNewWallet(c)
                val receiver = fundNewWallet(c)

                sender.use { s ->
                    receiver.use { r ->
                        sendPayments(c, s, r.address, 4)

                        // First page (limit=2)
                        val page1 = c.accountTx(account = s.address, limit = 2).getOrThrow()
                        (page1.transactions.size >= 1) shouldBe true

                        // If there's a marker, fetch the second page
                        val marker = page1.marker
                        if (marker != null) {
                            val page2 = c.accountTx(account = s.address, limit = 2, marker = marker).getOrThrow()
                            (page2.transactions.size >= 1) shouldBe true

                            // Ensure no overlap: tx JsonElement toString used as identity proxy
                            val set1 = page1.transactions.map { it.tx.toString() }.toSet()
                            val set2 = page2.transactions.map { it.tx.toString() }.toSet()
                            (set1 intersect set2).isEmpty() shouldBe true
                        } else {
                            // All transactions fit in one page
                            (page1.transactions.size >= 4) shouldBe true
                        }
                    }
                }
            }
        }
    }

    test("accountTransactions forward=true returns earliest transaction first") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                val sender = fundNewWallet(c)
                val receiver = fundNewWallet(c)

                sender.use { s ->
                    receiver.use { r ->
                        sendPayments(c, s, r.address, 3)

                        val forwardTx = c.accountTransactions(account = s.address, forward = true).toList()
                        val reverseTx = c.accountTransactions(account = s.address, forward = false).toList()

                        // Same count regardless of order
                        forwardTx.size shouldBe reverseTx.size
                        (forwardTx.size >= 3) shouldBe true

                        // First tx in forward == last tx in reverse
                        forwardTx.first().tx.toString() shouldBe reverseTx.last().tx.toString()
                    }
                }
            }
        }
    }
})
