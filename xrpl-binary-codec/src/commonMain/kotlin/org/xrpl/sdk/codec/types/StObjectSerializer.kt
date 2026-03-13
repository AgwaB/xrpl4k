@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.codec.definitions.Definitions
import org.xrpl.sdk.codec.definitions.FieldId

/**
 * Serializer for the XRPL STObject type.
 *
 * Serializes JSON object fields in canonical order (sorted by type code ascending,
 * then by field code ascending). Each field is written as a field header followed
 * by the field value dispatched to the appropriate [TypeSerializer].
 *
 * Nested STObjects are terminated with the ObjectEndMarker byte (`0xE1`).
 * Top-level objects are not terminated.
 */
internal object StObjectSerializer : TypeSerializer<Map<String, Any?>> {
    /** Object end marker byte. */
    private const val OBJECT_END_MARKER: Int = 0xE1

    /**
     * Writes an STObject to the [writer].
     *
     * @param writer Destination binary writer.
     * @param value Map of field name to field value.
     */
    override fun write(
        writer: BinaryWriter,
        value: Map<String, Any?>,
    ) {
        writeFields(writer, value)
    }

    /**
     * Writes an STObject as a nested object (with end marker).
     *
     * @param writer Destination binary writer.
     * @param value Map of field name to field value.
     */
    internal fun writeNested(
        writer: BinaryWriter,
        value: Map<String, Any?>,
    ) {
        writeFields(writer, value)
        writer.writeUInt8(OBJECT_END_MARKER)
    }

    /**
     * Reads an STObject from the [reader].
     *
     * Reads fields until the buffer is exhausted (top-level) or until the
     * ObjectEndMarker is encountered (nested).
     *
     * @param reader Source binary reader.
     * @return Map of field name to deserialized value.
     */
    override fun read(reader: BinaryReader): Map<String, Any?> = readUntilEnd(reader, stopAtMarker = false)

    /**
     * Reads a nested STObject (stops at ObjectEndMarker).
     *
     * @param reader Source binary reader.
     * @return Map of field name to deserialized value.
     */
    internal fun readNested(reader: BinaryReader): Map<String, Any?> = readUntilEnd(reader, stopAtMarker = true)

    private fun writeFields(
        writer: BinaryWriter,
        fields: Map<String, Any?>,
    ) {
        // Get fields in canonical order, skipping non-serialized fields
        val ordered = Definitions.getCanonicalFieldOrder(fields.keys)

        for (fieldDef in ordered) {
            if (!fieldDef.isSerialized) continue
            val fieldValue = fields[fieldDef.name] ?: continue

            // Write field header
            val headerBytes = fieldDef.fieldId.toBytes()
            writer.writeBytes(headerBytes)

            // Dispatch to the appropriate serializer
            val typeCode = fieldDef.typeCode.value
            writeFieldValue(writer, typeCode, fieldDef.name, fieldValue)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun writeFieldValue(
        writer: BinaryWriter,
        typeCode: Int,
        fieldName: String,
        value: Any,
    ) {
        val registry = TypeSerializerRegistry
        val serializer =
            registry.getSerializer(typeCode)
                ?: throw IllegalArgumentException(
                    "No serializer for type code $typeCode (field '$fieldName')",
                )

        (serializer as TypeSerializer<Any?>).write(writer, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun readUntilEnd(
        reader: BinaryReader,
        stopAtMarker: Boolean,
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()

        while (!reader.isExhausted()) {
            val nextByte = reader.peek()

            // Check for end marker
            if (nextByte == OBJECT_END_MARKER) {
                if (stopAtMarker) {
                    reader.readUInt8() // consume the marker
                }
                break
            }

            // Read field header
            // We need to read the FieldId from the reader
            val fieldId = readFieldId(reader)

            // Look up the field definition
            val fieldDef =
                Definitions.fieldsByFieldId[fieldId]
                    ?: throw IllegalArgumentException(
                        "Unknown field ID: typeCode=${fieldId.typeCode}, fieldCode=${fieldId.fieldCode}",
                    )

            // Read the field value
            val typeCode = fieldDef.typeCode.value
            val serializer =
                TypeSerializerRegistry.getSerializer(typeCode)
                    ?: throw IllegalArgumentException(
                        "No serializer for type code $typeCode (field '${fieldDef.name}')",
                    )

            val fieldValue = (serializer as TypeSerializer<Any?>).read(reader)
            result[fieldDef.name] = fieldValue
        }

        return result
    }

    /**
     * Reads a [FieldId] from the binary reader.
     */
    private fun readFieldId(reader: BinaryReader): FieldId {
        val firstByte = reader.readUInt8()
        val highNibble = firstByte shr 4
        val lowNibble = firstByte and 0x0F

        val typeCode = if (highNibble == 0) reader.readUInt8() else highNibble
        val fieldCode = if (lowNibble == 0) reader.readUInt8() else lowNibble

        return FieldId(typeCode = typeCode, fieldCode = fieldCode)
    }
}
