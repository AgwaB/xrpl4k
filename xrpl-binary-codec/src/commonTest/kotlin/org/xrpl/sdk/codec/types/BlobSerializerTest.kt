package org.xrpl.sdk.codec.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class BlobSerializerTest : FunSpec({

    test("roundtrip empty blob") {
        val writer = BinaryWriter()
        BlobSerializer.write(writer, "")
        val reader = BinaryReader(writer.toByteArray())
        BlobSerializer.read(reader) shouldBe ""
    }

    test("roundtrip single byte") {
        val writer = BinaryWriter()
        BlobSerializer.write(writer, "ab")
        val reader = BinaryReader(writer.toByteArray())
        BlobSerializer.read(reader) shouldBe "ab"
    }

    test("roundtrip multiple bytes") {
        val hex = "0102030405060708"
        val writer = BinaryWriter()
        BlobSerializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        BlobSerializer.read(reader) shouldBe hex
    }

    test("roundtrip large blob") {
        val hex = "aa".repeat(200)
        val writer = BinaryWriter()
        BlobSerializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        BlobSerializer.read(reader) shouldBe hex
    }

    test("VL prefix is correct for short blob") {
        val writer = BinaryWriter()
        BlobSerializer.write(writer, "aabb")
        val bytes = writer.toByteArray()
        // VL prefix for length 2 should be single byte 0x02
        bytes[0] shouldBe 0x02.toByte()
        bytes[1] shouldBe 0xAA.toByte()
        bytes[2] shouldBe 0xBB.toByte()
        bytes.size shouldBe 3
    }

    test("roundtrip all zeros") {
        val hex = "00".repeat(32)
        val writer = BinaryWriter()
        BlobSerializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        BlobSerializer.read(reader) shouldBe hex
    }
})
