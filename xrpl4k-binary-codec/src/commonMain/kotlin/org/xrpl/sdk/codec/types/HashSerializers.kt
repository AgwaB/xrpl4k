package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for fixed-width hash types.
 *
 * Reads and writes a fixed number of bytes, represented as hex strings.
 *
 * @param byteSize Number of bytes for this hash type.
 */
internal class HashSerializer(
    private val byteSize: Int,
) : TypeSerializer<String> {
    override fun write(
        writer: BinaryWriter,
        value: String,
    ) {
        val bytes = value.hexToByteArray()
        require(bytes.size == byteSize) {
            "Expected $byteSize bytes for Hash${byteSize * 8}, got ${bytes.size}"
        }
        writer.writeBytes(bytes)
    }

    override fun read(reader: BinaryReader): String = reader.readBytes(byteSize).toHexString()
}

/** Serializer for Hash128 (16 bytes / 128 bits). */
internal val Hash128Serializer: TypeSerializer<String> = HashSerializer(byteSize = 16)

/** Serializer for Hash192 (24 bytes / 192 bits). */
internal val Hash192Serializer: TypeSerializer<String> = HashSerializer(byteSize = 24)

/** Serializer for Hash256 (32 bytes / 256 bits). */
internal val Hash256Serializer: TypeSerializer<String> = HashSerializer(byteSize = 32)
