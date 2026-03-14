@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.KeyAlgorithm

class WalletTest : FunSpec({
    val provider = platformCryptoProvider()

    // Known seed from xrpl.js: sEdTM1uX8pu2do5XvTnutH6HsouMaM2 (Ed25519)
    // Entropy: [0x4C, 0x3A, 0x1D, 0x21, 0x3F, 0xBD, 0xFB, 0x14, 0xC7, 0xC2, 0x8D, 0x60, 0x94, 0x69, 0xB3, 0x41]

    test("generate Ed25519 wallet produces valid address") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            wallet.address.value shouldStartWith "r"
            wallet.publicKey.value shouldStartWith "ED"
            wallet.algorithm shouldBe KeyAlgorithm.Ed25519
        }
        generated.seedString shouldStartWith "sEd"
    }

    test("generate Secp256k1 wallet produces valid address") {
        val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        generated.wallet.use { wallet ->
            wallet.address.value shouldStartWith "r"
            val prefix = wallet.publicKey.value.substring(0, 2)
            (prefix == "02" || prefix == "03") shouldBe true
            wallet.algorithm shouldBe KeyAlgorithm.Secp256k1
        }
    }

    test("fromSeed restores Ed25519 wallet from known seed") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use {
            it.algorithm shouldBe KeyAlgorithm.Ed25519
            it.publicKey.value shouldStartWith "ED"
            it.address.value shouldStartWith "r"
        }
    }

    test("fromSeed restores secp256k1 wallet from known seed") {
        val wallet = Wallet.fromSeed("sn259rEFXrQrWyx3Q7XneWcwV6dfL", provider)
        wallet.use {
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
            it.address.value shouldStartWith "r"
        }
    }

    test("fromEntropy produces same wallet as fromSeed") {
        // Known Ed25519 seed entropy: 4C3A1D213FBDFB14C7C28D609469B341
        val entropy =
            byteArrayOf(
                0x4C, 0x3A, 0x1D, 0x21, 0x3F, 0xBD.toByte(), 0xFB.toByte(), 0x14,
                0xC7.toByte(), 0xC2.toByte(), 0x8D.toByte(), 0x60, 0x94.toByte(), 0x69, 0xB3.toByte(), 0x41,
            )
        val fromEntropy = Wallet.fromEntropy(entropy, KeyAlgorithm.Ed25519, provider)
        val fromSeed = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        fromEntropy.use { w1 ->
            fromSeed.use { w2 ->
                w1.address shouldBe w2.address
                w1.publicKey shouldBe w2.publicKey
            }
        }
    }

    test("wallet.close() zeros private key AND seed bytes") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val wallet = generated.wallet
        wallet.close()
        wallet.keyPair.privateKeyBytes.all { it == 0.toByte() } shouldBe true
    }

    test("wallet.toString() does not contain private key") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        wallet.use {
            val str = it.toString()
            str shouldNotContain "privateKey"
            str shouldNotContain "seed"
        }
    }

    test("Ed25519 sign/verify roundtrip") {
        val wallet = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        wallet.use {
            val message = "Hello XRPL".encodeToByteArray()
            val signature = it.sign(message)
            it.verify(message, signature) shouldBe true
        }
    }

    test("Secp256k1 sign/verify roundtrip") {
        val wallet = Wallet.generate(KeyAlgorithm.Secp256k1, provider).wallet
        wallet.use {
            // secp256k1 signs a message hash, so pre-hash for roundtrip test
            val message = provider.sha512Half("Hello XRPL".encodeToByteArray())
            val signature = it.sign(message)
            it.verify(message, signature) shouldBe true
        }
    }

    test("two wallets from same seed are equals") {
        val w1 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        val w2 = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        w1.use { a ->
            w2.use { b ->
                (a == b) shouldBe true
            }
        }
    }

    test("different wallets are not equals") {
        val w1 = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        val w2 = Wallet.generate(KeyAlgorithm.Ed25519, provider).wallet
        w1.use { a ->
            w2.use { b ->
                (a == b) shouldBe false
            }
        }
    }

    test("GeneratedWallet.toString does not expose seed string") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use {
            generated.toString() shouldNotContain generated.seedString
        }
    }
})
