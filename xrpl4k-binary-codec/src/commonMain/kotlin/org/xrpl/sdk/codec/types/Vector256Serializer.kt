package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL Vector256 type.
 *
 * A VL-prefixed array of 32-byte (256-bit) hashes.
 * Values are represented as a list of hex strings.
 */
internal object Vector256Serializer : TypeSerializer<List<String>> {
    /** Size in bytes of each hash entry. */
    private const val HASH_SIZE: Int = 32

    override fun write(
        writer: BinaryWriter,
        value: List<String>,
    ) {
        val totalBytes = value.size * HASH_SIZE
        writer.writeVlLength(totalBytes)
        for (hash in value) {
            val bytes = hash.hexToByteArray()
            require(bytes.size == HASH_SIZE) {
                "Each Vector256 entry must be $HASH_SIZE bytes (${HASH_SIZE * 2} hex chars). " +
                    "Got ${bytes.size} bytes."
            }
            writer.writeBytes(bytes)
        }
    }

    override fun read(reader: BinaryReader): List<String> {
        val totalBytes = reader.readVlLength()
        require(totalBytes % HASH_SIZE == 0) {
            "Vector256 length ($totalBytes) must be a multiple of $HASH_SIZE"
        }
        val count = totalBytes / HASH_SIZE
        return List(count) {
            reader.readBytes(HASH_SIZE).toHexString()
        }
    }
}
