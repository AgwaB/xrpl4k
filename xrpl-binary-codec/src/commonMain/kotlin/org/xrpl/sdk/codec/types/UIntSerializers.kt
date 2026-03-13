package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for unsigned 8-bit integers (1 byte).
 */
internal object UInt8Serializer : TypeSerializer<Int> {
    override fun write(
        writer: BinaryWriter,
        value: Int,
    ) {
        writer.writeUInt8(value)
    }

    override fun read(reader: BinaryReader): Int = reader.readUInt8()
}

/**
 * Serializer for unsigned 16-bit integers (2 bytes, big-endian).
 */
internal object UInt16Serializer : TypeSerializer<Int> {
    override fun write(
        writer: BinaryWriter,
        value: Int,
    ) {
        writer.writeUInt16(value)
    }

    override fun read(reader: BinaryReader): Int = reader.readUInt16()
}

/**
 * Serializer for unsigned 32-bit integers (4 bytes, big-endian).
 *
 * Uses [Long] because UInt32 max (4294967295) exceeds [Int.MAX_VALUE].
 */
internal object UInt32Serializer : TypeSerializer<Long> {
    override fun write(
        writer: BinaryWriter,
        value: Long,
    ) {
        writer.writeUInt32(value)
    }

    override fun read(reader: BinaryReader): Long = reader.readUInt32()
}

/**
 * Serializer for unsigned 64-bit integers (8 bytes, big-endian).
 *
 * The full 64-bit range is represented as a signed [Long] with the same bit pattern.
 */
internal object UInt64Serializer : TypeSerializer<Long> {
    override fun write(
        writer: BinaryWriter,
        value: Long,
    ) {
        writer.writeUInt64(value)
    }

    override fun read(reader: BinaryReader): Long = reader.readUInt64()
}

/**
 * Serializer for fixed-width unsigned integers larger than 64 bits.
 *
 * These types are serialized as raw byte arrays and represented as hex strings.
 *
 * @param byteSize Number of bytes for this type.
 */
internal class BigUIntSerializer(
    private val byteSize: Int,
) : TypeSerializer<String> {
    override fun write(
        writer: BinaryWriter,
        value: String,
    ) {
        val bytes = value.hexToByteArray()
        require(bytes.size == byteSize) {
            "Expected $byteSize bytes for UInt${byteSize * 8}, got ${bytes.size}"
        }
        writer.writeBytes(bytes)
    }

    override fun read(reader: BinaryReader): String = reader.readBytes(byteSize).toHexString()
}

/** Serializer for UInt96 (12 bytes). */
internal val UInt96Serializer: TypeSerializer<String> = BigUIntSerializer(byteSize = 12)

/** Serializer for UInt128 (16 bytes). */
internal val UInt128Serializer: TypeSerializer<String> = BigUIntSerializer(byteSize = 16)

/** Serializer for UInt160 (20 bytes). */
internal val UInt160Serializer: TypeSerializer<String> = BigUIntSerializer(byteSize = 20)

/** Serializer for UInt256 (32 bytes). */
internal val UInt256Serializer: TypeSerializer<String> = BigUIntSerializer(byteSize = 32)

/** Serializer for UInt384 (48 bytes). */
internal val UInt384Serializer: TypeSerializer<String> = BigUIntSerializer(byteSize = 48)

/** Serializer for UInt512 (64 bytes). */
internal val UInt512Serializer: TypeSerializer<String> = BigUIntSerializer(byteSize = 64)
