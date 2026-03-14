@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.platformCryptoProvider
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for sugar functions: autofill and submitAndWait.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class SugarFunctionsIntegrationTest : IntegrationTestBase({

    val provider = platformCryptoProvider()

    test("autofill fills Fee, Sequence, and LastLedgerSequence automatically") {
        runTest {
            val client = createClient()
            client.use { c ->
                val sender = fundNewWallet(c)
                val receiver = fundNewWallet(c)

                sender.use { s ->
                    receiver.use { r ->
                        val unsigned =
                            XrplTransaction.Unsigned(
                                transactionType = TransactionType.Payment,
                                account = s.address,
                                fields =
                                    PaymentFields(
                                        destination = r.address,
                                        amount = XrpAmount(XrpDrops(1_000_000L)),
                                    ),
                            )

                        val result = c.autofill(unsigned)
                        result.shouldBeInstanceOf<XrplResult.Success<*>>()
                        val filled = result.getOrThrow()

                        (filled.fee.value > 0L) shouldBe true
                        (filled.sequence > 0u) shouldBe true
                        (filled.lastLedgerSequence > 0u) shouldBe true
                    }
                }
            }
        }
    }

    test("submitAndWait sends payment and returns validated result") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                val sender = fundNewWallet(c)
                val receiver = fundNewWallet(c)

                sender.use { s ->
                    receiver.use { r ->
                        val unsigned =
                            XrplTransaction.Unsigned(
                                transactionType = TransactionType.Payment,
                                account = s.address,
                                fields =
                                    PaymentFields(
                                        destination = r.address,
                                        amount = XrpAmount(XrpDrops(1_000_000L)),
                                    ),
                            )

                        // In standalone mode the ledger does not auto-close.
                        // Advance it periodically while submitAndWait polls.
                        val advancer =
                            launch {
                                repeat(30) {
                                    delay(2000)
                                    ledgerAccept(c)
                                }
                            }

                        val result = c.submitAndWait(unsigned, s)
                        advancer.cancelAndJoin()

                        result.shouldBeInstanceOf<XrplResult.Success<*>>()
                        val validated = result.getOrThrow()
                        validated.hash.shouldNotBeNull()
                        validated.ledgerIndex.shouldNotBeNull()
                        (validated.engineResult?.startsWith("tes")) shouldBe true
                    }
                }
            }
        }
    }
})
