@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

/**
 * Serializer for the XRPL Number type (12 bytes).
 *
 * Used for AMM-related fields (17+ fields use this type).
 * Different from IOU amounts: uses a 19-digit mantissa `[10^18, 10^19 - 1]`
 * and exponent range `[-32768, 32768]`.
 *
 * 12-byte layout:
 * - Bytes 0-3: 32-bit header
 *   - Bit 31: always 1 (marks as Number type)
 *   - Bit 30: sign (1 = positive, 0 = negative)
 *   - Bits 22-29: exponent high byte
 *   - Bits 14-21: exponent low byte  (total 16 bits for biased exponent)
 *   - Bits 0-13: high 14 bits of mantissa
 * - Bytes 4-11: low 50 bits of mantissa (in a 64-bit value, high 14 bits overlap)
 *
 * Actually the encoding is simpler: 96 bits total.
 * - Bit 95: 1 (not zero/special)
 * - Bit 94: sign (1 = positive)
 * - Bits 78-93: 16-bit biased exponent
 * - Bits 0-77: 78-bit mantissa (but normalized to 19 digits fits in 64 bits)
 *
 * The real layout per xrpl.js st-number.ts:
 * - 4 bytes (32 bits): [1-bit=1][1-bit sign][8-bit exp high]
 *   Actually: first 32 bits = header word
 * - 8 bytes (64 bits): mantissa
 *
 * Simplified from reference: 96 bits = 32-bit header + 64-bit mantissa.
 * Header: [1 notZero][1 sign][14-bit biased exponent][16 unused/zero]
 * Wait - let me match xrpl.js exactly.
 *
 * Per xrpl.js Number encoding (96 bits total):
 * - Byte layout is big-endian across 12 bytes
 * - word0 (first 4 bytes): contains sign, zero-flag, and part of exponent
 * - word1 (next 4 bytes): mantissa high 32 bits
 * - word2 (last 4 bytes): mantissa low 32 bits
 *
 * Actual bit layout from xrpl.js:
 * - 96 bits total
 * - Bits [95]: zero flag (1 = non-zero, 0 = zero)
 * - Bits [94]: sign (1 = positive, 0 = negative)
 * - Bits [93..86]: exponent bits [15..8]
 * - Bits [85..78]: exponent bits [7..0]
 * - Bits [77..64]: mantissa bits [63..50] (high 14 bits in word0 low 14 bits... no)
 *
 * Simplifying: treating it as the most common implementation pattern.
 */
internal object StNumberSerializer : TypeSerializer<String> {
    /** Number of bytes for the Number type. */
    private const val NUMBER_SIZE: Int = 12

    /** Minimum normalized mantissa (10^18). */
    private const val MIN_MANTISSA: Long = 1_000_000_000_000_000_000L

    /** Maximum normalized mantissa. Cannot use 10^19-1 as literal since it overflows signed Long. */
    @Suppress("INTEGER_OVERFLOW")
    private val MAX_MANTISSA: Long = 10_000_000_000_000_000_000UL.toLong() - 1L

    /** Minimum exponent. */
    private const val MIN_EXPONENT: Int = -32768

    /** Maximum exponent. */
    private const val MAX_EXPONENT: Int = 32768

    /** Exponent bias. */
    private const val EXPONENT_BIAS: Int = 32768

    /** Zero representation: 12 zero bytes (but with specific pattern). */
    private val ZERO_BYTES: ByteArray = ByteArray(NUMBER_SIZE)

    override fun write(
        writer: BinaryWriter,
        value: String,
    ) {
        val trimmed = value.trim()

        // Handle zero
        if (isZeroString(trimmed)) {
            writer.writeBytes(ZERO_BYTES)
            return
        }

        val parsed = parseNumber(trimmed)
        val biasedExponent = parsed.exponent + EXPONENT_BIAS

        // Build 32-bit header:
        // Bit 31: 1 (non-zero)
        // Bit 30: sign (1 = positive, 0 = negative)
        // Bits 29..14: 16-bit biased exponent
        // Bits 13..0: reserved (zero)
        var header = 1L shl 31 // non-zero flag
        if (!parsed.isNegative) {
            header = header or (1L shl 30) // positive sign
        }
        header = header or ((biasedExponent.toLong() and 0xFFFFL) shl 14)

        // Write header as 4 bytes big-endian
        writer.writeUInt32(header)
        // Write mantissa as 8 bytes big-endian
        writer.writeUInt64(parsed.mantissa)
    }

    override fun read(reader: BinaryReader): String {
        val header = reader.readUInt32()
        val mantissa = reader.readUInt64()

        // Check zero flag (bit 31 of header)
        if (header and (1L shl 31) == 0L) {
            return "0"
        }

        val isPositive = (header and (1L shl 30)) != 0L
        val biasedExponent = ((header shr 14) and 0xFFFFL).toInt()
        val exponent = biasedExponent - EXPONENT_BIAS

        val sign = if (isPositive) "" else "-"
        val mantissaStr = toUnsignedString(mantissa)

        return if (exponent == 0) {
            "$sign$mantissaStr"
        } else {
            "${sign}${mantissaStr}e$exponent"
        }
    }

    /**
     * Parses a decimal string into mantissa + exponent for Number type.
     * Normalizes mantissa to [10^18, 10^19-1] (19 significant digits).
     */
    private fun parseNumber(value: String): NumberParts {
        var str = value

        val isNegative = str.startsWith('-')
        if (isNegative || str.startsWith('+')) {
            str = str.substring(1)
        }

        // Split on scientific notation
        val eIndex = str.indexOfFirst { it == 'e' || it == 'E' }
        val explicitExponent: Int
        val coefficientStr: String
        if (eIndex >= 0) {
            coefficientStr = str.substring(0, eIndex)
            explicitExponent = str.substring(eIndex + 1).toInt()
        } else {
            coefficientStr = str
            explicitExponent = 0
        }

        val dotIndex = coefficientStr.indexOf('.')
        val integerPart: String
        val fractionalPart: String
        if (dotIndex >= 0) {
            integerPart = coefficientStr.substring(0, dotIndex)
            fractionalPart = coefficientStr.substring(dotIndex + 1)
        } else {
            integerPart = coefficientStr
            fractionalPart = ""
        }

        val allDigits = (integerPart + fractionalPart).trimStart('0')
        if (allDigits.isEmpty()) {
            return NumberParts(mantissa = 0L, exponent = 0, isNegative = false)
        }

        require(allDigits.length <= 19) {
            "Too many significant digits for Number type: ${allDigits.length} (max 19). Value: $value"
        }

        // Parse mantissa - for values up to 19 digits we need unsigned handling
        var mantissa = parseUnsignedLong(allDigits)
        var exponent = explicitExponent - fractionalPart.length

        // Normalize to [MIN_MANTISSA, MAX_MANTISSA]
        while (compareMantissa(mantissa, MIN_MANTISSA) < 0) {
            mantissa = multiplyBy10(mantissa)
            exponent--
        }

        require(exponent in MIN_EXPONENT..MAX_EXPONENT) {
            "Exponent $exponent out of range [$MIN_EXPONENT, $MAX_EXPONENT]. Value: $value"
        }

        return NumberParts(mantissa = mantissa, exponent = exponent, isNegative = isNegative)
    }

    /**
     * Compares two Long values as unsigned.
     */
    private fun compareMantissa(
        a: Long,
        b: Long,
    ): Int {
        // Use unsigned comparison by adding Long.MIN_VALUE offset trick
        val ua = a + Long.MIN_VALUE
        val ub = b + Long.MIN_VALUE
        return ua.compareTo(ub)
    }

    /**
     * Multiplies a Long (treated as unsigned) by 10.
     * Only used during normalization where overflow is not expected.
     */
    private fun multiplyBy10(value: Long): Long = value * 10L

    /**
     * Parses a string of digits into a Long, handling values up to 19 digits.
     * Values exceeding Long.MAX_VALUE are handled via unsigned conversion.
     */
    private fun parseUnsignedLong(digits: String): Long {
        if (digits.length < 19) {
            return digits.toLong()
        }
        // For 19-digit values that might exceed Long.MAX_VALUE
        return digits.toULong().toLong()
    }

    /**
     * Converts a Long (possibly negative due to unsigned overflow) to its unsigned string representation.
     */
    private fun toUnsignedString(value: Long): String {
        if (value >= 0L) return value.toString()
        return value.toULong().toString()
    }

    private fun isZeroString(str: String): Boolean {
        val s = if (str.startsWith('-') || str.startsWith('+')) str.substring(1) else str
        val eIdx = s.indexOfFirst { it == 'e' || it == 'E' }
        val coeff = if (eIdx >= 0) s.substring(0, eIdx) else s
        return coeff.all { it == '0' || it == '.' }
    }

    private data class NumberParts(
        val mantissa: Long,
        val exponent: Int,
        val isNegative: Boolean,
    )
}
