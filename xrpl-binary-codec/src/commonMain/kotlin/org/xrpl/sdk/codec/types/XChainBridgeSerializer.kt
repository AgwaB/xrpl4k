package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL XChainBridge type.
 *
 * Composite type consisting of:
 * - LockingChainDoor (20-byte AccountID, VL-prefixed)
 * - LockingChainIssue (Issue)
 * - IssuingChainDoor (20-byte AccountID, VL-prefixed)
 * - IssuingChainIssue (Issue)
 *
 * Represented as a [Map] with the four named fields.
 */
internal object XChainBridgeSerializer : TypeSerializer<Map<String, Any>> {
    /** AccountID byte size. */
    private const val ACCOUNT_ID_LENGTH: Int = 20

    @Suppress("UNCHECKED_CAST")
    override fun write(
        writer: BinaryWriter,
        value: Map<String, Any>,
    ) {
        // LockingChainDoor
        val lockingDoor =
            value["LockingChainDoor"] as? String
                ?: throw IllegalArgumentException("XChainBridge requires 'LockingChainDoor' field")
        writeAccountId(writer, lockingDoor)

        // LockingChainIssue
        val lockingIssue =
            value["LockingChainIssue"] as? Map<String, String>
                ?: throw IllegalArgumentException("XChainBridge requires 'LockingChainIssue' field")
        IssueSerializer.write(writer, lockingIssue)

        // IssuingChainDoor
        val issuingDoor =
            value["IssuingChainDoor"] as? String
                ?: throw IllegalArgumentException("XChainBridge requires 'IssuingChainDoor' field")
        writeAccountId(writer, issuingDoor)

        // IssuingChainIssue
        val issuingIssue =
            value["IssuingChainIssue"] as? Map<String, String>
                ?: throw IllegalArgumentException("XChainBridge requires 'IssuingChainIssue' field")
        IssueSerializer.write(writer, issuingIssue)
    }

    override fun read(reader: BinaryReader): Map<String, Any> {
        val lockingDoor = readAccountId(reader)
        val lockingIssue = IssueSerializer.read(reader)
        val issuingDoor = readAccountId(reader)
        val issuingIssue = IssueSerializer.read(reader)

        return mapOf(
            "LockingChainDoor" to lockingDoor,
            "LockingChainIssue" to lockingIssue,
            "IssuingChainDoor" to issuingDoor,
            "IssuingChainIssue" to issuingIssue,
        )
    }

    private fun writeAccountId(
        writer: BinaryWriter,
        accountId: String,
    ) {
        val bytes = accountId.hexToByteArray()
        require(bytes.size == ACCOUNT_ID_LENGTH) {
            "AccountID must be $ACCOUNT_ID_LENGTH bytes. Got ${bytes.size}"
        }
        writer.writeVlLength(ACCOUNT_ID_LENGTH)
        writer.writeBytes(bytes)
    }

    private fun readAccountId(reader: BinaryReader): String {
        val length = reader.readVlLength()
        require(length == ACCOUNT_ID_LENGTH) {
            "Expected AccountID length $ACCOUNT_ID_LENGTH, got $length"
        }
        return reader.readBytes(ACCOUNT_ID_LENGTH).toHexString()
    }
}
