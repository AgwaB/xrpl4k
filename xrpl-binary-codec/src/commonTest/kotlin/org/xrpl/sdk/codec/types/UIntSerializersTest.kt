package org.xrpl.sdk.codec.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class UIntSerializersTest : FunSpec({

    // ---- UInt8 ----

    test("UInt8 roundtrip zero") {
        val writer = BinaryWriter()
        UInt8Serializer.write(writer, 0)
        val reader = BinaryReader(writer.toByteArray())
        UInt8Serializer.read(reader) shouldBe 0
    }

    test("UInt8 roundtrip max") {
        val writer = BinaryWriter()
        UInt8Serializer.write(writer, 255)
        val reader = BinaryReader(writer.toByteArray())
        UInt8Serializer.read(reader) shouldBe 255
    }

    test("UInt8 roundtrip mid-value") {
        val writer = BinaryWriter()
        UInt8Serializer.write(writer, 0xAB)
        val reader = BinaryReader(writer.toByteArray())
        UInt8Serializer.read(reader) shouldBe 0xAB
    }

    // ---- UInt16 ----

    test("UInt16 roundtrip zero") {
        val writer = BinaryWriter()
        UInt16Serializer.write(writer, 0)
        val reader = BinaryReader(writer.toByteArray())
        UInt16Serializer.read(reader) shouldBe 0
    }

    test("UInt16 roundtrip max") {
        val writer = BinaryWriter()
        UInt16Serializer.write(writer, 65535)
        val reader = BinaryReader(writer.toByteArray())
        UInt16Serializer.read(reader) shouldBe 65535
    }

    test("UInt16 roundtrip big-endian") {
        val writer = BinaryWriter()
        UInt16Serializer.write(writer, 0x1234)
        val bytes = writer.toByteArray()
        bytes shouldBe byteArrayOf(0x12, 0x34)
        val reader = BinaryReader(bytes)
        UInt16Serializer.read(reader) shouldBe 0x1234
    }

    // ---- UInt32 ----

    test("UInt32 roundtrip zero") {
        val writer = BinaryWriter()
        UInt32Serializer.write(writer, 0L)
        val reader = BinaryReader(writer.toByteArray())
        UInt32Serializer.read(reader) shouldBe 0L
    }

    test("UInt32 roundtrip max") {
        val writer = BinaryWriter()
        UInt32Serializer.write(writer, 0xFFFFFFFFL)
        val reader = BinaryReader(writer.toByteArray())
        UInt32Serializer.read(reader) shouldBe 0xFFFFFFFFL
    }

    test("UInt32 roundtrip exceeds Int.MAX_VALUE") {
        val value = 3_000_000_000L
        val writer = BinaryWriter()
        UInt32Serializer.write(writer, value)
        val reader = BinaryReader(writer.toByteArray())
        UInt32Serializer.read(reader) shouldBe value
    }

    // ---- UInt64 ----

    test("UInt64 roundtrip zero") {
        val writer = BinaryWriter()
        UInt64Serializer.write(writer, 0L)
        val reader = BinaryReader(writer.toByteArray())
        UInt64Serializer.read(reader) shouldBe 0L
    }

    test("UInt64 roundtrip Long.MAX_VALUE") {
        val writer = BinaryWriter()
        UInt64Serializer.write(writer, Long.MAX_VALUE)
        val reader = BinaryReader(writer.toByteArray())
        UInt64Serializer.read(reader) shouldBe Long.MAX_VALUE
    }

    test("UInt64 roundtrip negative bit pattern") {
        // Unsigned 64-bit value larger than Long.MAX_VALUE stored as negative Long
        val value = -1L // 0xFFFFFFFFFFFFFFFF
        val writer = BinaryWriter()
        UInt64Serializer.write(writer, value)
        val reader = BinaryReader(writer.toByteArray())
        UInt64Serializer.read(reader) shouldBe value
    }

    // ---- BigUInt types ----

    test("UInt96 roundtrip") {
        val hex = "0102030405060708090a0b0c"
        val writer = BinaryWriter()
        UInt96Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt96Serializer.read(reader) shouldBe hex
    }

    test("UInt128 roundtrip") {
        val hex = "0102030405060708090a0b0c0d0e0f10"
        val writer = BinaryWriter()
        UInt128Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt128Serializer.read(reader) shouldBe hex
    }

    test("UInt160 roundtrip") {
        val hex = "0102030405060708090a0b0c0d0e0f1011121314"
        val writer = BinaryWriter()
        UInt160Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt160Serializer.read(reader) shouldBe hex
    }

    test("UInt256 roundtrip") {
        val hex = "0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20"
        val writer = BinaryWriter()
        UInt256Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt256Serializer.read(reader) shouldBe hex
    }

    test("UInt384 roundtrip") {
        val hex = "01".repeat(48)
        val writer = BinaryWriter()
        UInt384Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt384Serializer.read(reader) shouldBe hex
    }

    test("UInt512 roundtrip") {
        val hex = "ab".repeat(64)
        val writer = BinaryWriter()
        UInt512Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt512Serializer.read(reader) shouldBe hex
    }

    test("UInt256 all zeros") {
        val hex = "00".repeat(32)
        val writer = BinaryWriter()
        UInt256Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt256Serializer.read(reader) shouldBe hex
    }

    test("UInt256 all ones") {
        val hex = "ff".repeat(32)
        val writer = BinaryWriter()
        UInt256Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        UInt256Serializer.read(reader) shouldBe hex
    }
})
