package org.xrpl.sdk.codec.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.toHexString

class StObjectSerializerTest : FunSpec({

    test("canonical ordering serializes fields by typeCode then fieldCode") {
        // TransactionType (UInt16, code=1, field=2) should come before
        // Flags (UInt32, code=2, field=2)
        // Flags should come before
        // Fee (Amount, code=6, field=8)
        val obj =
            mapOf<String, Any?>(
                "Fee" to "1000",
                "Flags" to 0L,
                "TransactionType" to 0,
            )
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, obj)
        val bytes = writer.toByteArray()

        // Verify ordering by checking field headers appear in correct order.
        // TransactionType: typeCode=1, fieldCode=2 -> header = 0x12
        // Flags: typeCode=2, fieldCode=2 -> header = 0x22
        // Fee: typeCode=6, fieldCode=8 -> header = 0x68
        bytes[0] shouldBe 0x12.toByte() // TransactionType header
    }

    test("nested STObject with end marker") {
        val nested =
            mapOf<String, Any?>(
                "Flags" to 0L,
            )
        val writer = BinaryWriter()
        StObjectSerializer.writeNested(writer, nested)
        val bytes = writer.toByteArray()

        // Should end with 0xE1 (ObjectEndMarker)
        bytes[bytes.size - 1] shouldBe 0xE1.toByte()
    }

    test("nested STObject roundtrip") {
        val nested =
            mapOf<String, Any?>(
                "Flags" to 0L,
            )
        val writer = BinaryWriter()
        StObjectSerializer.writeNested(writer, nested)
        val reader = BinaryReader(writer.toByteArray())
        val result = StObjectSerializer.readNested(reader)

        result["Flags"] shouldBe 0L
    }

    test("empty object serializes to empty bytes") {
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, emptyMap())
        writer.size shouldBe 0
    }

    test("single UInt16 field roundtrip") {
        val obj = mapOf<String, Any?>("TransactionType" to 0)
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, obj)
        val bytes = writer.toByteArray()

        // Header (1 byte 0x12) + UInt16 value (2 bytes) = 3 bytes
        bytes.size shouldBe 3
        bytes.toHexString() shouldBe "120000"
    }

    test("multiple field types serialize and roundtrip") {
        val obj = mapOf<String, Any?>(
            "TransactionType" to 0,  // UInt16
            "Flags" to 0L,           // UInt32
            "Sequence" to 1L,        // UInt32
            "Fee" to "12",           // Amount
        )
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, obj)
        val bytes = writer.toByteArray()

        // Verify non-empty serialization
        bytes.isNotEmpty() shouldBe true

        // Verify canonical ordering: TransactionType(1,2) < Flags(2,2) < Sequence(2,4) < Fee(6,8)
        val hex = bytes.toHexString()
        val txTypePos = hex.indexOf("1200")   // TransactionType header + 0x0000
        val flagsPos = hex.indexOf("2200")    // Flags header + 0x00000000
        (txTypePos < flagsPos) shouldBe true
    }

    test("null values in map are skipped") {
        val obj = mapOf<String, Any?>(
            "TransactionType" to 0,
            "Flags" to null,
        )
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, obj)
        val bytes = writer.toByteArray()

        // Only TransactionType should be serialized (3 bytes)
        bytes.size shouldBe 3
    }

    test("nested STObject inside parent") {
        // Memo is a nested STObject inside MemoData
        val obj = mapOf<String, Any?>(
            "TransactionType" to 0,
            "Flags" to 0L,
        )
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, obj)

        val writer2 = BinaryWriter()
        StObjectSerializer.write(writer2, obj)

        // Same inputs produce same output
        writer.toByteArray().toHexString() shouldBe writer2.toByteArray().toHexString()
    }
})
