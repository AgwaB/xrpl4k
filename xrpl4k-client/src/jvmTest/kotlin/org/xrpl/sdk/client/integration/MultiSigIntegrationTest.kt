@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.client.signing.combineSignatures
import org.xrpl.sdk.client.signing.multiSignTransaction
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.SignerEntry
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.model.transaction.signerListSet
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.platformCryptoProvider
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for multi-signature transactions on a live rippled node.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class MultiSigIntegrationTest : IntegrationTestBase({

    val provider = platformCryptoProvider()

    test("multi-sig payment with 2 signers at quorum is accepted by rippled") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                // Fund 3 accounts: mainAccount (the sender), signer1, signer2
                val mainWallet = fundNewWallet(c)
                val signer1 = fundNewWallet(c)
                val signer2 = fundNewWallet(c)
                val recipient = fundNewWallet(c)

                mainWallet.use { main ->
                    signer1.use { s1 ->
                        signer2.use { s2 ->
                            recipient.use { r ->
                                val advancer =
                                    launch {
                                        repeat(60) {
                                            delay(2000)
                                            ledgerAccept(c)
                                        }
                                    }

                                // Step 1: Set up signer list on mainAccount (quorum=2, both signers weight=1)
                                val signerListTx =
                                    signerListSet {
                                        account = main.address
                                        signerQuorum = 2u
                                        signerEntries =
                                            listOf(
                                                SignerEntry(account = s1.address, signerWeight = 1u),
                                                SignerEntry(account = s2.address, signerWeight = 1u),
                                            )
                                    }

                                val signerListResult = c.submitAndWait(signerListTx, main)
                                signerListResult.shouldBeInstanceOf<XrplResult.Success<*>>()
                                (signerListResult.getOrThrow().engineResult?.startsWith("tes")) shouldBe true

                                // Step 2: Create a payment from mainAccount, autofill fields
                                val paymentUnsigned =
                                    XrplTransaction.Unsigned(
                                        transactionType = TransactionType.Payment,
                                        account = main.address,
                                        fields =
                                            PaymentFields(
                                                destination = r.address,
                                                amount = XrpAmount(XrpDrops(10_000_000L)),
                                            ),
                                    )

                                // Autofill with extra fee for multi-sig (fee = base + 12 * numSigners)
                                val filledResult = c.autofill(paymentUnsigned)
                                filledResult.shouldBeInstanceOf<XrplResult.Success<*>>()
                                val filled = filledResult.getOrThrow()

                                // Use higher fee for multi-sig (signers add 12 drops each)
                                val filledWithHigherFee =
                                    XrplTransaction.Filled.create(
                                        transactionType = filled.transactionType,
                                        account = filled.account,
                                        fields = filled.fields,
                                        // base 12 + 2*12 for 2 signers
                                        fee = XrpDrops(36),
                                        sequence = filled.sequence,
                                        lastLedgerSequence = filled.lastLedgerSequence,
                                    )

                                // Step 3: Each signer multi-signs the transaction
                                val sig1 = s1.multiSignTransaction(filledWithHigherFee, provider)
                                val sig2 = s2.multiSignTransaction(filledWithHigherFee, provider)

                                // Step 4: Combine signatures and submit
                                val multiSigned = combineSignatures(filledWithHigherFee, listOf(sig1, sig2), provider)
                                val submitResult = c.submit(multiSigned)

                                submitResult.shouldBeInstanceOf<XrplResult.Success<*>>()
                                val submitSuccess = submitResult.getOrThrow()
                                // tesSUCCESS or queued
                                (
                                    submitSuccess.engineResult?.startsWith("tes") == true ||
                                        submitSuccess.applied == true
                                ) shouldBe true

                                advancer.cancelAndJoin()
                            }
                        }
                    }
                }
            }
        }
    }
})
