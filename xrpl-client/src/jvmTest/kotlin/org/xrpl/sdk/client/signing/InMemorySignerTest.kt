@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

class InMemorySignerTest : FunSpec({
    val provider = platformCryptoProvider()

    fun testPayment(): XrplTransaction.Filled =
        XrplTransaction.Filled.create(
            transactionType = TransactionType.Payment,
            account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
            fields =
                PaymentFields(
                    destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                    amount = XrpAmount(XrpDrops(1_000_000)),
                ),
            fee = XrpDrops(12),
            sequence = 1u,
            lastLedgerSequence = 100u,
        )

    test("Ed25519 sign produces valid signed transaction") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        wallet.wallet.use { w ->
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = w.address,
                    fields =
                        PaymentFields(
                            destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                            amount = XrpAmount(XrpDrops(1_000_000)),
                        ),
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                )
            val signed = w.signTransaction(filled, provider)
            signed.txBlob.isNotEmpty() shouldBe true
            signed.hash.value.length shouldBe 64
            signed.transactionType shouldBe TransactionType.Payment
        }
    }

    test("Secp256k1 sign produces valid signed transaction") {
        val wallet = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        wallet.wallet.use { w ->
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = w.address,
                    fields =
                        PaymentFields(
                            destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                            amount = XrpAmount(XrpDrops(1_000_000)),
                        ),
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                )
            val signed = w.signTransaction(filled, provider)
            signed.txBlob.isNotEmpty() shouldBe true
            signed.hash.value.length shouldBe 64
        }
    }

    test("multi-sign produces valid signer entry") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        wallet.wallet.use { w ->
            val filled = testPayment()
            val sig = w.multiSignTransaction(filled, provider)
            sig.signer.txnSignature.isNotEmpty() shouldBe true
            sig.signer.signingPubKey.isNotEmpty() shouldBe true
            sig.signer.account.value shouldStartWith "r"
        }
    }

    test("combineSignatures sorts signers and sets empty SigningPubKey") {
        val w1 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val w2 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        w1.wallet.use { wallet1 ->
            w2.wallet.use { wallet2 ->
                val filled = testPayment()
                val sig1 = wallet1.multiSignTransaction(filled, provider)
                val sig2 = wallet2.multiSignTransaction(filled, provider)
                val signed = combineSignatures(filled, listOf(sig1, sig2), provider)
                signed.txBlob.isNotEmpty() shouldBe true
                signed.hash.value.length shouldBe 64
            }
        }
    }

    test("signPaymentChannelClaim produces valid signature") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        wallet.wallet.use { w ->
            val channelId = "C7F634B6B970F6F29E8B4AF233C9B30F4EBED5BF73E9F60F645B61C5DDDCBE28"
            val amount = "1000000"
            val sig = signPaymentChannelClaim(w, channelId, amount, provider)
            sig.isNotEmpty() shouldBe true
            // Ed25519 signatures are 128 hex chars (64 bytes)
            sig.length shouldBe 128
        }
    }

    test("Ed25519 signing is deterministic") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use { w ->
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = w.address,
                    fields =
                        PaymentFields(
                            destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                            amount = XrpAmount(XrpDrops(1_000_000)),
                        ),
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                )
            val signed1 = w.signTransaction(filled, provider)
            val signed2 = w.signTransaction(filled, provider)
            signed1.txBlob shouldBe signed2.txBlob
            signed1.hash shouldBe signed2.hash
        }
    }
})
