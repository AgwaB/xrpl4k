package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL Blob type (variable-length byte data).
 *
 * Writes a VL length prefix followed by the raw bytes.
 * Values are represented as hex strings.
 */
internal object BlobSerializer : TypeSerializer<String> {
    override fun write(
        writer: BinaryWriter,
        value: String,
    ) {
        val bytes = value.hexToByteArray()
        writer.writeVlLength(bytes.size)
        writer.writeBytes(bytes)
    }

    override fun read(reader: BinaryReader): String {
        val length = reader.readVlLength()
        return reader.readBytes(length).toHexString()
    }
}
