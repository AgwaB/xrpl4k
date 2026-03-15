@file:Suppress("MagicNumber")
@file:OptIn(ExperimentalXrplApi::class)

package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.transaction.BatchFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

class BatchSignUtilsTest : FunSpec({

    val provider = platformCryptoProvider()

    // A minimal inner transaction map in wire format for BinaryCodec.encode.
    // TransactionType must be numeric (0 = Payment), account IDs must be 20-byte hex.
    val accountHex = "b5f762798a53d543a014caf8b297cff8f2f937e8"
    val destHex1 = "f667b0ca50cc7709a220b0561b85e53a48461a8f"
    val destHex2 = "0ae35e0e69eac12e8fe38c45d3e5aa3d79f9c111"

    fun innerTxMap(dest: String = destHex1): Map<String, Any?> =
        mapOf(
            // Payment = 0
            "TransactionType" to 0,
            "Flags" to 0,
            "Account" to accountHex,
            "Destination" to dest,
            "Amount" to "1000000",
            "Fee" to "12",
            "Sequence" to 1,
            "SigningPubKey" to "",
        )

    fun batchTx(
        rawTransactions: List<Map<String, Any?>>,
        flags: UInt? = null,
    ): XrplTransaction.Filled =
        XrplTransaction.Filled.create(
            transactionType = TransactionType.Batch,
            account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
            fields = BatchFields(rawTransactions = rawTransactions, flags = flags),
            fee = XrpDrops(12),
            sequence = 1u,
            lastLedgerSequence = 100u,
        )

    // ── signBatchTransaction ────────────────────────────────────

    test("signBatchTransaction produces non-empty signature") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val filled = batchTx(listOf(innerTxMap()))
            val sig = wallet.signBatchTransaction(filled, provider)
            sig.signer.txnSignature.isNotEmpty() shouldBe true
            sig.signer.signingPubKey.isNotEmpty() shouldBe true
            sig.signer.account.value shouldStartWith "r"
        }
    }

    test("signBatchTransaction with Secp256k1 produces non-empty signature") {
        val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        generated.wallet.use { wallet ->
            val filled = batchTx(listOf(innerTxMap()))
            val sig = wallet.signBatchTransaction(filled, provider)
            sig.signer.txnSignature.isNotEmpty() shouldBe true
            sig.signer.signingPubKey.isNotEmpty() shouldBe true
        }
    }

    test("signBatchTransaction is deterministic for Ed25519") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val filled = batchTx(listOf(innerTxMap()))
            val sig1 = wallet.signBatchTransaction(filled, provider)
            val sig2 = wallet.signBatchTransaction(filled, provider)
            sig1.signer.txnSignature shouldBe sig2.signer.txnSignature
        }
    }

    test("signBatchTransaction with different inner txs produces different signatures") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val filled1 = batchTx(listOf(innerTxMap(destHex1)))
            val filled2 = batchTx(listOf(innerTxMap(destHex2)))
            val sig1 = wallet.signBatchTransaction(filled1, provider)
            val sig2 = wallet.signBatchTransaction(filled2, provider)
            sig1.signer.txnSignature shouldNotBe sig2.signer.txnSignature
        }
    }

    // ── combineBatchSigners ─────────────────────────────────────

    test("combineBatchSigners sorts signers by account and formats correctly") {
        val w1 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val w2 = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        w1.wallet.use { wallet1 ->
            w2.wallet.use { wallet2 ->
                val filled = batchTx(listOf(innerTxMap()))
                val sig1 = wallet1.signBatchTransaction(filled, provider)
                val sig2 = wallet2.signBatchTransaction(filled, provider)

                val combined = combineBatchSigners(listOf(sig1, sig2))
                combined shouldHaveSize 2

                // Verify sorted by account
                @Suppress("UNCHECKED_CAST")
                val accounts =
                    combined.map { entry ->
                        (entry["BatchSigner"] as Map<String, Any?>)["Account"] as String
                    }
                accounts shouldBe accounts.sorted()

                // Verify structure has BatchSigner wrapper
                @Suppress("UNCHECKED_CAST")
                val first = combined[0]["BatchSigner"] as Map<String, Any?>
                first.containsKey("Account") shouldBe true
                first.containsKey("SigningPubKey") shouldBe true
                first.containsKey("TxnSignature") shouldBe true
            }
        }
    }

    test("combineBatchSigners with single signer returns list of one") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val filled = batchTx(listOf(innerTxMap()))
            val sig = wallet.signBatchTransaction(filled, provider)
            val combined = combineBatchSigners(listOf(sig))
            combined shouldHaveSize 1
        }
    }
})
