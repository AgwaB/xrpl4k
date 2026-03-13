package org.xrpl.sdk.core.util

/**
 * Valid hexadecimal characters (uppercase).
 */
private const val HEX_CHARS_UPPER = "0123456789ABCDEF"

/**
 * Converts a [ByteArray] to its lowercase hexadecimal string representation.
 *
 * Each byte is encoded as exactly two hex characters.
 */
public fun ByteArray.toHexString(): String =
    buildString(size * 2) {
        for (byte in this@toHexString) {
            val unsigned = byte.toInt() and 0xFF
            append(HEX_CHARS_UPPER[unsigned shr 4])
            append(HEX_CHARS_UPPER[unsigned and 0x0F])
        }
    }.lowercase()

/**
 * Converts a hexadecimal string to a [ByteArray].
 *
 * @throws IllegalArgumentException if the string has odd length or contains non-hex characters.
 */
public fun String.hexToByteArray(): ByteArray {
    require(length % 2 == 0) {
        "Hex string must have even length. Got length $length. Ensure no characters are missing."
    }
    return ByteArray(length / 2) { index ->
        val high = charToHexDigit(this[index * 2])
        val low = charToHexDigit(this[index * 2 + 1])
        ((high shl 4) or low).toByte()
    }
}

/**
 * Returns `true` if every character in this string is a valid hexadecimal digit (case-insensitive).
 */
public fun String.isHex(): Boolean = all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

private fun charToHexDigit(char: Char): Int =
    when (char) {
        in '0'..'9' -> char - '0'
        in 'a'..'f' -> char - 'a' + 10
        in 'A'..'F' -> char - 'A' + 10
        else -> throw IllegalArgumentException(
            "Invalid hex character '$char'. Only 0-9, a-f, A-F are allowed.",
        )
    }
