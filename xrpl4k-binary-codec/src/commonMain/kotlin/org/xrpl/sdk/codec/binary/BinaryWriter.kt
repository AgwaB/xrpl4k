package org.xrpl.sdk.codec.binary

/**
 * Growable byte buffer for writing XRPL binary format.
 *
 * All integer writes are big-endian. The buffer doubles in size automatically
 * whenever capacity is exceeded.
 *
 * @param initialCapacity Initial buffer size in bytes. Defaults to 256.
 */
internal class BinaryWriter(initialCapacity: Int = 256) {
    private var buffer: ByteArray = ByteArray(initialCapacity)
    private var position: Int = 0

    /** Current number of bytes written. */
    public val size: Int get() = position

    /**
     * Writes a single unsigned byte.
     *
     * @param value Integer in range 0..255.
     */
    public fun writeUInt8(value: Int) {
        require(value in 0..255) { "UInt8 value out of range: $value" }
        ensureCapacity(1)
        buffer[position++] = value.toByte()
    }

    /**
     * Writes a 2-byte big-endian unsigned integer.
     *
     * @param value Integer in range 0..65535.
     */
    public fun writeUInt16(value: Int) {
        require(value in 0..65535) { "UInt16 value out of range: $value" }
        ensureCapacity(2)
        buffer[position++] = (value shr 8 and 0xFF).toByte()
        buffer[position++] = (value and 0xFF).toByte()
    }

    /**
     * Writes a 4-byte big-endian unsigned integer.
     *
     * @param value Long in range 0..4294967295.
     */
    public fun writeUInt32(value: Long) {
        require(value in 0..0xFFFFFFFFL) { "UInt32 value out of range: $value" }
        ensureCapacity(4)
        buffer[position++] = (value shr 24 and 0xFF).toByte()
        buffer[position++] = (value shr 16 and 0xFF).toByte()
        buffer[position++] = (value shr 8 and 0xFF).toByte()
        buffer[position++] = (value and 0xFF).toByte()
    }

    /**
     * Writes an 8-byte big-endian value.
     *
     * Used for XRPL amounts. The bit pattern is interpreted as an unsigned 64-bit integer
     * by the XRPL binary codec, but Kotlin represents it as a signed [Long].
     *
     * @param value Long (any bit pattern is accepted; full 64-bit range).
     */
    public fun writeUInt64(value: Long) {
        ensureCapacity(8)
        buffer[position++] = (value ushr 56 and 0xFF).toByte()
        buffer[position++] = (value ushr 48 and 0xFF).toByte()
        buffer[position++] = (value ushr 40 and 0xFF).toByte()
        buffer[position++] = (value ushr 32 and 0xFF).toByte()
        buffer[position++] = (value ushr 24 and 0xFF).toByte()
        buffer[position++] = (value ushr 16 and 0xFF).toByte()
        buffer[position++] = (value ushr 8 and 0xFF).toByte()
        buffer[position++] = (value and 0xFF).toByte()
    }

    /**
     * Appends raw bytes to the buffer.
     *
     * @param bytes Bytes to write.
     */
    public fun writeBytes(bytes: ByteArray) {
        ensureCapacity(bytes.size)
        bytes.copyInto(buffer, position)
        position += bytes.size
    }

    /**
     * Writes an XRPL variable-length (VL) length prefix for the given [length].
     *
     * Delegates to [VlEncoding.encode].
     *
     * @param length Non-negative length value to encode.
     */
    public fun writeVlLength(length: Int) {
        VlEncoding.encode(this, length)
    }

    /**
     * Returns a copy of the bytes written so far.
     *
     * @return Byte array of length [size].
     */
    public fun toByteArray(): ByteArray = buffer.copyOf(position)

    private fun ensureCapacity(needed: Int) {
        if (position + needed > buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, position + needed))
        }
    }
}
