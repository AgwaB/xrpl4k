package org.xrpl.sdk.codec.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class Vector256SerializerTest : FunSpec({

    val hash1 = "aa".repeat(32) // 64-char hex (32 bytes)
    val hash2 = "bb".repeat(32)
    val hash3 = "cc".repeat(32)

    // ── Empty list ────────────────────────────────────────────────────────

    test("empty list writes VL prefix of 0 then nothing else") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, emptyList())
        val bytes = writer.toByteArray()
        bytes.size shouldBe 1
        bytes[0] shouldBe 0x00.toByte() // VL length = 0
    }

    test("empty list roundtrip") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, emptyList())
        val reader = BinaryReader(writer.toByteArray())
        Vector256Serializer.read(reader) shouldBe emptyList()
    }

    // ── Single hash ───────────────────────────────────────────────────────

    test("single hash writes VL prefix of 32") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1))
        val bytes = writer.toByteArray()
        // VL prefix for 32 bytes = single byte 0x20
        bytes[0] shouldBe 0x20.toByte()
    }

    test("single hash produces 1 (VL) + 32 (data) = 33 bytes") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1))
        writer.size shouldBe 33
    }

    test("single hash roundtrip") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1))
        val reader = BinaryReader(writer.toByteArray())
        val result = Vector256Serializer.read(reader)

        result.size shouldBe 1
        result[0] shouldBe hash1
    }

    // ── Multiple hashes ───────────────────────────────────────────────────

    test("two hashes roundtrip") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1, hash2))
        val reader = BinaryReader(writer.toByteArray())
        val result = Vector256Serializer.read(reader)

        result.size shouldBe 2
        result[0] shouldBe hash1
        result[1] shouldBe hash2
    }

    test("three hashes roundtrip preserves order") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1, hash2, hash3))
        val reader = BinaryReader(writer.toByteArray())
        val result = Vector256Serializer.read(reader)

        result.size shouldBe 3
        result[0] shouldBe hash1
        result[1] shouldBe hash2
        result[2] shouldBe hash3
    }

    test("two hashes total byte count: 1 (VL) + 64 (data) = 65") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1, hash2))
        writer.size shouldBe 65
    }

    // ── Error cases ───────────────────────────────────────────────────────

    test("hash with wrong size (31 bytes) throws") {
        val shortHash = "aa".repeat(31) // 62 hex chars
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Vector256Serializer.write(writer, listOf(shortHash))
        }
    }

    test("hash with wrong size (33 bytes) throws") {
        val longHash = "aa".repeat(33) // 66 hex chars
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Vector256Serializer.write(writer, listOf(longHash))
        }
    }

    // ── Determinism ───────────────────────────────────────────────────────

    test("same inputs produce same output") {
        val input = listOf(hash1, hash2)

        val writer1 = BinaryWriter()
        Vector256Serializer.write(writer1, input)

        val writer2 = BinaryWriter()
        Vector256Serializer.write(writer2, input)

        writer1.toByteArray().toList() shouldBe writer2.toByteArray().toList()
    }

    // ── Read produces lowercase hex ───────────────────────────────────────

    test("read returns lowercase hex strings") {
        val writer = BinaryWriter()
        Vector256Serializer.write(writer, listOf(hash1))
        val reader = BinaryReader(writer.toByteArray())
        val result = Vector256Serializer.read(reader)

        result[0].length shouldBe 64
        result[0].all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
    }
})
