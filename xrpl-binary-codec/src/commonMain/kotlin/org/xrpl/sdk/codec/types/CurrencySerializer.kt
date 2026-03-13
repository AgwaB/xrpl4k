package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.isHex
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL Currency type (20 bytes / 160 bits).
 *
 * Two formats are supported:
 * - **Standard 3-character currency code** (e.g. `"USD"`):
 *   Encoded as 20 bytes with zeros except bytes 12-14 which hold the ASCII code.
 * - **Non-standard (hex) currency code**: 40-character hex string decoded to 20 raw bytes.
 *
 * XRP is represented as 20 zero bytes.
 */
internal object CurrencySerializer : TypeSerializer<String> {
    /** Currency code byte size. */
    private const val CURRENCY_LENGTH: Int = 20

    /** Byte offset where the 3-char ASCII code starts in standard encoding. */
    private const val ASCII_OFFSET: Int = 12

    override fun write(
        writer: BinaryWriter,
        value: String,
    ) {
        val bytes = encode(value)
        writer.writeBytes(bytes)
    }

    override fun read(reader: BinaryReader): String {
        val bytes = reader.readBytes(CURRENCY_LENGTH)
        return decode(bytes)
    }

    /**
     * Encodes a currency string to its 20-byte representation.
     *
     * @param currency Currency string -- either 3-char code, `"XRP"`, or 40-char hex.
     * @return 20-byte currency code.
     */
    internal fun encode(currency: String): ByteArray {
        // XRP is all zeros
        if (currency == "XRP") {
            return ByteArray(CURRENCY_LENGTH)
        }

        // Non-standard: 40-char hex string
        if (currency.length == 40 && currency.isHex()) {
            val bytes = currency.hexToByteArray()
            require(bytes.size == CURRENCY_LENGTH) {
                "Non-standard currency hex must be $CURRENCY_LENGTH bytes"
            }
            return bytes
        }

        // Standard 3-character currency code
        require(currency.length == 3) {
            "Standard currency code must be exactly 3 characters. Got: '$currency'"
        }
        val bytes = ByteArray(CURRENCY_LENGTH)
        bytes[ASCII_OFFSET] = currency[0].code.toByte()
        bytes[ASCII_OFFSET + 1] = currency[1].code.toByte()
        bytes[ASCII_OFFSET + 2] = currency[2].code.toByte()
        return bytes
    }

    /**
     * Decodes 20 bytes into a currency string.
     *
     * @param bytes 20-byte currency code.
     * @return Currency string -- `"XRP"` for all zeros, 3-char code if standard, or 40-char hex.
     */
    internal fun decode(bytes: ByteArray): String {
        require(bytes.size == CURRENCY_LENGTH) {
            "Currency must be $CURRENCY_LENGTH bytes. Got ${bytes.size}"
        }

        // All zeros = XRP
        if (bytes.all { it == 0.toByte() }) {
            return "XRP"
        }

        // Check if it's a standard 3-char code:
        // Bytes 0-11 and 15-19 should all be zero, and byte 12 should not be 0x00
        val isStandard =
            bytes[0] == 0.toByte() &&
                (0 until ASCII_OFFSET).all { bytes[it] == 0.toByte() } &&
                (ASCII_OFFSET + 3 until CURRENCY_LENGTH).all { bytes[it] == 0.toByte() }

        return if (isStandard) {
            val c0 = bytes[ASCII_OFFSET].toInt().toChar()
            val c1 = bytes[ASCII_OFFSET + 1].toInt().toChar()
            val c2 = bytes[ASCII_OFFSET + 2].toInt().toChar()
            "$c0$c1$c2"
        } else {
            bytes.toHexString()
        }
    }
}
