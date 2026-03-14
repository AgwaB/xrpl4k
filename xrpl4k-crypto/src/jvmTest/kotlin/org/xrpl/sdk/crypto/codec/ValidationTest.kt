package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

class ValidationTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── Classic address validation ──────────────────────────────────────

    test("valid classic address returns true") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            isValidClassicAddress(wallet.address.value, provider) shouldBe true
        }
    }

    test("valid secp256k1 classic address returns true") {
        val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        generated.wallet.use { wallet ->
            isValidClassicAddress(wallet.address.value, provider) shouldBe true
        }
    }

    test("invalid string returns false for classic address") {
        isValidClassicAddress("notAnAddress", provider) shouldBe false
    }

    test("empty string returns false for classic address") {
        isValidClassicAddress("", provider) shouldBe false
    }

    test("classic address with modified last char returns false (checksum mismatch)") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val address = wallet.address.value
            // Flip the last character to break the checksum
            val lastChar = address.last()
            val replacement = if (lastChar == 'r') 's' else 'r'
            val corrupted = address.dropLast(1) + replacement
            isValidClassicAddress(corrupted, provider) shouldBe false
        }
    }

    // ── X-Address validation ────────────────────────────────────────────

    test("valid X-Address returns true") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val xAddress = XAddressCodec.encode(wallet.address, tag = null, isTest = false, provider)
            isValidXAddress(xAddress.value, provider) shouldBe true
        }
    }

    test("valid X-Address with tag returns true") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val xAddress = XAddressCodec.encode(wallet.address, tag = 12345u, isTest = false, provider)
            isValidXAddress(xAddress.value, provider) shouldBe true
        }
    }

    test("invalid string returns false for X-Address") {
        isValidXAddress("notAnXAddress", provider) shouldBe false
    }

    test("empty string returns false for X-Address") {
        isValidXAddress("", provider) shouldBe false
    }

    // ── isValidAddress (classic or X) ───────────────────────────────────

    test("isValidAddress accepts classic address") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            isValidAddress(wallet.address.value, provider) shouldBe true
        }
    }

    test("isValidAddress accepts X-Address") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val xAddress = XAddressCodec.encode(wallet.address, tag = null, isTest = false, provider)
            isValidAddress(xAddress.value, provider) shouldBe true
        }
    }

    test("isValidAddress rejects invalid string") {
        isValidAddress("garbage", provider) shouldBe false
    }

    test("isValidAddress rejects empty string") {
        isValidAddress("", provider) shouldBe false
    }

    // ── Secret/seed validation ──────────────────────────────────────────

    test("valid Ed25519 seed returns true") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        isValidSecret(generated.seedString, provider) shouldBe true
    }

    test("valid secp256k1 seed returns true") {
        val generated = Wallet.generate(KeyAlgorithm.Secp256k1, provider)
        isValidSecret(generated.seedString, provider) shouldBe true
    }

    test("invalid seed returns false") {
        isValidSecret("notASeed", provider) shouldBe false
    }

    test("empty string returns false for secret") {
        isValidSecret("", provider) shouldBe false
    }

    test("seed with modified last char returns false (checksum mismatch)") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val seed = generated.seedString
        val lastChar = seed.last()
        val replacement = if (lastChar == 's') 't' else 's'
        val corrupted = seed.dropLast(1) + replacement
        isValidSecret(corrupted, provider) shouldBe false
    }

    // ── Well-known addresses ─────────────────────────────────────────

    test("well-known genesis address is a valid classic address") {
        isValidClassicAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", provider) shouldBe true
    }

    test("well-known genesis address is not a valid X-Address") {
        isValidXAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", provider) shouldBe false
    }

    test("isValidAddress accepts well-known genesis address") {
        isValidAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", provider) shouldBe true
    }

    // ── X-Address with testnet flag ──────────────────────────────────

    test("testnet X-Address is valid") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            val xAddress = XAddressCodec.encode(wallet.address, tag = null, isTest = true, provider)
            isValidXAddress(xAddress.value, provider) shouldBe true
        }
    }

    test("classic address is not a valid X-Address") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        generated.wallet.use { wallet ->
            isValidXAddress(wallet.address.value, provider) shouldBe false
        }
    }

    // ── Edge cases ───────────────────────────────────────────────────

    test("whitespace-only string returns false for classic address") {
        isValidClassicAddress("   ", provider) shouldBe false
    }

    test("whitespace-only string returns false for secret") {
        isValidSecret("   ", provider) shouldBe false
    }

    test("seed with corrupted middle characters returns false") {
        val generated = Wallet.generate(KeyAlgorithm.Ed25519, provider)
        val seed = generated.seedString
        val corrupted = seed.take(5) + "ZZZZ" + seed.drop(9)
        isValidSecret(corrupted, provider) shouldBe false
    }
})
