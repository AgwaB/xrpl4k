package org.xrpl.sdk.codec.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

class StArraySerializerTest : FunSpec({

    // ── Empty array ───────────────────────────────────────────────────────

    test("empty array writes only the array end marker (0xF1)") {
        val writer = BinaryWriter()
        StArraySerializer.write(writer, emptyList())
        val bytes = writer.toByteArray()
        bytes.size shouldBe 1
        bytes[0] shouldBe 0xF1.toByte()
    }

    test("empty array roundtrip") {
        val writer = BinaryWriter()
        StArraySerializer.write(writer, emptyList())
        val reader = BinaryReader(writer.toByteArray())
        val result = StArraySerializer.read(reader)
        result shouldBe emptyList()
    }

    // ── Single element ────────────────────────────────────────────────────

    test("single element array ends with 0xF1 array end marker") {
        val element = mapOf<String, Any?>("Flags" to 0L)
        val writer = BinaryWriter()
        StArraySerializer.write(writer, listOf(element))
        val bytes = writer.toByteArray()
        bytes[bytes.size - 1] shouldBe 0xF1.toByte()
    }

    test("single element contains nested object end marker 0xE1") {
        val element = mapOf<String, Any?>("Flags" to 0L)
        val writer = BinaryWriter()
        StArraySerializer.write(writer, listOf(element))
        val bytes = writer.toByteArray()
        // 0xE1 must appear before the final 0xF1
        val e1Index = bytes.indexOfFirst { it == 0xE1.toByte() }
        e1Index shouldBe (bytes.size - 2)
    }

    test("single element roundtrip") {
        val element = mapOf<String, Any?>("Flags" to 0L)
        val writer = BinaryWriter()
        StArraySerializer.write(writer, listOf(element))
        val reader = BinaryReader(writer.toByteArray())
        val result = StArraySerializer.read(reader)

        result.size shouldBe 1
        result[0]["Flags"] shouldBe 0L
    }

    // ── Multiple elements ─────────────────────────────────────────────────

    test("two elements roundtrip") {
        val elements =
            listOf(
                mapOf<String, Any?>("Flags" to 0L),
                mapOf<String, Any?>("Flags" to 1L),
            )
        val writer = BinaryWriter()
        StArraySerializer.write(writer, elements)
        val reader = BinaryReader(writer.toByteArray())
        val result = StArraySerializer.read(reader)

        result.size shouldBe 2
        result[0]["Flags"] shouldBe 0L
        result[1]["Flags"] shouldBe 1L
    }

    test("three elements roundtrip preserves order") {
        val elements =
            listOf(
                mapOf<String, Any?>("Flags" to 0L),
                mapOf<String, Any?>("Sequence" to 1L),
                mapOf<String, Any?>("Flags" to 2L),
            )
        val writer = BinaryWriter()
        StArraySerializer.write(writer, elements)
        val reader = BinaryReader(writer.toByteArray())
        val result = StArraySerializer.read(reader)

        result.size shouldBe 3
        result[0]["Flags"] shouldBe 0L
        result[1]["Sequence"] shouldBe 1L
        result[2]["Flags"] shouldBe 2L
    }

    test("multiple elements: each has an 0xE1 object end marker") {
        val elements =
            listOf(
                mapOf<String, Any?>("Flags" to 0L),
                mapOf<String, Any?>("Flags" to 1L),
            )
        val writer = BinaryWriter()
        StArraySerializer.write(writer, elements)
        val bytes = writer.toByteArray()

        val e1Count = bytes.count { it == 0xE1.toByte() }
        e1Count shouldBe 2
    }

    // ── Determinism ───────────────────────────────────────────────────────

    test("same input always produces same bytes") {
        val elements = listOf(mapOf<String, Any?>("Flags" to 0L, "Sequence" to 1L))

        val writer1 = BinaryWriter()
        StArraySerializer.write(writer1, elements)

        val writer2 = BinaryWriter()
        StArraySerializer.write(writer2, elements)

        writer1.toByteArray().toList() shouldBe writer2.toByteArray().toList()
    }
})
