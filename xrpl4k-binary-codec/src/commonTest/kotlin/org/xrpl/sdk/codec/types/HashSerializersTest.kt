package org.xrpl.sdk.codec.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class HashSerializersTest : FunSpec({

    // ── Hash128 (16 bytes / 32 hex chars) ────────────────────────────────

    test("Hash128 write produces 16 bytes") {
        val hex = "00".repeat(16)
        val writer = BinaryWriter()
        Hash128Serializer.write(writer, hex)
        writer.size shouldBe 16
    }

    test("Hash128 roundtrip all zeros") {
        val hex = "00".repeat(16)
        val writer = BinaryWriter()
        Hash128Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        Hash128Serializer.read(reader) shouldBe hex
    }

    test("Hash128 roundtrip non-trivial value") {
        val hex = "0102030405060708090a0b0c0d0e0f10"
        val writer = BinaryWriter()
        Hash128Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        Hash128Serializer.read(reader) shouldBe hex
    }

    test("Hash128 rejects wrong size (15 bytes)") {
        val hex = "00".repeat(15)
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Hash128Serializer.write(writer, hex)
        }
    }

    test("Hash128 rejects wrong size (17 bytes)") {
        val hex = "00".repeat(17)
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Hash128Serializer.write(writer, hex)
        }
    }

    // ── Hash192 (24 bytes / 48 hex chars) ────────────────────────────────

    test("Hash192 write produces 24 bytes") {
        val hex = "00".repeat(24)
        val writer = BinaryWriter()
        Hash192Serializer.write(writer, hex)
        writer.size shouldBe 24
    }

    test("Hash192 roundtrip all zeros") {
        val hex = "00".repeat(24)
        val writer = BinaryWriter()
        Hash192Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        Hash192Serializer.read(reader) shouldBe hex
    }

    test("Hash192 roundtrip non-trivial value") {
        val hex = "aabbccddeeff00112233445566778899aabbccdd00112233"
        val writer = BinaryWriter()
        Hash192Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        Hash192Serializer.read(reader) shouldBe hex
    }

    test("Hash192 rejects wrong size (23 bytes)") {
        val hex = "00".repeat(23)
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Hash192Serializer.write(writer, hex)
        }
    }

    // ── Hash256 (32 bytes / 64 hex chars) ────────────────────────────────

    test("Hash256 write produces 32 bytes") {
        val hex = "00".repeat(32)
        val writer = BinaryWriter()
        Hash256Serializer.write(writer, hex)
        writer.size shouldBe 32
    }

    test("Hash256 roundtrip all zeros") {
        val hex = "00".repeat(32)
        val writer = BinaryWriter()
        Hash256Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        Hash256Serializer.read(reader) shouldBe hex
    }

    test("Hash256 roundtrip non-trivial value") {
        val hex = "b5e74f1b3dcd7dc8b11c6e4f06fe7e4d8d1f2a5c3e8b0247a96f3d2e1c5b4a9f"
        val writer = BinaryWriter()
        Hash256Serializer.write(writer, hex)
        val reader = BinaryReader(writer.toByteArray())
        Hash256Serializer.read(reader) shouldBe hex
    }

    test("Hash256 rejects wrong size (31 bytes)") {
        val hex = "00".repeat(31)
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Hash256Serializer.write(writer, hex)
        }
    }

    test("Hash256 rejects wrong size (33 bytes)") {
        val hex = "00".repeat(33)
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            Hash256Serializer.write(writer, hex)
        }
    }

    // ── Read produces lowercase hex ───────────────────────────────────────

    test("read returns lowercase hex string") {
        val bytes = ByteArray(32) { it.toByte() }
        val reader = BinaryReader(bytes)
        val result = Hash256Serializer.read(reader)
        result.length shouldBe 64
        result.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
    }
})
