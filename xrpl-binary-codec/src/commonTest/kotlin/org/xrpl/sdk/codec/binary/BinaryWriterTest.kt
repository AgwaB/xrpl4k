package org.xrpl.sdk.codec.binary

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BinaryWriterTest : FunSpec({

    test("writeUInt8 writes single byte") {
        val writer = BinaryWriter()
        writer.writeUInt8(0xAB)
        writer.toByteArray() shouldBe byteArrayOf(0xAB.toByte())
    }

    test("writeUInt8 accepts boundary values 0 and 255") {
        val writer = BinaryWriter()
        writer.writeUInt8(0)
        writer.writeUInt8(255)
        writer.toByteArray() shouldBe byteArrayOf(0x00, 0xFF.toByte())
    }

    test("writeUInt8 rejects value below 0") {
        shouldThrow<IllegalArgumentException> {
            BinaryWriter().writeUInt8(-1)
        }
    }

    test("writeUInt8 rejects value above 255") {
        shouldThrow<IllegalArgumentException> {
            BinaryWriter().writeUInt8(256)
        }
    }

    test("writeUInt16 writes two bytes big-endian") {
        val writer = BinaryWriter()
        writer.writeUInt16(0x1234)
        writer.toByteArray() shouldBe byteArrayOf(0x12, 0x34)
    }

    test("writeUInt16 accepts boundary values 0 and 65535") {
        val writer = BinaryWriter()
        writer.writeUInt16(0)
        writer.writeUInt16(65535)
        writer.toByteArray() shouldBe byteArrayOf(0x00, 0x00, 0xFF.toByte(), 0xFF.toByte())
    }

    test("writeUInt16 rejects value below 0") {
        shouldThrow<IllegalArgumentException> {
            BinaryWriter().writeUInt16(-1)
        }
    }

    test("writeUInt16 rejects value above 65535") {
        shouldThrow<IllegalArgumentException> {
            BinaryWriter().writeUInt16(65536)
        }
    }

    test("writeUInt32 writes four bytes big-endian") {
        val writer = BinaryWriter()
        writer.writeUInt32(0x12345678L)
        writer.toByteArray() shouldBe byteArrayOf(0x12, 0x34, 0x56, 0x78)
    }

    test("writeUInt32 accepts boundary values 0 and 4294967295") {
        val writer = BinaryWriter()
        writer.writeUInt32(0L)
        writer.writeUInt32(0xFFFFFFFFL)
        writer.toByteArray() shouldBe
            byteArrayOf(
                0x00, 0x00, 0x00, 0x00,
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            )
    }

    test("writeUInt32 rejects value below 0") {
        shouldThrow<IllegalArgumentException> {
            BinaryWriter().writeUInt32(-1L)
        }
    }

    test("writeUInt32 rejects value above 4294967295") {
        shouldThrow<IllegalArgumentException> {
            BinaryWriter().writeUInt32(0x100000000L)
        }
    }

    test("writeUInt64 writes eight bytes big-endian") {
        val writer = BinaryWriter()
        writer.writeUInt64(0x0102030405060708L)
        writer.toByteArray() shouldBe byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
    }

    test("writeUInt64 accepts 0") {
        val writer = BinaryWriter()
        writer.writeUInt64(0L)
        writer.toByteArray() shouldBe ByteArray(8)
    }

    test("writeUInt64 accepts Long.MAX_VALUE") {
        val writer = BinaryWriter()
        writer.writeUInt64(Long.MAX_VALUE)
        writer.toByteArray() shouldBe
            byteArrayOf(
                0x7F, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            )
    }

    test("writeBytes appends raw bytes") {
        val writer = BinaryWriter()
        writer.writeBytes(byteArrayOf(0x01, 0x02, 0x03))
        writer.toByteArray() shouldBe byteArrayOf(0x01, 0x02, 0x03)
    }

    test("writeBytes appends empty array without error") {
        val writer = BinaryWriter()
        writer.writeBytes(byteArrayOf())
        writer.size shouldBe 0
    }

    test("size reflects number of bytes written") {
        val writer = BinaryWriter()
        writer.size shouldBe 0
        writer.writeUInt8(0x01)
        writer.size shouldBe 1
        writer.writeUInt16(0x0203)
        writer.size shouldBe 3
        writer.writeUInt32(0x04050607L)
        writer.size shouldBe 7
        writer.writeUInt64(0L)
        writer.size shouldBe 15
    }

    test("buffer auto-grows beyond initial capacity") {
        val writer = BinaryWriter(initialCapacity = 4)
        repeat(10) { writer.writeUInt32(0xDEADBEEFL) }
        writer.size shouldBe 40
        val bytes = writer.toByteArray()
        bytes.size shouldBe 40
        // Verify content of first and last UInt32
        bytes[0] shouldBe 0xDE.toByte()
        bytes[36] shouldBe 0xDE.toByte()
    }

    test("multiple sequential writes produce correct output") {
        val writer = BinaryWriter()
        writer.writeUInt8(0xAA)
        writer.writeUInt16(0xBBCC)
        writer.writeUInt32(0xDDEEFF00L)
        writer.toByteArray() shouldBe
            byteArrayOf(
                0xAA.toByte(),
                0xBB.toByte(), 0xCC.toByte(),
                0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte(), 0x00,
            )
    }

    test("toByteArray returns independent copy") {
        val writer = BinaryWriter()
        writer.writeUInt8(0x01)
        val first = writer.toByteArray()
        writer.writeUInt8(0x02)
        val second = writer.toByteArray()
        first.size shouldBe 1
        second.size shouldBe 2
    }
})
