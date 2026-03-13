package org.xrpl.sdk.codec.binary

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BinaryReaderTest : FunSpec({

    test("readUInt8 reads single byte as unsigned") {
        val reader = BinaryReader(byteArrayOf(0xAB.toByte()))
        reader.readUInt8() shouldBe 0xAB
    }

    test("readUInt8 reads boundary values 0 and 255") {
        val reader = BinaryReader(byteArrayOf(0x00, 0xFF.toByte()))
        reader.readUInt8() shouldBe 0
        reader.readUInt8() shouldBe 255
    }

    test("readUInt8 throws on exhausted buffer") {
        val reader = BinaryReader(byteArrayOf())
        shouldThrow<IllegalArgumentException> {
            reader.readUInt8()
        }
    }

    test("readUInt16 reads two bytes big-endian as unsigned") {
        val reader = BinaryReader(byteArrayOf(0x12, 0x34))
        reader.readUInt16() shouldBe 0x1234
    }

    test("readUInt16 reads boundary value 65535") {
        val reader = BinaryReader(byteArrayOf(0xFF.toByte(), 0xFF.toByte()))
        reader.readUInt16() shouldBe 65535
    }

    test("readUInt16 reads boundary value 0") {
        val reader = BinaryReader(byteArrayOf(0x00, 0x00))
        reader.readUInt16() shouldBe 0
    }

    test("readUInt16 throws when fewer than 2 bytes remain") {
        val reader = BinaryReader(byteArrayOf(0x01))
        shouldThrow<IllegalArgumentException> {
            reader.readUInt16()
        }
    }

    test("readUInt32 reads four bytes big-endian as unsigned Long") {
        val reader = BinaryReader(byteArrayOf(0x12, 0x34, 0x56, 0x78))
        reader.readUInt32() shouldBe 0x12345678L
    }

    test("readUInt32 reads boundary value 4294967295") {
        val reader =
            BinaryReader(
                byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()),
            )
        reader.readUInt32() shouldBe 0xFFFFFFFFL
    }

    test("readUInt32 reads boundary value 0") {
        val reader = BinaryReader(byteArrayOf(0x00, 0x00, 0x00, 0x00))
        reader.readUInt32() shouldBe 0L
    }

    test("readUInt32 throws when fewer than 4 bytes remain") {
        val reader = BinaryReader(byteArrayOf(0x01, 0x02, 0x03))
        shouldThrow<IllegalArgumentException> {
            reader.readUInt32()
        }
    }

    test("readUInt64 reads eight bytes big-endian") {
        val reader =
            BinaryReader(
                byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
            )
        reader.readUInt64() shouldBe 0x0102030405060708L
    }

    test("readUInt64 reads Long.MAX_VALUE") {
        val reader =
            BinaryReader(
                byteArrayOf(
                    0x7F,
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                ),
            )
        reader.readUInt64() shouldBe Long.MAX_VALUE
    }

    test("readUInt64 reads all zeros") {
        val reader = BinaryReader(ByteArray(8))
        reader.readUInt64() shouldBe 0L
    }

    test("readUInt64 throws when fewer than 8 bytes remain") {
        val reader = BinaryReader(ByteArray(7))
        shouldThrow<IllegalArgumentException> {
            reader.readUInt64()
        }
    }

    test("readBytes reads exact count") {
        val reader = BinaryReader(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        reader.readBytes(3) shouldBe byteArrayOf(0x01, 0x02, 0x03)
        reader.remaining() shouldBe 1
    }

    test("readBytes returns empty array for count 0") {
        val reader = BinaryReader(byteArrayOf(0x01))
        reader.readBytes(0) shouldBe byteArrayOf()
        reader.remaining() shouldBe 1
    }

    test("readBytes throws when not enough bytes remain") {
        val reader = BinaryReader(byteArrayOf(0x01, 0x02))
        shouldThrow<IllegalArgumentException> {
            reader.readBytes(3)
        }
    }

    test("remaining decrements with each read") {
        val reader = BinaryReader(byteArrayOf(0x01, 0x02, 0x03))
        reader.remaining() shouldBe 3
        reader.readUInt8()
        reader.remaining() shouldBe 2
        reader.readUInt16()
        reader.remaining() shouldBe 0
    }

    test("isExhausted returns false initially and true after all bytes consumed") {
        val reader = BinaryReader(byteArrayOf(0x01))
        reader.isExhausted() shouldBe false
        reader.readUInt8()
        reader.isExhausted() shouldBe true
    }

    test("isExhausted returns true for empty buffer") {
        BinaryReader(byteArrayOf()).isExhausted() shouldBe true
    }

    test("peek returns next byte without advancing position") {
        val reader = BinaryReader(byteArrayOf(0xAB.toByte(), 0xCD.toByte()))
        reader.peek() shouldBe 0xAB
        reader.peek() shouldBe 0xAB
        reader.remaining() shouldBe 2
    }

    test("peek throws when buffer is exhausted") {
        val reader = BinaryReader(byteArrayOf())
        shouldThrow<IllegalArgumentException> {
            reader.peek()
        }
    }

    test("multiple sequential reads produce correct values") {
        val reader =
            BinaryReader(
                byteArrayOf(
                    0xAA.toByte(),
                    0xBB.toByte(),
                    0xCC.toByte(),
                    0xDD.toByte(),
                    0xEE.toByte(),
                    0xFF.toByte(),
                    0x00,
                ),
            )
        reader.readUInt8() shouldBe 0xAA
        reader.readUInt16() shouldBe 0xBBCC
        reader.readUInt32() shouldBe 0xDDEEFF00L
        reader.isExhausted() shouldBe true
    }

    test("underflow error message includes position and remaining info") {
        val reader = BinaryReader(byteArrayOf(0x01))
        reader.readUInt8() // consume the only byte
        val ex =
            shouldThrow<IllegalArgumentException> {
                reader.readUInt8()
            }
        ex.message?.contains("position") shouldBe true
    }
})
