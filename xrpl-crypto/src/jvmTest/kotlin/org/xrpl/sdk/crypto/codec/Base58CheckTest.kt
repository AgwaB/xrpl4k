@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for Base58Check encoding/decoding with double-SHA-256 checksums.
 */
class Base58CheckTest : FunSpec({

    val provider = platformCryptoProvider()

    test("encode then decodeRaw roundtrip") {
        val prefix = byteArrayOf(0x00)
        val payload = ByteArray(20) { it.toByte() }
        val encoded = Base58Check.encode(prefix, payload, provider)
        val decoded = Base58Check.decodeRaw(encoded, provider)
        decoded shouldBe prefix + payload
    }

    test("encode then decodeChecked roundtrip") {
        val prefix = byteArrayOf(0x00)
        val payload = ByteArray(20) { it.toByte() }
        val encoded = Base58Check.encode(prefix, payload, provider)
        val (decodedPrefix, decodedPayload) =
            Base58Check.decodeChecked(
                encoded,
                listOf(prefix),
                expectedPayloadLength = 20,
                provider,
            )
        decodedPrefix shouldBe prefix
        decodedPayload shouldBe payload
    }

    test("checksum mismatch throws IllegalArgumentException") {
        val prefix = byteArrayOf(0x00)
        val payload = ByteArray(20) { it.toByte() }
        val encoded = Base58Check.encode(prefix, payload, provider)
        // Corrupt last character
        val corrupted = encoded.dropLast(1) + (if (encoded.last() == 'r') 'p' else 'r')
        val result = runCatching { Base58Check.decodeRaw(corrupted, provider) }
        result.isFailure shouldBe true
        (result.exceptionOrNull() is IllegalArgumentException) shouldBe true
    }

    test("decodeChecked with multi-byte prefix") {
        val prefix = byteArrayOf(0x01, 0xE1.toByte(), 0x4B)
        val payload = ByteArray(16) { (it + 1).toByte() }
        val encoded = Base58Check.encode(prefix, payload, provider)
        val (decodedPrefix, decodedPayload) =
            Base58Check.decodeChecked(
                encoded,
                // try 1-byte first, then 3-byte
                listOf(byteArrayOf(0x21), prefix),
                expectedPayloadLength = 16,
                provider,
            )
        decodedPrefix shouldBe prefix
        decodedPayload shouldBe payload
    }

    test("decodeChecked with wrong prefix throws") {
        val prefix = byteArrayOf(0x00)
        val payload = ByteArray(20) { it.toByte() }
        val encoded = Base58Check.encode(prefix, payload, provider)
        val result =
            runCatching {
                Base58Check.decodeChecked(
                    encoded,
                    // wrong prefix
                    listOf(byteArrayOf(0x01)),
                    expectedPayloadLength = 20,
                    provider,
                )
            }
        result.isFailure shouldBe true
    }

    test("decodeRaw on too-short input throws") {
        val result = runCatching { Base58Check.decodeRaw("rr", provider) }
        result.isFailure shouldBe true
        (result.exceptionOrNull() is IllegalArgumentException) shouldBe true
    }

    test("xrpl.js reference: encode '123456789' with version 0") {
        // From xrpl.js test: encode stringToBytes('123456789') with version [0]
        // "123456789" as ASCII bytes = 0x31 0x32 0x33 0x34 0x35 0x36 0x37 0x38 0x39
        val payload = "123456789".toByteArray(Charsets.US_ASCII)
        val encoded = Base58Check.encode(byteArrayOf(0x00), payload, provider)
        encoded shouldBe "rnaC7gW34M77Kneb78s"
    }
})
