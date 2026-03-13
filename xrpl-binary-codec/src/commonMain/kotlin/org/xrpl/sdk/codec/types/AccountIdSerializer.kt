package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL AccountID type.
 *
 * AccountIDs are 20-byte (160-bit) values, VL-prefixed when serialized.
 * Input/output values are hex strings representing the 20-byte account ID.
 *
 * Note: Base58 (r-address) encoding/decoding is handled at a higher level.
 * This serializer works with the raw 20-byte hex representation.
 */
internal object AccountIdSerializer : TypeSerializer<String> {
    /** AccountID byte size. */
    private const val ACCOUNT_ID_LENGTH: Int = 20

    override fun write(
        writer: BinaryWriter,
        value: String,
    ) {
        val bytes = value.hexToByteArray()
        require(bytes.size == ACCOUNT_ID_LENGTH) {
            "AccountID must be $ACCOUNT_ID_LENGTH bytes (${ACCOUNT_ID_LENGTH * 2} hex chars). " +
                "Got ${bytes.size} bytes."
        }
        writer.writeVlLength(ACCOUNT_ID_LENGTH)
        writer.writeBytes(bytes)
    }

    override fun read(reader: BinaryReader): String {
        val length = reader.readVlLength()
        require(length == ACCOUNT_ID_LENGTH) {
            "Expected AccountID length $ACCOUNT_ID_LENGTH, got $length"
        }
        return reader.readBytes(ACCOUNT_ID_LENGTH).toHexString()
    }
}
