@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * T8 — Signing integration tests with golden file reference vectors.
 */
class SigningIntegrationTest : FunSpec({

    val provider = platformCryptoProvider()
    val recipient = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")

    fun loadGoldenVectors(name: String): JsonObject {
        val text =
            this::class.java.classLoader
                .getResource("fixtures/golden/$name")!!
                .readText()
        return Json.parseToJsonElement(text).jsonObject
    }

    fun signVector(
        seed: String,
        vec: JsonObject,
    ): Pair<String, String> {
        val wallet = Wallet.fromSeed(seed, provider)
        return wallet.use { w ->
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = w.address,
                    fields =
                        PaymentFields(
                            destination = Address(vec["destination"]!!.jsonPrimitive.content),
                            amount = XrpAmount(XrpDrops(vec["amount"]!!.jsonPrimitive.content.toLong())),
                        ),
                    fee = XrpDrops(vec["fee"]!!.jsonPrimitive.content.toLong()),
                    sequence = vec["sequence"]!!.jsonPrimitive.int.toUInt(),
                    lastLedgerSequence = vec["lastLedgerSequence"]!!.jsonPrimitive.int.toUInt(),
                )
            val signed = w.signTransaction(filled, provider)
            signed.txBlob to signed.hash.value
        }
    }

    // ── Ed25519 golden file vectors ──────────────────────────────────────

    test("Ed25519 signing matches golden file for 3 vectors") {
        val golden = loadGoldenVectors("ed25519-signing-vectors.json")
        val seed = golden["seed"]!!.jsonPrimitive.content
        val vectors = golden["vectors"]!!.jsonArray

        vectors.size shouldBe 3

        for (vec in vectors) {
            val obj = vec.jsonObject
            val (txBlob, hash) = signVector(seed, obj)
            txBlob shouldBe obj["expectedTxBlob"]!!.jsonPrimitive.content
            hash shouldBe obj["expectedHash"]!!.jsonPrimitive.content
        }
    }

    // ── secp256k1 golden file vectors ────────────────────────────────────

    test("secp256k1 signing matches golden file for 3 vectors") {
        val golden = loadGoldenVectors("secp256k1-signing-vectors.json")
        val seed = golden["seed"]!!.jsonPrimitive.content
        val vectors = golden["vectors"]!!.jsonArray

        vectors.size shouldBe 3

        for (vec in vectors) {
            val obj = vec.jsonObject
            val (txBlob, hash) = signVector(seed, obj)
            txBlob shouldBe obj["expectedTxBlob"]!!.jsonPrimitive.content
            hash shouldBe obj["expectedHash"]!!.jsonPrimitive.content
        }
    }

    // ── Ed25519 wallet identity from known seed ──────────────────────────

    test("Ed25519 wallet from golden seed has expected address and public key") {
        val golden = loadGoldenVectors("ed25519-signing-vectors.json")
        val wallet = Wallet.fromSeed(golden["seed"]!!.jsonPrimitive.content, provider)
        wallet.use { w ->
            w.address.value shouldBe golden["address"]!!.jsonPrimitive.content
            w.publicKey.value shouldBe golden["publicKey"]!!.jsonPrimitive.content
            w.algorithm shouldBe KeyAlgorithm.Ed25519
        }
    }

    test("secp256k1 wallet from golden seed has expected address and public key") {
        val golden = loadGoldenVectors("secp256k1-signing-vectors.json")
        val wallet = Wallet.fromSeed(golden["seed"]!!.jsonPrimitive.content, provider)
        wallet.use { w ->
            w.address.value shouldBe golden["address"]!!.jsonPrimitive.content
            w.publicKey.value shouldBe golden["publicKey"]!!.jsonPrimitive.content
            w.algorithm shouldBe KeyAlgorithm.Secp256k1
        }
    }

    // ── Multi-signing with 2+ signers ────────────────────────────────────

    test("multi-signing with 2 signers produces sorted Signers array") {
        val w1 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        val w2 = Wallet.fromSeed("sp6JS7f14BuwFY8Mw6bTtLKWauoUs", provider)

        w1.use { wallet1 ->
            w2.use { wallet2 ->
                val filled =
                    XrplTransaction.Filled.create(
                        transactionType = TransactionType.Payment,
                        account = wallet1.address,
                        fields =
                            PaymentFields(
                                destination = recipient,
                                amount = XrpAmount(XrpDrops(1_000_000)),
                            ),
                        fee = XrpDrops(12),
                        sequence = 1u,
                        lastLedgerSequence = 100u,
                    )

                val sig1 = wallet1.multiSignTransaction(filled, provider)
                val sig2 = wallet2.multiSignTransaction(filled, provider)

                // Combine in both orders — result should be same (sorted)
                val combined1 = combineSignatures(filled, listOf(sig1, sig2), provider)
                val combined2 = combineSignatures(filled, listOf(sig2, sig1), provider)

                combined1.txBlob shouldBe combined2.txBlob
                combined1.hash shouldBe combined2.hash
                combined1.txBlob.shouldNotBeEmpty()

                // Verify both account IDs are in the blob
                val id1 = AddressCodec.decodeAddress(wallet1.address, provider).hex
                val id2 = AddressCodec.decodeAddress(wallet2.address, provider).hex
                combined1.txBlob.lowercase().contains(id1) shouldBe true
                combined1.txBlob.lowercase().contains(id2) shouldBe true
            }
        }
    }

    test("multi-signing with 3 signers produces correct output") {
        val entropy1 = ByteArray(16) { it.toByte() }
        val entropy2 = ByteArray(16) { (it + 0x10).toByte() }
        val entropy3 = ByteArray(16) { (it + 0x20).toByte() }

        val wallet1 = Wallet.fromEntropy(entropy1, KeyAlgorithm.Ed25519, provider)
        val wallet2 = Wallet.fromEntropy(entropy2, KeyAlgorithm.Ed25519, provider)
        val wallet3 = Wallet.fromEntropy(entropy3, KeyAlgorithm.Ed25519, provider)

        wallet1.use { w1 ->
            wallet2.use { w2 ->
                wallet3.use { w3 ->
                    val filled =
                        XrplTransaction.Filled.create(
                            transactionType = TransactionType.Payment,
                            account = w1.address,
                            fields =
                                PaymentFields(
                                    destination = recipient,
                                    amount = XrpAmount(XrpDrops(1_000_000)),
                                ),
                            fee = XrpDrops(12),
                            sequence = 1u,
                            lastLedgerSequence = 100u,
                        )

                    val sig1 = w1.multiSignTransaction(filled, provider)
                    val sig2 = w2.multiSignTransaction(filled, provider)
                    val sig3 = w3.multiSignTransaction(filled, provider)

                    val combined = combineSignatures(filled, listOf(sig3, sig1, sig2), provider)
                    combined.txBlob.shouldNotBeEmpty()
                    combined.hash.value.length shouldBe 64

                    // Deterministic: same input always gives same output
                    val combined2 = combineSignatures(filled, listOf(sig2, sig3, sig1), provider)
                    combined.txBlob shouldBe combined2.txBlob
                }
            }
        }
    }

    // ── Sign/verify roundtrip property test ──────────────────────────────

    test("property: Ed25519 signTransaction is deterministic for varying amounts (100+ iterations)") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use { w ->
            for (i in 1..120) {
                val amount = i.toLong() * 10_000L
                val filled =
                    XrplTransaction.Filled.create(
                        transactionType = TransactionType.Payment,
                        account = w.address,
                        fields =
                            PaymentFields(
                                destination = recipient,
                                amount = XrpAmount(XrpDrops(amount)),
                            ),
                        fee = XrpDrops(12),
                        sequence = i.toUInt(),
                        lastLedgerSequence = 100u,
                    )
                val s1 = w.signTransaction(filled, provider)
                val s2 = w.signTransaction(filled, provider)
                s1.txBlob shouldBe s2.txBlob
                s1.hash shouldBe s2.hash
            }
        }
    }
})
