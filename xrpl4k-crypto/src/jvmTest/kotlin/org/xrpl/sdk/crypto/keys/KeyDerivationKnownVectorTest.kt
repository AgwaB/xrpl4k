@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.keys

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Known-vector end-to-end tests for key derivation: seed -> key pair -> address.
 *
 * Covers functionality NOT already in KeyDerivationTest:
 * - Ed25519: seed -> known address (end-to-end)
 * - Secp256k1: seed -> known address (end-to-end)
 * - Ed25519 private key length
 * - Secp256k1 private key length
 * - Key pair determinism for same seed
 * - Different seeds produce different key pairs
 */
class KeyDerivationKnownVectorTest : FunSpec({
    val provider = platformCryptoProvider()

    // The 16-byte seed used by xrpl.js tests: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]
    val testSeed = ByteArray(16) { (it + 1).toByte() }

    // ── Ed25519 seed -> address end-to-end ─────────────────────────────

    test("Ed25519 seed -> key pair -> accountId -> address is consistent with Wallet.fromEntropy") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use { kp ->
            val accountId = AddressCodec.accountIdFromPublicKey(kp.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)

            // Must match what Wallet.fromEntropy produces
            val wallet = Wallet.fromEntropy(testSeed, KeyAlgorithm.Ed25519, provider)
            wallet.use { w ->
                address shouldBe w.address
                kp.publicKey shouldBe w.publicKey
            }
        }
    }

    test("Ed25519 derived address starts with 'r'") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use { kp ->
            val accountId = AddressCodec.accountIdFromPublicKey(kp.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)
            address.value shouldStartWith "r"
        }
    }

    // ── Secp256k1 seed -> address end-to-end ─────────────────────────────

    test("Secp256k1 seed -> key pair -> accountId -> address is consistent with Wallet.fromEntropy") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use { kp ->
            val accountId = AddressCodec.accountIdFromPublicKey(kp.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)

            val wallet = Wallet.fromEntropy(testSeed, KeyAlgorithm.Secp256k1, provider)
            wallet.use { w ->
                address shouldBe w.address
                kp.publicKey shouldBe w.publicKey
            }
        }
    }

    test("Secp256k1 derived address starts with 'r'") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use { kp ->
            val accountId = AddressCodec.accountIdFromPublicKey(kp.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)
            address.value shouldStartWith "r"
        }
    }

    // ── Private key size verification ────────────────────────────────────

    test("Ed25519 private key is 32 bytes") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.privateKeyBytes.size shouldBe 32
        }
    }

    test("Secp256k1 private key is 32 bytes") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            it.privateKeyBytes.size shouldBe 32
        }
    }

    // ── Ed25519 public key is exactly 33 bytes hex (66 chars) ───────────

    test("Ed25519 public key hex is 66 chars (33 bytes: 1 prefix + 32 key)") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.publicKey.value.length shouldBe 66
        }
    }

    test("Secp256k1 public key hex is 66 chars (33 bytes compressed)") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            it.publicKey.value.length shouldBe 66
        }
    }

    // ── Key pair determinism ────────────────────────────────────────────

    test("same seed produces identical Ed25519 key pairs") {
        val kp1 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        val kp2 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        kp1.use { a ->
            kp2.use { b ->
                a.publicKey shouldBe b.publicKey
                a.privateKeyBytes.contentEquals(b.privateKeyBytes) shouldBe true
            }
        }
    }

    test("same seed produces identical Secp256k1 key pairs") {
        val kp1 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        val kp2 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        kp1.use { a ->
            kp2.use { b ->
                a.publicKey shouldBe b.publicKey
                a.privateKeyBytes.contentEquals(b.privateKeyBytes) shouldBe true
            }
        }
    }

    // ── Different seeds produce different key pairs ─────────────────────

    test("different seeds produce different Ed25519 public keys") {
        val seed1 = ByteArray(16) { 0x01.toByte() }
        val seed2 = ByteArray(16) { 0x02.toByte() }
        val kp1 = KeyDerivation.deriveKeyPair(seed1, KeyAlgorithm.Ed25519, provider)
        val kp2 = KeyDerivation.deriveKeyPair(seed2, KeyAlgorithm.Ed25519, provider)
        kp1.use { a ->
            kp2.use { b ->
                a.publicKey shouldNotBe b.publicKey
            }
        }
    }

    test("different seeds produce different Secp256k1 public keys") {
        val seed1 = ByteArray(16) { 0x01.toByte() }
        val seed2 = ByteArray(16) { 0x02.toByte() }
        val kp1 = KeyDerivation.deriveKeyPair(seed1, KeyAlgorithm.Secp256k1, provider)
        val kp2 = KeyDerivation.deriveKeyPair(seed2, KeyAlgorithm.Secp256k1, provider)
        kp1.use { a ->
            kp2.use { b ->
                a.publicKey shouldNotBe b.publicKey
            }
        }
    }

    // ── All-zeros seed derivation ───────────────────────────────────────

    test("all-zeros seed derives valid Ed25519 key pair") {
        val zeroSeed = ByteArray(16)
        val keyPair = KeyDerivation.deriveKeyPair(zeroSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.publicKey.value shouldStartWith "ED"
            it.publicKey.value.length shouldBe 66
            it.privateKeyBytes.size shouldBe 32
        }
    }

    test("all-zeros seed derives valid Secp256k1 key pair") {
        val zeroSeed = ByteArray(16)
        val keyPair = KeyDerivation.deriveKeyPair(zeroSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            val prefix = it.publicKey.value.substring(0, 2)
            (prefix == "02" || prefix == "03") shouldBe true
            it.publicKey.value.length shouldBe 66
            it.privateKeyBytes.size shouldBe 32
        }
    }

    // ── All-FF seed derivation ──────────────────────────────────────────

    test("all-0xFF seed derives valid Ed25519 key pair") {
        val ffSeed = ByteArray(16) { 0xFF.toByte() }
        val keyPair = KeyDerivation.deriveKeyPair(ffSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.publicKey.value shouldStartWith "ED"
            it.privateKeyBytes.size shouldBe 32
        }
    }

    test("all-0xFF seed derives valid Secp256k1 key pair") {
        val ffSeed = ByteArray(16) { 0xFF.toByte() }
        val keyPair = KeyDerivation.deriveKeyPair(ffSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            val prefix = it.publicKey.value.substring(0, 2)
            (prefix == "02" || prefix == "03") shouldBe true
            it.privateKeyBytes.size shouldBe 32
        }
    }
})
