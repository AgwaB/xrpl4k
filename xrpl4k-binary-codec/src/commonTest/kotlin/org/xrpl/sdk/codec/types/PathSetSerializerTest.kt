package org.xrpl.sdk.codec.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class PathSetSerializerTest : FunSpec({

    val account1 = "aa".repeat(20) // 40-char hex (20 bytes)
    val account2 = "bb".repeat(20)
    val issuer1 = "cc".repeat(20)

    // ── Empty path set ────────────────────────────────────────────────────

    test("empty path set serializes to single terminator byte (0x00)") {
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, emptyList())
        val bytes = writer.toByteArray()
        bytes.size shouldBe 1
        bytes[0] shouldBe 0x00.toByte()
    }

    test("empty path set roundtrip") {
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, emptyList())
        val reader = BinaryReader(writer.toByteArray())
        PathSetSerializer.read(reader) shouldBe emptyList()
    }

    // ── Single step type flags ────────────────────────────────────────────

    test("step with account only writes typeFlag 0x01") {
        val pathSet = listOf(listOf(mapOf("account" to account1)))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val bytes = writer.toByteArray()
        // First byte after start is the typeFlag for the first step
        bytes[0] shouldBe 0x01.toByte()
    }

    test("step with currency only writes typeFlag 0x10") {
        val pathSet = listOf(listOf(mapOf("currency" to "USD")))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val bytes = writer.toByteArray()
        bytes[0] shouldBe 0x10.toByte()
    }

    test("step with issuer only writes typeFlag 0x20") {
        val pathSet = listOf(listOf(mapOf("issuer" to issuer1)))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val bytes = writer.toByteArray()
        bytes[0] shouldBe 0x20.toByte()
    }

    test("step with all three writes typeFlag 0x31") {
        val step = mapOf("account" to account1, "currency" to "USD", "issuer" to issuer1)
        val pathSet = listOf(listOf(step))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val bytes = writer.toByteArray()
        bytes[0] shouldBe 0x31.toByte()
    }

    // ── Roundtrip tests ───────────────────────────────────────────────────

    test("single path single account step roundtrip") {
        val pathSet = listOf(listOf(mapOf("account" to account1)))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val reader = BinaryReader(writer.toByteArray())
        val result = PathSetSerializer.read(reader)

        result.size shouldBe 1
        result[0].size shouldBe 1
        result[0][0]["account"] shouldBe account1
    }

    test("single path single currency step roundtrip") {
        val pathSet = listOf(listOf(mapOf("currency" to "USD")))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val reader = BinaryReader(writer.toByteArray())
        val result = PathSetSerializer.read(reader)

        result.size shouldBe 1
        result[0][0]["currency"] shouldBe "USD"
    }

    test("single path single issuer step roundtrip") {
        val pathSet = listOf(listOf(mapOf("issuer" to issuer1)))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val reader = BinaryReader(writer.toByteArray())
        val result = PathSetSerializer.read(reader)

        result.size shouldBe 1
        result[0][0]["issuer"] shouldBe issuer1
    }

    test("step with all three components roundtrip") {
        val step = mapOf("account" to account1, "currency" to "USD", "issuer" to issuer1)
        val pathSet = listOf(listOf(step))
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val reader = BinaryReader(writer.toByteArray())
        val result = PathSetSerializer.read(reader)

        result.size shouldBe 1
        result[0][0]["account"] shouldBe account1
        result[0][0]["currency"] shouldBe "USD"
        result[0][0]["issuer"] shouldBe issuer1
    }

    // ── Multi-path ────────────────────────────────────────────────────────

    test("two paths have 0xFF separator byte") {
        val pathSet =
            listOf(
                listOf(mapOf("account" to account1)),
                listOf(mapOf("account" to account2)),
            )
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val bytes = writer.toByteArray()

        // Structure: [0x01][account1(20)][0xFF][0x01][account2(20)][0x00]
        // Separator is at position 1+20 = 21
        bytes[21] shouldBe 0xFF.toByte()
    }

    test("two paths roundtrip") {
        val pathSet =
            listOf(
                listOf(mapOf("account" to account1)),
                listOf(mapOf("account" to account2)),
            )
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val reader = BinaryReader(writer.toByteArray())
        val result = PathSetSerializer.read(reader)

        result.size shouldBe 2
        result[0][0]["account"] shouldBe account1
        result[1][0]["account"] shouldBe account2
    }

    test("path with multiple steps roundtrip") {
        val pathSet =
            listOf(
                listOf(
                    mapOf("account" to account1),
                    mapOf("currency" to "XRP"),
                    mapOf("account" to account2),
                ),
            )
        val writer = BinaryWriter()
        PathSetSerializer.write(writer, pathSet)
        val reader = BinaryReader(writer.toByteArray())
        val result = PathSetSerializer.read(reader)

        result.size shouldBe 1
        result[0].size shouldBe 3
        result[0][0]["account"] shouldBe account1
        result[0][1]["currency"] shouldBe "XRP"
        result[0][2]["account"] shouldBe account2
    }

    // ── Error cases ───────────────────────────────────────────────────────

    test("account with wrong byte count throws") {
        val pathSet = listOf(listOf(mapOf("account" to "aa".repeat(19)))) // 19 bytes
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            PathSetSerializer.write(writer, pathSet)
        }
    }

    test("issuer with wrong byte count throws") {
        val pathSet = listOf(listOf(mapOf("issuer" to "cc".repeat(21)))) // 21 bytes
        val writer = BinaryWriter()
        shouldThrow<IllegalArgumentException> {
            PathSetSerializer.write(writer, pathSet)
        }
    }
})
