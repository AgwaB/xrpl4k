@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.client.rpc.tx
import org.xrpl.sdk.client.signing.signTransaction
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Full payment lifecycle integration test.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class PaymentLifecycleTest : IntegrationTestBase({

    val provider = platformCryptoProvider()

    test("fund wallet from genesis and verify balance") {
        runTest {
            val client = createClient()
            client.use { c ->
                val wallet = fundNewWallet(c)
                wallet.use { w ->
                    val info = c.accountInfo(w.address).getOrThrow()
                    info.balance shouldBe FUNDING_AMOUNT
                }
            }
        }
    }

    test("send XRP payment between two funded wallets") {
        runTest {
            val client = createClient()
            client.use { c ->
                val sender = fundNewWallet(c)
                val receiver = fundNewWallet(c)

                sender.use { s ->
                    receiver.use { r ->
                        val senderInfo = c.accountInfo(s.address).getOrThrow()

                        val paymentAmount = XrpDrops(10_000_000L)
                        val filled =
                            XrplTransaction.Filled.create(
                                transactionType = TransactionType.Payment,
                                account = s.address,
                                fields =
                                    PaymentFields(
                                        destination = r.address,
                                        amount = XrpAmount(paymentAmount),
                                    ),
                                fee = XrpDrops(12),
                                sequence = senderInfo.sequence,
                                lastLedgerSequence = UInt.MAX_VALUE,
                            )
                        val signed = s.signTransaction(filled, provider)

                        val submitResult = c.submit(signed)
                        submitResult.shouldBeInstanceOf<XrplResult.Success<*>>()

                        ledgerAccept(c)

                        val txResult = c.tx(signed.hash)
                        txResult.shouldBeInstanceOf<XrplResult.Success<*>>()
                        val txInfo = txResult.getOrThrow()
                        txInfo.validated shouldBe true

                        val receiverInfo = c.accountInfo(r.address).getOrThrow()
                        (receiverInfo.balance.value > FUNDING_AMOUNT.value) shouldBe true
                    }
                }
            }
        }
    }

    test("accountInfo returns NotFound for unfunded account") {
        runTest {
            val client = createClient()
            client.use { c ->
                val generated = Wallet.generate(provider = provider)
                generated.wallet.use { w ->
                    val result = c.accountInfo(w.address)
                    result.shouldBeInstanceOf<XrplResult.Failure>()
                }
            }
        }
    }
})
