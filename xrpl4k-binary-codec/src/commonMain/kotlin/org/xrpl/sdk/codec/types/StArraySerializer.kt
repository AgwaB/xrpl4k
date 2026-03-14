@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

/**
 * Serializer for the XRPL STArray type.
 *
 * An array of STObjects. Each element is written as a nested STObject
 * (with ObjectEndMarker `0xE1`). The array itself is terminated with
 * ArrayEndMarker (`0xF1`).
 *
 * Array elements are typically wrapped in a single-key object where the key
 * is the element type name (e.g., `[{"Memo": {...}}, {"Memo": {...}}]`).
 */
internal object StArraySerializer : TypeSerializer<List<Map<String, Any?>>> {
    /** Array end marker byte. */
    private const val ARRAY_END_MARKER: Int = 0xF1

    /** Object end marker byte (for detecting nested object boundaries). */
    private const val OBJECT_END_MARKER: Int = 0xE1

    override fun write(
        writer: BinaryWriter,
        value: List<Map<String, Any?>>,
    ) {
        for (element in value) {
            StObjectSerializer.writeNested(writer, element)
        }
        writer.writeUInt8(ARRAY_END_MARKER)
    }

    override fun read(reader: BinaryReader): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()

        while (!reader.isExhausted()) {
            val nextByte = reader.peek()
            if (nextByte == ARRAY_END_MARKER) {
                reader.readUInt8() // consume marker
                break
            }
            result.add(StObjectSerializer.readNested(reader))
        }

        return result
    }
}
