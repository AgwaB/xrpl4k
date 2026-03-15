@file:Suppress("MagicNumber")
@file:OptIn(ExperimentalXrplApi::class)

package org.xrpl.sdk.client.signing

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.transaction.LoanSetFields
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

class LoanSignUtilsTest : FunSpec({

    val provider = platformCryptoProvider()

    fun loanSetTx(): XrplTransaction.Filled =
        XrplTransaction.Filled.create(
            transactionType = TransactionType.LoanSet,
            account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
            fields = LoanSetFields(collateralAsset = "XRP", loanAsset = "USD"),
            fee = XrpDrops(12),
            sequence = 1u,
            lastLedgerSequence = 100u,
        )

    // ── signLoanSetByCounterparty ─────────────────────────────────

    test("signLoanSetByCounterparty produces non-empty signature") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val sig = wallet.signLoanSetByCounterparty(loanSetTx(), provider)
            sig.signer.txnSignature.isNotEmpty() shouldBe true
            sig.signer.signingPubKey.isNotEmpty() shouldBe true
            sig.signer.account.value shouldStartWith "r"
        }
    }

    test("signLoanSetByCounterparty with Secp256k1 produces non-empty signature") {
        val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        generated.wallet.use { wallet ->
            val sig = wallet.signLoanSetByCounterparty(loanSetTx(), provider)
            sig.signer.txnSignature.isNotEmpty() shouldBe true
        }
    }

    test("signLoanSetByCounterparty is deterministic for Ed25519") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val tx = loanSetTx()
            val sig1 = wallet.signLoanSetByCounterparty(tx, provider)
            val sig2 = wallet.signLoanSetByCounterparty(tx, provider)
            sig1.signer.txnSignature shouldBe sig2.signer.txnSignature
        }
    }

    test("signLoanSetByCounterparty rejects non-LoanSet transaction") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val paymentTx =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    fields =
                        PaymentFields(
                            destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                            amount = org.xrpl.sdk.core.model.amount.XrpAmount(XrpDrops(1_000_000)),
                        ),
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                )
            shouldThrow<IllegalArgumentException> {
                wallet.signLoanSetByCounterparty(paymentTx, provider)
            }
        }
    }

    // ── combineLoanSetCounterpartySigners ─────────────────────────

    test("combineLoanSetCounterpartySigners sorts signers and formats correctly") {
        val w1 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val w2 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        w1.wallet.use { wallet1 ->
            w2.wallet.use { wallet2 ->
                val tx = loanSetTx()
                val sig1 = wallet1.signLoanSetByCounterparty(tx, provider)
                val sig2 = wallet2.signLoanSetByCounterparty(tx, provider)

                val combined = combineLoanSetCounterpartySigners(tx, listOf(sig1, sig2))
                combined shouldHaveSize 2

                // Verify sorted by account
                @Suppress("UNCHECKED_CAST")
                val accounts =
                    combined.map { entry ->
                        (entry["Signer"] as Map<String, Any?>)["Account"] as String
                    }
                accounts shouldBe accounts.sorted()

                // Verify structure has Signer wrapper
                @Suppress("UNCHECKED_CAST")
                val first = combined[0]["Signer"] as Map<String, Any?>
                first.containsKey("Account") shouldBe true
                first.containsKey("SigningPubKey") shouldBe true
                first.containsKey("TxnSignature") shouldBe true
            }
        }
    }

    test("combineLoanSetCounterpartySigners with single signer returns list of one") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val tx = loanSetTx()
            val sig = wallet.signLoanSetByCounterparty(tx, provider)
            val combined = combineLoanSetCounterpartySigners(tx, listOf(sig))
            combined shouldHaveSize 1
        }
    }

    test("combineLoanSetCounterpartySigners rejects empty signatures") {
        shouldThrow<IllegalArgumentException> {
            combineLoanSetCounterpartySigners(loanSetTx(), emptyList())
        }
    }

    test("different wallets produce different counterparty signatures") {
        val w1 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val w2 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        w1.wallet.use { wallet1 ->
            w2.wallet.use { wallet2 ->
                val tx = loanSetTx()
                val sig1 = wallet1.signLoanSetByCounterparty(tx, provider)
                val sig2 = wallet2.signLoanSetByCounterparty(tx, provider)
                sig1.signer.txnSignature shouldNotBe sig2.signer.txnSignature
            }
        }
    }
})
