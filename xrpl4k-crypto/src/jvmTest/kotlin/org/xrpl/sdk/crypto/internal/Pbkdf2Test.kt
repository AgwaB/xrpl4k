@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.crypto.platformCryptoProvider

class Pbkdf2Test : FunSpec({

    val provider = platformCryptoProvider()

    // ── Output length ─────────────────────────────────────────────────────

    test("output length matches requested keyLength of 64") {
        val result =
            pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 1, 64, provider)
        result.size shouldBe 64
    }

    test("output length 32 bytes (less than one SHA-512 block)") {
        val result =
            pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 1, 32, provider)
        result.size shouldBe 32
    }

    test("output length 128 bytes (two SHA-512 blocks)") {
        val result =
            pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 1, 128, provider)
        result.size shouldBe 128
    }

    test("output length 1 byte") {
        val result =
            pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 1, 1, provider)
        result.size shouldBe 1
    }

    // ── Determinism ───────────────────────────────────────────────────────

    test("same inputs produce the same output") {
        val a = pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 2048, 64, provider)
        val b = pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 2048, 64, provider)
        a.contentEquals(b) shouldBe true
    }

    // ── Sensitivity ───────────────────────────────────────────────────────

    test("different passwords produce different outputs") {
        val a = pbkdf2HmacSha512("password1".encodeToByteArray(), "salt".encodeToByteArray(), 1, 64, provider)
        val b = pbkdf2HmacSha512("password2".encodeToByteArray(), "salt".encodeToByteArray(), 1, 64, provider)
        a.contentEquals(b) shouldBe false
    }

    test("different salts produce different outputs") {
        val a = pbkdf2HmacSha512("password".encodeToByteArray(), "salt1".encodeToByteArray(), 1, 64, provider)
        val b = pbkdf2HmacSha512("password".encodeToByteArray(), "salt2".encodeToByteArray(), 1, 64, provider)
        a.contentEquals(b) shouldBe false
    }

    test("more iterations produces a different output") {
        val a = pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 1, 64, provider)
        val b = pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 2, 64, provider)
        a.contentEquals(b) shouldBe false
    }

    test("empty password and salt are accepted") {
        val result = pbkdf2HmacSha512(ByteArray(0), ByteArray(0), 1, 64, provider)
        result.size shouldBe 64
    }

    // ── BIP39 compatibility ───────────────────────────────────────────────

    test("BIP39 parameters: 2048 iterations, 64-byte output") {
        // BIP39 derives seed via PBKDF2-HMAC-SHA512(mnemonic, "mnemonic"+passphrase, 2048, 64)
        val mnemonic =
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        val result =
            pbkdf2HmacSha512(
                mnemonic.encodeToByteArray(),
                "mnemonic".encodeToByteArray(),
                2048,
                64,
                provider,
            )
        result.size shouldBe 64
        // Result is non-zero (not all zeros)
        result.any { it != 0.toByte() } shouldBe true
    }

    test("two-block output: first 64 bytes differ from bytes 65-128") {
        val result =
            pbkdf2HmacSha512("password".encodeToByteArray(), "salt".encodeToByteArray(), 1, 128, provider)
        val block1 = result.copyOfRange(0, 64)
        val block2 = result.copyOfRange(64, 128)
        block1.contentEquals(block2) shouldBe false
    }
})
