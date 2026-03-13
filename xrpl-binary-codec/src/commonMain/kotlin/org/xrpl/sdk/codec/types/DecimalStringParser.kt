package org.xrpl.sdk.codec.types

/**
 * Result of parsing a decimal string into its components.
 *
 * @param mantissa Absolute mantissa value (always positive).
 * @param exponent Exponent such that the value equals `mantissa * 10^exponent`.
 * @param isNegative `true` if the original value was negative.
 * @param isZero `true` if the value is zero.
 */
internal data class ParsedDecimal(
    val mantissa: Long,
    val exponent: Int,
    val isNegative: Boolean,
    val isZero: Boolean,
)

/**
 * Parses decimal string representations into [ParsedDecimal] for XRPL IOU amount encoding.
 *
 * Handles formats such as: `"1.5"`, `"0.00123"`, `"1.23e5"`, `"-1.23e-5"`, `"100"`.
 *
 * After parsing, the mantissa is normalized to the range `[10^15, 10^16 - 1]`
 * (exactly 16 significant digits). Values with more than 16 significant digits
 * are rejected rather than silently truncated.
 */
internal object DecimalStringParser {
    /** Minimum normalized mantissa (10^15). */
    private const val MIN_MANTISSA: Long = 1_000_000_000_000_000L

    /** Maximum normalized mantissa (10^16 - 1). */
    private const val MAX_MANTISSA: Long = 9_999_999_999_999_999L

    /** Minimum exponent after normalization. */
    private const val MIN_EXPONENT: Int = -96

    /** Maximum exponent after normalization. */
    private const val MAX_EXPONENT: Int = 80

    /**
     * Parses a decimal string value into a [ParsedDecimal].
     *
     * @param value Decimal string (e.g. `"1.5"`, `"-0.001"`, `"1e10"`).
     * @return Parsed and normalized decimal.
     * @throws IllegalArgumentException if the value has more than 16 significant digits
     *   or the normalized exponent is out of range.
     */
    public fun parse(value: String): ParsedDecimal {
        require(value.isNotBlank()) { "Decimal string must not be blank" }

        var str = value.trim()

        // Handle sign
        val isNegative = str.startsWith('-')
        if (isNegative || str.startsWith('+')) {
            str = str.substring(1)
        }

        // Handle zero
        if (isZeroString(str)) {
            return ParsedDecimal(
                mantissa = 0L,
                exponent = 0,
                isNegative = false,
                isZero = true,
            )
        }

        // Split on 'e' or 'E' for scientific notation
        val explicitExponent: Int
        val coefficientStr: String
        val eIndex = str.indexOfFirst { it == 'e' || it == 'E' }
        if (eIndex >= 0) {
            coefficientStr = str.substring(0, eIndex)
            explicitExponent = str.substring(eIndex + 1).toInt()
        } else {
            coefficientStr = str
            explicitExponent = 0
        }

        // Parse the coefficient: may contain a decimal point
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

        // Build digit string (all significant digits concatenated)
        val allDigits = integerPart + fractionalPart

        // Strip leading zeros to find significant digits
        val stripped = allDigits.trimStart('0')
        if (stripped.isEmpty()) {
            return ParsedDecimal(
                mantissa = 0L,
                exponent = 0,
                isNegative = false,
                isZero = true,
            )
        }

        // Count significant digits -- reject > 16
        require(stripped.length <= 16) {
            "Too many significant digits: ${stripped.length} (max 16). " +
                "Value: $value"
        }

        // The mantissa from the raw digits (before normalization)
        var mantissa = stripped.toLong()
        // Exponent adjustment from the fractional part
        var exponent = explicitExponent - fractionalPart.length

        // Normalize mantissa to [MIN_MANTISSA, MAX_MANTISSA]
        while (mantissa < MIN_MANTISSA) {
            mantissa *= 10L
            exponent--
        }
        // This should not happen if we capped at 16 digits, but guard anyway
        while (mantissa > MAX_MANTISSA) {
            // Only allow if trailing digit is 0 (no precision loss)
            require(mantissa % 10L == 0L) {
                "Cannot normalize mantissa without precision loss: $mantissa"
            }
            mantissa /= 10L
            exponent++
        }

        require(exponent in MIN_EXPONENT..MAX_EXPONENT) {
            "Exponent $exponent out of range [$MIN_EXPONENT, $MAX_EXPONENT] after normalization. " +
                "Value: $value"
        }

        return ParsedDecimal(
            mantissa = mantissa,
            exponent = exponent,
            isNegative = isNegative,
            isZero = false,
        )
    }

    /**
     * Checks whether a string (with sign already stripped) represents zero.
     */
    private fun isZeroString(str: String): Boolean {
        val eIndex = str.indexOfFirst { it == 'e' || it == 'E' }
        val coefficient = if (eIndex >= 0) str.substring(0, eIndex) else str
        return coefficient.all { it == '0' || it == '.' }
    }
}
