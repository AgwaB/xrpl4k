package org.xrpl.sdk.codec.binary

/**
 * Byte array reader for parsing XRPL binary format.
 *
 * All integer reads are big-endian. Maintains an internal position cursor
 * that advances with each read operation.
 *
 * @param buffer Source byte array to read from.
 */
internal class BinaryReader(private val buffer: ByteArray) {
    private var position: Int = 0

    /**
     * Reads a single byte as an unsigned value in range 0..255.
     *
     * @return Unsigned byte value.
     */
    public fun readUInt8(): Int {
        checkAvailable(1)
        return buffer[position++].toInt() and 0xFF
    }

    /**
     * Reads 2 bytes big-endian as an unsigned value in range 0..65535.
     *
     * @return Unsigned 16-bit value.
     */
    public fun readUInt16(): Int {
        checkAvailable(2)
        return ((buffer[position++].toInt() and 0xFF) shl 8) or
            (buffer[position++].toInt() and 0xFF)
    }

    /**
     * Reads 4 bytes big-endian as an unsigned value returned as a [Long] in range 0..4294967295.
     *
     * @return Unsigned 32-bit value as Long.
     */
    public fun readUInt32(): Long {
        checkAvailable(4)
        return ((buffer[position++].toLong() and 0xFF) shl 24) or
            ((buffer[position++].toLong() and 0xFF) shl 16) or
            ((buffer[position++].toLong() and 0xFF) shl 8) or
            (buffer[position++].toLong() and 0xFF)
    }

    /**
     * Reads 8 bytes big-endian.
     *
     * The XRPL binary codec treats this as an unsigned 64-bit integer for amounts,
     * but Kotlin represents it as a signed [Long] (same bit pattern).
     *
     * @return 64-bit value as Long.
     */
    public fun readUInt64(): Long {
        checkAvailable(8)
        return ((buffer[position++].toLong() and 0xFF) shl 56) or
            ((buffer[position++].toLong() and 0xFF) shl 48) or
            ((buffer[position++].toLong() and 0xFF) shl 40) or
            ((buffer[position++].toLong() and 0xFF) shl 32) or
            ((buffer[position++].toLong() and 0xFF) shl 24) or
            ((buffer[position++].toLong() and 0xFF) shl 16) or
            ((buffer[position++].toLong() and 0xFF) shl 8) or
            (buffer[position++].toLong() and 0xFF)
    }

    /**
     * Reads exactly [count] bytes and returns them as a new array.
     *
     * @param count Number of bytes to read. Must be non-negative.
     * @return New byte array of length [count].
     */
    public fun readBytes(count: Int): ByteArray {
        checkAvailable(count)
        val result = buffer.copyOfRange(position, position + count)
        position += count
        return result
    }

    /**
     * Reads an XRPL variable-length (VL) length prefix.
     *
     * Delegates to [VlEncoding.decode].
     *
     * @return Decoded length value.
     */
    public fun readVlLength(): Int = VlEncoding.decode(this)

    /**
     * Returns the number of bytes not yet consumed.
     *
     * @return Bytes remaining from current position to end of buffer.
     */
    public fun remaining(): Int = buffer.size - position

    /**
     * Returns `true` if all bytes have been consumed.
     *
     * @return `true` when [remaining] is zero.
     */
    public fun isExhausted(): Boolean = position >= buffer.size

    /**
     * Returns the next byte value without advancing the position.
     *
     * @return Unsigned byte value in range 0..255.
     * @throws IllegalArgumentException if the buffer is exhausted.
     */
    public fun peek(): Int {
        checkAvailable(1)
        return buffer[position].toInt() and 0xFF
    }

    private fun checkAvailable(count: Int) {
        require(remaining() >= count) {
            "Buffer underflow. Need $count bytes but only ${remaining()} remaining at position $position."
        }
    }
}
