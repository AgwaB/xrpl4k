package org.xrpl.sdk.codec.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class CurrencySerializerTest : FunSpec({

    // ── encode ────────────────────────────────────────────────────────────

    test("XRP encodes to 20 zero bytes") {
        val bytes = CurrencySerializer.encode("XRP")
        bytes.size shouldBe 20
        bytes.all { it == 0.toByte() } shouldBe true
    }

    test("3-char code places ASCII at bytes 12-14") {
        val bytes = CurrencySerializer.encode("USD")
        bytes.size shouldBe 20
        // Bytes 0-11 and 15-19 must be zero
        (0 until 12).all { bytes[it] == 0.toByte() } shouldBe true
        (15 until 20).all { bytes[it] == 0.toByte() } shouldBe true
        // Bytes 12-14 = 'U','S','D'
        bytes[12] shouldBe 'U'.code.toByte()
        bytes[13] shouldBe 'S'.code.toByte()
        bytes[14] shouldBe 'D'.code.toByte()
    }

    test("EUR encodes correctly") {
        val bytes = CurrencySerializer.encode("EUR")
        bytes[12] shouldBe 'E'.code.toByte()
        bytes[13] shouldBe 'U'.code.toByte()
        bytes[14] shouldBe 'R'.code.toByte()
    }

    test("40-char hex non-standard currency encodes to raw bytes") {
        val hex = "0000000000000000000000005553440000000001"
        val bytes = CurrencySerializer.encode(hex)
        bytes.size shouldBe 20
        bytes[19] shouldBe 0x01.toByte()
    }

    test("2-char currency throws") {
        shouldThrow<IllegalArgumentException> {
            CurrencySerializer.encode("US")
        }
    }

    test("4-char currency throws") {
        shouldThrow<IllegalArgumentException> {
            CurrencySerializer.encode("USDT")
        }
    }

    // ── decode ────────────────────────────────────────────────────────────

    test("all-zero bytes decode to XRP") {
        val bytes = ByteArray(20)
        CurrencySerializer.decode(bytes) shouldBe "XRP"
    }

    test("standard bytes decode to 3-char code") {
        val bytes = ByteArray(20)
        bytes[12] = 'U'.code.toByte()
        bytes[13] = 'S'.code.toByte()
        bytes[14] = 'D'.code.toByte()
        CurrencySerializer.decode(bytes) shouldBe "USD"
    }

    test("non-standard bytes decode to lowercase hex") {
        val bytes = ByteArray(20)
        bytes[19] = 0x01.toByte() // non-zero at position 19 → non-standard
        val result = CurrencySerializer.decode(bytes)
        result.length shouldBe 40
        result shouldBe "0000000000000000000000000000000000000001"
    }

    test("decode with wrong size throws") {
        shouldThrow<IllegalArgumentException> {
            CurrencySerializer.decode(ByteArray(19))
        }
    }

    // ── encode/decode roundtrip ───────────────────────────────────────────

    test("XRP roundtrip") {
        val bytes = CurrencySerializer.encode("XRP")
        CurrencySerializer.decode(bytes) shouldBe "XRP"
    }

    test("USD roundtrip") {
        val bytes = CurrencySerializer.encode("USD")
        CurrencySerializer.decode(bytes) shouldBe "USD"
    }

    test("non-standard hex roundtrip") {
        val hex = "0102030405060708090a0b0c0d0e0f1011121314"
        val bytes = CurrencySerializer.encode(hex)
        CurrencySerializer.decode(bytes) shouldBe hex
    }

    // ── write/read via BinaryWriter/Reader ────────────────────────────────

    test("write and read XRP roundtrip") {
        val writer = BinaryWriter()
        CurrencySerializer.write(writer, "XRP")
        val reader = BinaryReader(writer.toByteArray())
        CurrencySerializer.read(reader) shouldBe "XRP"
    }

    test("write and read USD roundtrip") {
        val writer = BinaryWriter()
        CurrencySerializer.write(writer, "USD")
        val reader = BinaryReader(writer.toByteArray())
        CurrencySerializer.read(reader) shouldBe "USD"
    }

    test("write produces exactly 20 bytes") {
        val writer = BinaryWriter()
        CurrencySerializer.write(writer, "EUR")
        writer.size shouldBe 20
    }
})
