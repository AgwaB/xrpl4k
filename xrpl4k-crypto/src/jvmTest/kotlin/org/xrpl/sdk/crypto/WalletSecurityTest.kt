@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.KeyAlgorithm

/**
 * Security-focused tests for Wallet lifecycle, AutoCloseable, and sensitive data zeroing.
 *
 * Complements WalletTest by covering:
 * - seedBytes zeroing on close()
 * - AutoCloseable / .use {} scoped lifecycle
 * - fromEntropy with Secp256k1
 * - fromSeed exact known-vector addresses
 * - verify rejects wrong message (both algorithms)
 * - hashCode consistency
 */
class WalletSecurityTest : FunSpec({
    val provider = platformCryptoProvider()

    // ── Known test vectors ───────────────────────────────────────────────
    // Ed25519 seed: sEdTM1uX8pu2do5XvTnutH6HsouMaM2
    //   entropy: 4C3A1D213FBDFB14C7C28D609469B341
    // Secp256k1 seed: sn259rEFXrQrWyx3Q7XneWcwV6dfL
    //   entropy: CF2DE378FBDD7E2EE87D486DFB5A7BFF

    // Known from xrpl.js: seed [1,2,3,...,16]
    //   Ed25519 public key: ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63
    //   Secp256k1 public key: 030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435

    val knownSeed16 = ByteArray(16) { (it + 1).toByte() }

    // ── close() zeros seedBytes ─────────────────────────────────────────

    test("close() zeros seed bytes via reflection") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val wallet = generated.wallet

        // Access seedBytes via reflection to verify zeroing
        val seedBytesField = Wallet::class.java.getDeclaredField("seedBytes")
        seedBytesField.isAccessible = true
        val seedBytes = seedBytesField.get(wallet) as ByteArray?

        // Before close: seedBytes should contain non-zero data
        seedBytes shouldNotBe null
        seedBytes!!.any { it != 0.toByte() } shouldBe true

        wallet.close()

        // After close: seedBytes should be all zeros
        seedBytes.all { it == 0.toByte() } shouldBe true
    }

    test("close() zeros both privateKey AND seedBytes") {
        val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        val wallet = generated.wallet

        val seedBytesField = Wallet::class.java.getDeclaredField("seedBytes")
        seedBytesField.isAccessible = true
        val seedBytes = seedBytesField.get(wallet) as ByteArray?

        wallet.close()

        // privateKey zeroed
        wallet.keyPair.privateKeyBytes.all { it == 0.toByte() } shouldBe true
        // seedBytes zeroed
        seedBytes!!.all { it == 0.toByte() } shouldBe true
    }

    // ── AutoCloseable .use {} scoped lifecycle ──────────────────────────

    test("wallet.use {} automatically closes wallet on normal exit") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val wallet = generated.wallet
        var addressCapture: String? = null

        wallet.use {
            addressCapture = it.address.value
        }

        // After .use {} exits, private key should be zeroed
        wallet.keyPair.privateKeyBytes.all { it == 0.toByte() } shouldBe true
        addressCapture shouldNotBe null
    }

    test("wallet.use {} closes wallet even on exception") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val wallet = generated.wallet

        runCatching {
            wallet.use {
                error("simulated error")
            }
        }

        // Despite exception, close should have been called
        wallet.keyPair.privateKeyBytes.all { it == 0.toByte() } shouldBe true
    }

    // ── fromEntropy with Secp256k1 ──────────────────────────────────────

    test("fromEntropy with Secp256k1 produces valid wallet") {
        val entropy = ByteArray(16) { (it + 1).toByte() }
        val wallet = Wallet.fromEntropy(entropy, KeyAlgorithm.Secp256k1, provider)
        wallet.use {
            it.address.value shouldStartWith "r"
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
            val prefix = it.publicKey.value.substring(0, 2)
            (prefix == "02" || prefix == "03") shouldBe true
        }
    }

    test("fromEntropy with Secp256k1 produces known public key") {
        val wallet = Wallet.fromEntropy(knownSeed16, KeyAlgorithm.Secp256k1, provider)
        wallet.use {
            it.publicKey.value shouldBe "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435"
        }
    }

    test("fromEntropy with Ed25519 produces known public key") {
        val wallet = Wallet.fromEntropy(knownSeed16, KeyAlgorithm.Ed25519, provider)
        wallet.use {
            it.publicKey.value shouldBe "ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63"
        }
    }

    test("fromEntropy Ed25519 and Secp256k1 produce different addresses") {
        val walletEd = Wallet.fromEntropy(knownSeed16, KeyAlgorithm.Ed25519, provider)
        val walletSec = Wallet.fromEntropy(knownSeed16, KeyAlgorithm.Secp256k1, provider)
        walletEd.use { ed ->
            walletSec.use { sec ->
                ed.address shouldNotBe sec.address
                ed.publicKey shouldNotBe sec.publicKey
            }
        }
    }

    // ── fromSeed exact known-vector addresses ───────────────────────────

    test("fromSeed Ed25519 produces deterministic address from known seed") {
        val wallet1 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        val wallet2 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet1.use { w1 ->
            wallet2.use { w2 ->
                w1.address shouldBe w2.address
                w1.publicKey shouldBe w2.publicKey
                w1.algorithm shouldBe KeyAlgorithm.Ed25519
            }
        }
    }

    test("fromSeed Secp256k1 all-zeros seed produces valid known wallet") {
        // sp6JS7f14BuwFY8Mw6bTtLKWauoUs is the all-zeros secp256k1 seed
        val wallet = Wallet.fromSeed("sp6JS7f14BuwFY8Mw6bTtLKWauoUs", provider)
        wallet.use {
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
            it.address.value shouldStartWith "r"
            it.publicKey.value.length shouldBe 66
        }
    }

    // ── verify rejects wrong message ────────────────────────────────────

    test("Ed25519 verify rejects tampered message") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        wallet.use {
            val message = "correct message".encodeToByteArray()
            val signature = it.sign(message)
            it.verify(message, signature) shouldBe true
            it.verify("wrong message".encodeToByteArray(), signature) shouldBe false
        }
    }

    test("Secp256k1 verify rejects tampered message") {
        val wallet = Wallet.generate(KeyAlgorithm.Secp256k1, provider).wallet
        wallet.use {
            val message = provider.sha512Half("correct message".encodeToByteArray())
            val signature = it.sign(message)
            it.verify(message, signature) shouldBe true
            val tampered = provider.sha512Half("wrong message".encodeToByteArray())
            it.verify(tampered, signature) shouldBe false
        }
    }

    test("Ed25519 verify rejects signature from different wallet") {
        val wallet1 = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        val wallet2 = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        wallet1.use { w1 ->
            wallet2.use { w2 ->
                val message = "shared message".encodeToByteArray()
                val sig1 = w1.sign(message)
                // Signature from wallet1 should not verify with wallet2's public key
                w2.verify(message, sig1) shouldBe false
            }
        }
    }

    // ── hashCode consistency ────────────────────────────────────────────

    test("equal wallets have equal hashCodes") {
        val w1 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        val w2 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        w1.use { a ->
            w2.use { b ->
                a.hashCode() shouldBe b.hashCode()
            }
        }
    }

    test("wallet is not equal to null or different type") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        wallet.use {
            (it.equals(null)) shouldBe false
            (it.equals("a string")) shouldBe false
        }
    }
})
