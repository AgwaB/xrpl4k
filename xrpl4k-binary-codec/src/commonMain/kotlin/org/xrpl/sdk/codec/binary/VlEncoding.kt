package org.xrpl.sdk.codec.binary

/**
 * XRPL variable-length (VL) encoding and decoding.
 *
 * Encodes non-negative lengths using 1, 2, or 3 bytes:
 * - **1 byte** (0–192): the length value directly.
 * - **2 bytes** (193–12480):
 *   - byte 0 = `193 + ((length - 193) shr 8)`
 *   - byte 1 = `(length - 193) and 0xFF`
 * - **3 bytes** (12481–918744):
 *   - byte 0 = `241 + ((length - 12481) shr 16)`
 *   - byte 1 = `((length - 12481) shr 8) and 0xFF`
 *   - byte 2 = `(length - 12481) and 0xFF`
 *
 * Maximum encodable length is 918744.
 */
internal object VlEncoding {
    private const val MAX_SINGLE = 192
    private const val MAX_DOUBLE = 12480
    private const val MAX_TRIPLE = 918744

    /**
     * Encodes [length] as a VL prefix and writes it to [writer].
     *
     * @param writer Destination [BinaryWriter].
     * @param length Non-negative length to encode. Must be in range 0..[MAX_TRIPLE].
     * @throws IllegalArgumentException if [length] is out of the supported range.
     */
    public fun encode(
        writer: BinaryWriter,
        length: Int,
    ) {
        require(length in 0..MAX_TRIPLE) {
            "VL length out of range: $length (max $MAX_TRIPLE)"
        }
        when {
            length <= MAX_SINGLE -> {
                writer.writeUInt8(length)
            }
            length <= MAX_DOUBLE -> {
                val adjusted = length - 193
                writer.writeUInt8(193 + (adjusted shr 8))
                writer.writeUInt8(adjusted and 0xFF)
            }
            else -> {
                val adjusted = length - 12481
                writer.writeUInt8(241 + (adjusted shr 16))
                writer.writeUInt8((adjusted shr 8) and 0xFF)
                writer.writeUInt8(adjusted and 0xFF)
            }
        }
    }

    /**
     * Reads a VL prefix from [reader] and returns the decoded length.
     *
     * @param reader Source [BinaryReader].
     * @return Decoded non-negative length.
     * @throws IllegalArgumentException if the encoded value is invalid or the buffer is exhausted.
     */
    public fun decode(reader: BinaryReader): Int {
        val first = reader.readUInt8()
        return when {
            first <= 192 -> first
            first <= 240 -> {
                val second = reader.readUInt8()
                193 + ((first - 193) shl 8) + second
            }
            first <= 254 -> {
                val second = reader.readUInt8()
                val third = reader.readUInt8()
                12481 + ((first - 241) shl 16) + (second shl 8) + third
            }
            else -> throw IllegalArgumentException(
                "Invalid VL first byte: $first",
            )
        }
    }
}
