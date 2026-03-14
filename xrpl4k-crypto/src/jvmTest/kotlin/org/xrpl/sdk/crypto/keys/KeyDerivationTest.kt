@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.keys

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for KeyDerivation -- Ed25519 and secp256k1 key pair derivation.
 *
 * Reference vectors from xrpl.js/packages/ripple-keypairs/test/fixtures/api.json
 * using entropy [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16].
 */
class KeyDerivationTest : FunSpec({

    val provider = platformCryptoProvider()

    // The 16-byte seed used by xrpl.js tests: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]
    val testSeed = ByteArray(16) { (it + 1).toByte() }

    // ── Ed25519 derivation ───────────────────────────────────────────────

    test("Ed25519 derivation from known seed produces expected public key") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.publicKey.value shouldBe "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63"
        }
    }

    test("Ed25519 public key starts with ED prefix") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.publicKey.value shouldStartWith "ED"
        }
    }

    test("Ed25519 derivation sets correct algorithm") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            it.algorithm shouldBe KeyAlgorithm.Ed25519
        }
    }

    // ── secp256k1 derivation ─────────────────────────────────────────────

    test("secp256k1 3-step derivation from known seed produces expected public key") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            it.publicKey.value shouldBe "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435"
        }
    }

    test("secp256k1 public key starts with 02 or 03") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            val prefix = it.publicKey.value.substring(0, 2)
            (prefix == "02" || prefix == "03") shouldBe true
        }
    }

    test("secp256k1 derivation sets correct algorithm") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        keyPair.use {
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
        }
    }

    // ── KeyPair.close() ─────────────────────────────────────────────────

    test("KeyPair.close() zeros private key bytes") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.close()
        keyPair.privateKeyBytes.all { it == 0.toByte() } shouldBe true
    }

    // ── KeyPair.toString() ──────────────────────────────────────────────

    test("KeyPair.toString() does not contain private key bytes") {
        val keyPair = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        keyPair.use {
            val str = it.toString()
            str shouldNotContain "privateKey"
            // Should contain publicKey and algorithm info
            str shouldStartWith "KeyPair("
        }
    }

    // ── KeyPair.equals() ────────────────────────────────────────────────

    test("KeyPair.equals() returns true for identical key pairs") {
        val kp1 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        val kp2 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        kp1.use { a ->
            kp2.use { b ->
                (a == b) shouldBe true
            }
        }
    }

    test("KeyPair.equals() returns false for different key pairs") {
        val kp1 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Ed25519, provider)
        val kp2 = KeyDerivation.deriveKeyPair(testSeed, KeyAlgorithm.Secp256k1, provider)
        kp1.use { a ->
            kp2.use { b ->
                (a == b) shouldBe false
            }
        }
    }

    // ── generateSeed() ──────────────────────────────────────────────────

    test("generateSeed() produces 16 bytes") {
        val seed = KeyDerivation.generateSeed(provider)
        seed.size shouldBe 16
    }

    test("generateSeed() does not produce all zeros") {
        val seed = KeyDerivation.generateSeed(provider)
        seed shouldNotBe ByteArray(16)
    }

    // ── Validation ──────────────────────────────────────────────────────

    test("deriveKeyPair rejects non-16-byte seed") {
        val result =
            runCatching {
                KeyDerivation.deriveKeyPair(ByteArray(15), KeyAlgorithm.Ed25519, provider)
            }
        result.isFailure shouldBe true
    }
})
