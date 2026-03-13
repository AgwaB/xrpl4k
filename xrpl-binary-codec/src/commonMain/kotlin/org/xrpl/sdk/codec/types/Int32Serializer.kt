package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

/**
 * Serializer for signed 32-bit integers (4 bytes, big-endian).
 *
 * Distinct from [UInt32Serializer] -- this handles the XRPL `Int32` type
 * which can represent negative values.
 */
internal object Int32Serializer : TypeSerializer<Int> {
    override fun write(
        writer: BinaryWriter,
        value: Int,
    ) {
        writer.writeUInt8((value ushr 24) and 0xFF)
        writer.writeUInt8((value ushr 16) and 0xFF)
        writer.writeUInt8((value ushr 8) and 0xFF)
        writer.writeUInt8(value and 0xFF)
    }

    override fun read(reader: BinaryReader): Int {
        val b0 = reader.readUInt8()
        val b1 = reader.readUInt8()
        val b2 = reader.readUInt8()
        val b3 = reader.readUInt8()
        return (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
    }
}
