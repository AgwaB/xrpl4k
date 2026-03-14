package org.xrpl.sdk.codec.definitions

/**
 * Encodes and decodes field header bytes per the XRPL binary format specification.
 *
 * The encoding scheme uses a variable-length header (1--3 bytes) based on whether
 * the [typeCode] and [fieldCode] are below or above 16:
 *
 * - Both < 16: 1 byte `(typeCode shl 4 or fieldCode)`
 * - typeCode >= 16, fieldCode < 16: 2 bytes `(fieldCode, typeCode)`
 * - typeCode < 16, fieldCode >= 16: 2 bytes `(typeCode shl 4, fieldCode)`
 * - Both >= 16: 3 bytes `(0x00, typeCode, fieldCode)`
 */
internal data class FieldId(
    val typeCode: Int,
    val fieldCode: Int,
) {
    /**
     * Encode this [FieldId] to its binary header representation.
     */
    fun toBytes(): ByteArray {
        val highNibble = if (typeCode < 16) typeCode else 0
        val lowNibble = if (fieldCode < 16) fieldCode else 0
        val firstByte = (highNibble shl 4) or lowNibble

        return when {
            typeCode < 16 && fieldCode < 16 ->
                byteArrayOf(firstByte.toByte())

            typeCode >= 16 && fieldCode < 16 ->
                byteArrayOf(firstByte.toByte(), typeCode.toByte())

            typeCode < 16 && fieldCode >= 16 ->
                byteArrayOf(firstByte.toByte(), fieldCode.toByte())

            else ->
                byteArrayOf(firstByte.toByte(), typeCode.toByte(), fieldCode.toByte())
        }
    }

    internal companion object {
        /**
         * Decode a [FieldId] from [bytes] starting at [offset].
         *
         * @return a [Pair] of the decoded [FieldId] and the number of bytes consumed.
         */
        fun fromBytes(
            bytes: ByteArray,
            offset: Int = 0,
        ): Pair<FieldId, Int> {
            val firstByte = bytes[offset].toInt() and 0xFF
            val highNibble = firstByte shr 4
            val lowNibble = firstByte and 0x0F

            var pos = offset + 1

            val typeCode =
                if (highNibble == 0) {
                    val tc = bytes[pos].toInt() and 0xFF
                    pos++
                    tc
                } else {
                    highNibble
                }

            val fieldCode =
                if (lowNibble == 0) {
                    val fc = bytes[pos].toInt() and 0xFF
                    pos++
                    fc
                } else {
                    lowNibble
                }

            return FieldId(typeCode = typeCode, fieldCode = fieldCode) to (pos - offset)
        }
    }
}
