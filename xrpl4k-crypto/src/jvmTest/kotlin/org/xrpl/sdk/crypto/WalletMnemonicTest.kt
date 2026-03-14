package org.xrpl.sdk.crypto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.KeyAlgorithm

class WalletMnemonicTest : FunSpec({
    val provider = platformCryptoProvider()

    // Well-known BIP39 test vector: 12-word mnemonic (all "abandon" x11 + "about")
    val testMnemonic12 =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    // 24-word mnemonic: "abandon" x23 + "art"
    val testMnemonic24 =
        "abandon abandon abandon abandon abandon abandon abandon abandon " +
            "abandon abandon abandon abandon abandon abandon abandon abandon " +
            "abandon abandon abandon abandon abandon abandon abandon art"

    test("fromMnemonic with 12-word mnemonic produces a valid wallet") {
        val wallet = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        wallet.use {
            it.address.value shouldStartWith "r"
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
            val prefix = it.publicKey.value.substring(0, 2)
            (prefix == "02" || prefix == "03") shouldBe true
        }
    }

    test("fromMnemonic is deterministic") {
        val wallet1 = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        val wallet2 = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        wallet1.use { w1 ->
            wallet2.use { w2 ->
                w1.address shouldBe w2.address
                w1.publicKey shouldBe w2.publicKey
            }
        }
    }

    test("fromMnemonic with 24-word mnemonic produces a valid wallet") {
        val wallet = Wallet.fromMnemonic(testMnemonic24, provider = provider)
        wallet.use {
            it.address.value shouldStartWith "r"
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
        }
    }

    test("fromMnemonic with 24-word mnemonic differs from 12-word") {
        val wallet12 = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        val wallet24 = Wallet.fromMnemonic(testMnemonic24, provider = provider)
        wallet12.use { w1 ->
            wallet24.use { w2 ->
                w1.address shouldNotBe w2.address
            }
        }
    }

    test("fromMnemonic with invalid mnemonic throws IllegalArgumentException") {
        shouldThrow<IllegalArgumentException> {
            Wallet.fromMnemonic("invalid words that are not bip39", provider = provider)
        }
    }

    test("fromMnemonic with wrong word count throws IllegalArgumentException") {
        shouldThrow<IllegalArgumentException> {
            Wallet.fromMnemonic("abandon abandon abandon", provider = provider)
        }
    }

    test("fromMnemonic with custom derivation path produces different wallet") {
        val walletDefault = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        val walletCustom = Wallet.fromMnemonic(
            testMnemonic12,
            derivationPath = "m/44'/144'/1'/0/0",
            provider = provider,
        )
        walletDefault.use { w1 ->
            walletCustom.use { w2 ->
                w1.address shouldNotBe w2.address
            }
        }
    }

    test("fromMnemonic with passphrase changes the derived address") {
        val walletNoPass = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        val walletWithPass = Wallet.fromMnemonic(
            testMnemonic12,
            passphrase = "my secret passphrase",
            provider = provider,
        )
        walletNoPass.use { w1 ->
            walletWithPass.use { w2 ->
                w1.address shouldNotBe w2.address
            }
        }
    }

    test("fromMnemonic cross-validation against xrpl.js expected values") {
        // Expected values from xrpl.js using @scure/bip39 + @scure/bip32:
        //   mnemonic: "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        //   derivationPath: "m/44'/144'/0'/0/0"
        //   PublicKey: 031D68BC1A142E6766B2BDFB006CCFE135EF2E0E2E94ABB5CF5C9AB6104776FBAE
        //   Address: rHsMGQEkVNJmpGWs8XUBoTBiAAbwxZN5v3
        val wallet = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        wallet.use {
            println("[CROSS-VALIDATION] PublicKey: ${it.publicKey.value}")
            println("[CROSS-VALIDATION] Address: ${it.address.value}")
            it.publicKey.value shouldBe "031D68BC1A142E6766B2BDFB006CCFE135EF2E0E2E94ABB5CF5C9AB6104776FBAE"
            it.address.value shouldBe "rHsMGQEkVNJmpGWs8XUBoTBiAAbwxZN5v3"
        }
    }

    test("fromMnemonic sign/verify roundtrip") {
        val wallet = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        wallet.use {
            val message = provider.sha512Half("Hello BIP39".encodeToByteArray())
            val signature = it.sign(message)
            it.verify(message, signature) shouldBe true
        }
    }

    test("fromMnemonic normalizes extra whitespace") {
        val messy = "  abandon  abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon  about  "
        val wallet1 = Wallet.fromMnemonic(testMnemonic12, provider = provider)
        val wallet2 = Wallet.fromMnemonic(messy, provider = provider)
        wallet1.use { w1 ->
            wallet2.use { w2 ->
                w1.address shouldBe w2.address
            }
        }
    }
})
