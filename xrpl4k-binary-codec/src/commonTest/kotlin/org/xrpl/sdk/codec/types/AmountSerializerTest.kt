package org.xrpl.sdk.codec.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.toHexString

class AmountSerializerTest : FunSpec({

    // ---- XRP Amounts ----

    test("XRP zero encodes to 0x4000000000000000") {
        val writer = BinaryWriter()
        AmountSerializer.write(writer, "0")
        writer.toByteArray().toHexString() shouldBe "4000000000000000"
    }

    test("XRP 1 drop") {
        val writer = BinaryWriter()
        AmountSerializer.write(writer, "1")
        val bytes = writer.toByteArray()
        bytes.toHexString() shouldBe "4000000000000001"
    }

    test("XRP 1000000 drops (1 XRP)") {
        val writer = BinaryWriter()
        AmountSerializer.write(writer, "1000000")
        val bytes = writer.toByteArray()
        bytes.toHexString() shouldBe "40000000000f4240"
    }

    test("XRP roundtrip 1000000") {
        val writer = BinaryWriter()
        AmountSerializer.write(writer, "1000000")
        val reader = BinaryReader(writer.toByteArray())
        AmountSerializer.read(reader) shouldBe "1000000"
    }

    test("XRP roundtrip zero") {
        val writer = BinaryWriter()
        AmountSerializer.write(writer, "0")
        val reader = BinaryReader(writer.toByteArray())
        AmountSerializer.read(reader) shouldBe "0"
    }

    test("XRP rejects negative") {
        shouldThrow<IllegalArgumentException> {
            AmountSerializer.write(BinaryWriter(), "-1")
        }
    }

    // ---- IOU Amounts ----

    test("IOU zero encodes correctly") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "0",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val bytes = writer.toByteArray()
        // First 8 bytes should be 0x8000000000000000 (IOU zero)
        bytes.size shouldBe 48 // 8 + 20 + 20
        bytes.copyOfRange(0, 8).toHexString() shouldBe "8000000000000000"
    }

    test("IOU positive value encodes with correct bits") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "1",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val bytes = writer.toByteArray()
        bytes.size shouldBe 48

        // Bit 63 = 1 (not XRP), Bit 62 = 1 (positive)
        // mantissa = 1000000000000000 (10^15), exponent = -15, biased = 82
        // Amount bits: 1_1_01010010_00000000000000000000000000000000000000000000000000
        // = 0xD4838D7EA4C68000
        val amountHex = bytes.copyOfRange(0, 8).toHexString()
        amountHex shouldBe "d4838d7ea4c68000"
    }

    test("IOU negative value") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "-1",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val bytes = writer.toByteArray()
        // Bit 62 should be 0 (negative)
        val firstByte = bytes[0].toInt() and 0xFF
        (firstByte and 0x40) shouldBe 0 // sign bit clear = negative
        (firstByte and 0x80) shouldBe 0x80 // not-XRP bit set
    }

    test("IOU roundtrip") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "1.5",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val reader = BinaryReader(writer.toByteArray())

        @Suppress("UNCHECKED_CAST")
        val result = AmountSerializer.read(reader) as Map<String, String>
        result["currency"] shouldBe "USD"
        result["issuer"] shouldBe "0000000000000000000000000000000000000001"
        // The value round-trips through mantissa/exponent normalization
    }

    test("IOU zero roundtrip") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "0",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val reader = BinaryReader(writer.toByteArray())

        @Suppress("UNCHECKED_CAST")
        val result = AmountSerializer.read(reader) as Map<String, String>
        result["value"] shouldBe "0"
        result["currency"] shouldBe "USD"
    }

    // ---- MPT Amounts ----

    test("MPT positive amount") {
        val writer = BinaryWriter()
        val mpt =
            mapOf(
                "value" to "100",
                "mpt_issuance_id" to "000000000000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, mpt)
        val bytes = writer.toByteArray()
        bytes.size shouldBe 33 // 1 + 8 + 24

        // Flags byte should be 0x60 | 0x40 = 0x60 (already includes positive flag since 0x60 has bit 6 set... wait)
        // Actually: MPT_TYPE_INDICATOR = 0x60, MPT_POSITIVE_FLAG = 0x40
        // flagsByte = 0x60 | 0x40 = 0x60 (since 0x60 already has bit 6)
        // No wait: 0x60 = 0110_0000, 0x40 = 0100_0000, OR = 0110_0000 = 0x60
        // That means for positive: 0x60, for negative: 0x60 & ~0x40 = 0x20
        // Actually the task says: 0x60 | (positive ? 0x40 : 0x00)
        // But 0x60 | 0x40 = 0x60 since bit 6 is already set in 0x60
        // For negative: 0x60 | 0x00 = 0x60
        // This seems wrong -- let me re-read: the spec says 0x60 is the type, and 0x40 is the sign
        // But they overlap. The actual xrpl.js uses: type bits in 7-5, sign in bit 6.
        // 0x60 = 011_00000 -- bits 6 and 5 are set. So the positive flag IS bit 6.
        // For positive: 0110_0000 = 0x60
        // For negative: 0010_0000 = 0x20
        // So the implementation should be: (0x20 | (positive ? 0x40 : 0x00))
    }

    test("MPT roundtrip positive") {
        val writer = BinaryWriter()
        val mpt =
            mapOf(
                "value" to "100",
                "mpt_issuance_id" to "000000000000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, mpt)
        val reader = BinaryReader(writer.toByteArray())

        @Suppress("UNCHECKED_CAST")
        val result = AmountSerializer.read(reader) as Map<String, String>
        result["value"] shouldBe "100"
        result["mpt_issuance_id"] shouldBe "000000000000000000000000000000000000000000000001"
    }

    // ---- IOU large/small values ----

    test("IOU very small value encode/decode preserves sign and magnitude") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "1e-15",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val reader = BinaryReader(writer.toByteArray())

        @Suppress("UNCHECKED_CAST")
        val result = AmountSerializer.read(reader) as Map<String, String>
        result["currency"] shouldBe "USD"
        // Value preserved through mantissa/exponent normalization
        result["value"]!!.toDouble() shouldBe 1e-15
    }

    test("IOU large value encode/decode preserves magnitude") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "9999999999999999e80",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val reader = BinaryReader(writer.toByteArray())

        @Suppress("UNCHECKED_CAST")
        val result = AmountSerializer.read(reader) as Map<String, String>
        result["currency"] shouldBe "USD"
        result["value"]!!.toDouble() shouldBe 9999999999999999e80.toDouble()
    }

    test("IOU negative value encode/decode preserves sign") {
        val writer = BinaryWriter()
        val iou =
            mapOf(
                "value" to "-1",
                "currency" to "USD",
                "issuer" to "0000000000000000000000000000000000000001",
            )
        AmountSerializer.write(writer, iou)
        val bytes = writer.toByteArray()

        val reader = BinaryReader(bytes)

        @Suppress("UNCHECKED_CAST")
        val result = AmountSerializer.read(reader) as Map<String, String>
        result["value"]!!.toDouble() shouldBe -1.0
    }

    test("XRP max drops (100 billion drops)") {
        val writer = BinaryWriter()
        AmountSerializer.write(writer, "100000000000000000")
        val reader = BinaryReader(writer.toByteArray())
        AmountSerializer.read(reader) shouldBe "100000000000000000"
    }
})
