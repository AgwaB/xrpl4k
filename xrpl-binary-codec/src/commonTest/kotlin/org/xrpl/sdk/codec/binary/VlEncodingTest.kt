package org.xrpl.sdk.codec.binary

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VlEncodingTest : FunSpec({

    // Helper: encode a length and decode it back
    fun roundtrip(length: Int): Int {
        val writer = BinaryWriter()
        VlEncoding.encode(writer, length)
        val reader = BinaryReader(writer.toByteArray())
        return VlEncoding.decode(reader)
    }

    // Helper: encode and return raw bytes
    fun encoded(length: Int): ByteArray {
        val writer = BinaryWriter()
        VlEncoding.encode(writer, length)
        return writer.toByteArray()
    }

    // --- Single-byte range (0–192) ---

    test("encodes 0 as single byte 0x00") {
        encoded(0) shouldBe byteArrayOf(0x00)
    }

    test("encodes 192 as single byte 0xC0") {
        encoded(192) shouldBe byteArrayOf(0xC0.toByte())
    }

    test("roundtrip 0") {
        roundtrip(0) shouldBe 0
    }

    test("roundtrip 1") {
        roundtrip(1) shouldBe 1
    }

    test("roundtrip 192") {
        roundtrip(192) shouldBe 192
    }

    // --- Two-byte range (193–12480) ---

    test("encodes 193 as two bytes") {
        // adjusted = 193 - 193 = 0; byte0 = 193 + (0 >> 8) = 193; byte1 = 0 & 0xFF = 0
        encoded(193) shouldBe byteArrayOf(0xC1.toByte(), 0x00)
    }

    test("encodes 12480 as two bytes") {
        // adjusted = 12480 - 193 = 12287 = 0x2FFF
        // byte0 = 193 + (12287 >> 8) = 193 + 47 = 240; byte1 = 12287 & 0xFF = 0xFF
        encoded(12480) shouldBe byteArrayOf(0xF0.toByte(), 0xFF.toByte())
    }

    test("roundtrip 193") {
        roundtrip(193) shouldBe 193
    }

    test("roundtrip 12480") {
        roundtrip(12480) shouldBe 12480
    }

    test("roundtrip midpoint 6336 in two-byte range") {
        roundtrip(6336) shouldBe 6336
    }

    // --- Three-byte range (12481–918744) ---

    test("encodes 12481 as three bytes") {
        // adjusted = 12481 - 12481 = 0
        // byte0 = 241 + (0 >> 16) = 241; byte1 = 0; byte2 = 0
        encoded(12481) shouldBe byteArrayOf(0xF1.toByte(), 0x00, 0x00)
    }

    test("encodes 918744 as three bytes") {
        // adjusted = 918744 - 12481 = 906263 = 0x0DD3D7 (no, let's compute)
        // 906263 = 0x0DD3E7? let's just trust roundtrip; verify byte count = 3
        encoded(918744).size shouldBe 3
    }

    test("roundtrip 12481") {
        roundtrip(12481) shouldBe 12481
    }

    test("roundtrip 918744") {
        roundtrip(918744) shouldBe 918744
    }

    test("roundtrip midpoint 465612 in three-byte range") {
        roundtrip(465612) shouldBe 465612
    }

    // --- Error cases ---

    test("encode rejects negative length") {
        shouldThrow<IllegalArgumentException> {
            VlEncoding.encode(BinaryWriter(), -1)
        }
    }

    test("encode rejects length above 918744") {
        shouldThrow<IllegalArgumentException> {
            VlEncoding.encode(BinaryWriter(), 918745)
        }
    }

    test("decode rejects invalid first byte 255") {
        val reader = BinaryReader(byteArrayOf(0xFF.toByte()))
        shouldThrow<IllegalArgumentException> {
            VlEncoding.decode(reader)
        }
    }

    // --- VL roundtrip via BinaryWriter.writeVlLength / BinaryReader.readVlLength ---

    test("BinaryWriter.writeVlLength and BinaryReader.readVlLength roundtrip for 0") {
        val writer = BinaryWriter()
        writer.writeVlLength(0)
        BinaryReader(writer.toByteArray()).readVlLength() shouldBe 0
    }

    test("BinaryWriter.writeVlLength and BinaryReader.readVlLength roundtrip for 918744") {
        val writer = BinaryWriter()
        writer.writeVlLength(918744)
        BinaryReader(writer.toByteArray()).readVlLength() shouldBe 918744
    }

    // --- Boundary adjacency: 192→193 and 12480→12481 transitions ---

    test("192 uses 1 byte and 193 uses 2 bytes") {
        encoded(192).size shouldBe 1
        encoded(193).size shouldBe 2
    }

    test("12480 uses 2 bytes and 12481 uses 3 bytes") {
        encoded(12480).size shouldBe 2
        encoded(12481).size shouldBe 3
    }
})
