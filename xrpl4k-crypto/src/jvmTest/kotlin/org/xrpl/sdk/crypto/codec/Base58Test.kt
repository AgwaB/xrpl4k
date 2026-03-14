@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for pure Base58 encode/decode (no checksum).
 */
class Base58Test : FunSpec({

    test("encode empty byte array produces empty string") {
        Base58.encode(ByteArray(0)) shouldBe ""
    }

    test("decode empty string produces empty byte array") {
        Base58.decode("") shouldBe ByteArray(0)
    }

    test("encode preserves leading zero bytes as 'r' characters") {
        // In XRP alphabet, 'r' is index 0 (the leading-zero character)
        val input = byteArrayOf(0, 0, 1)
        val encoded = Base58.encode(input)
        encoded.startsWith("rr") shouldBe true
        Base58.decode(encoded) shouldBe input
    }

    test("encode/decode roundtrip for known bytes") {
        val input = byteArrayOf(1, 2, 3, 4, 5)
        val encoded = Base58.encode(input)
        Base58.decode(encoded) shouldBe input
    }

    test("encode/decode roundtrip for all-zeros") {
        val input = ByteArray(20) // 20 zero bytes
        val encoded = Base58.encode(input)
        // Should be 20 'r' characters (the zero-byte representation)
        encoded.length shouldBe 20
        encoded.all { it == 'r' } shouldBe true
        Base58.decode(encoded) shouldBe input
    }

    test("encode/decode roundtrip for all 0xFF bytes") {
        val input = ByteArray(16) { 0xFF.toByte() }
        val encoded = Base58.encode(input)
        Base58.decode(encoded) shouldBe input
    }

    test("decode invalid character throws") {
        val result = runCatching { Base58.decode("invalid0character") }
        result.isFailure shouldBe true
        (result.exceptionOrNull() is IllegalArgumentException) shouldBe true
    }
})
