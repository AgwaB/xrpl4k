@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

/**
 * XRP Ledger-specific Base58 alphabet.
 *
 * Differs from Bitcoin's Base58 alphabet in character ordering.
 */
internal const val XRP_ALPHABET: String = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz"

/**
 * Pure-Kotlin Base58 encoding and decoding using the XRP alphabet.
 *
 * This implementation does not include checksum logic — see [Base58Check] for
 * encoding with double-SHA-256 checksums.
 */
internal object Base58 {
    private val alphabet = XRP_ALPHABET.toCharArray()
    private val baseMap =
        IntArray(128) { -1 }.also { map ->
            for (i in alphabet.indices) {
                map[alphabet[i].code] = i
            }
        }

    /**
     * Encodes a byte array to a Base58 string using the XRP alphabet.
     *
     * Leading zero bytes are preserved as the first character of the alphabet ('r').
     */
    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""

        // Count leading zeros
        var leadingZeros = 0
        while (leadingZeros < input.size && input[leadingZeros] == 0.toByte()) {
            leadingZeros++
        }

        // Allocate enough space in big-endian base58 representation
        val size = input.size * 138 / 100 + 1 // log(256) / log(58), rounded up
        val b58 = IntArray(size)

        // Process the bytes
        for (i in leadingZeros until input.size) {
            var carry = input[i].toInt() and 0xFF
            var j = size - 1
            while (carry != 0 || j >= 0) {
                if (j < 0) break
                carry += 256 * b58[j]
                b58[j] = carry % 58
                carry /= 58
                j--
            }
        }

        // Skip leading zeros in base58 result
        var firstNonZero = 0
        while (firstNonZero < size && b58[firstNonZero] == 0) {
            firstNonZero++
        }

        // Build result
        return buildString(leadingZeros + size - firstNonZero) {
            repeat(leadingZeros) { append(alphabet[0]) }
            for (i in firstNonZero until size) {
                append(alphabet[b58[i]])
            }
        }
    }

    /**
     * Decodes a Base58 string (XRP alphabet) to a byte array.
     *
     * @throws IllegalArgumentException if the string contains characters not in the XRP alphabet.
     */
    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)

        // Count leading 'r' characters (the zero-byte character in XRP alphabet)
        var leadingZeros = 0
        while (leadingZeros < input.length && input[leadingZeros] == alphabet[0]) {
            leadingZeros++
        }

        // Allocate enough space
        val size = input.length * 733 / 1000 + 1 // log(58) / log(256), rounded up
        val b256 = IntArray(size)

        for (i in leadingZeros until input.length) {
            val c = input[i]
            val digit = if (c.code < 128) baseMap[c.code] else -1
            require(digit >= 0) {
                "Invalid Base58 character '$c' at position $i. " +
                    "Only characters from the XRP Base58 alphabet are allowed."
            }
            var carry = digit
            var j = size - 1
            while (carry != 0 || j >= 0) {
                if (j < 0) break
                carry += 58 * b256[j]
                b256[j] = carry % 256
                carry /= 256
                j--
            }
        }

        // Skip leading zeros in base-256 result
        var firstNonZero = 0
        while (firstNonZero < size && b256[firstNonZero] == 0) {
            firstNonZero++
        }

        // Build result with leading zero bytes
        val result = ByteArray(leadingZeros + size - firstNonZero)
        // leadingZeros bytes are already 0
        for (i in firstNonZero until size) {
            result[leadingZeros + i - firstNonZero] = b256[i].toByte()
        }
        return result
    }
}
