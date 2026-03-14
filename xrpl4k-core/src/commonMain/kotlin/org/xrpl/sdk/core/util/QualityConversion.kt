@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.util

/**
 * One billion — the scale factor for XRPL billionths format used
 * by TransferRate, QualityIn, and QualityOut.
 */
public const val ONE_BILLION: Long = 1_000_000_000L

private const val TWO_BILLION: Long = 2_000_000_000L
private const val MAX_DECIMAL_PLACES = 9

/**
 * Converts a decimal string (0 to 1.00) to the billionths TransferRate format.
 *
 * - `"0"` → `0` (special case: no transfer fee)
 * - `"0.5"` → `1500000000` (50% fee)
 * - `"1"` → `2000000000` (100% fee, maximum)
 *
 * @param decimal a string decimal between 0 and 1.00 inclusive.
 * @return the transfer rate in billionths format, or `0` if the result equals [ONE_BILLION].
 * @throws IllegalArgumentException if the value is out of range, not a number, or exceeds precision.
 */
public fun decimalToTransferRate(decimal: String): Long {
    val scaled = decimalStringToScaled(decimal)
    val rate = scaled + ONE_BILLION

    require(rate in ONE_BILLION..TWO_BILLION) {
        "Decimal value must be between 0 and 1.00."
    }

    return if (rate == ONE_BILLION) 0L else rate
}

/**
 * Converts a TransferRate in billionths format to a decimal string.
 *
 * - `0` → `"0"` (special case: no transfer fee)
 * - `1500000000` → `"0.5"`
 *
 * @param rate the transfer rate in billionths format.
 * @return the decimal string representation.
 * @throws IllegalArgumentException if rate is negative or not an integer-like value.
 */
public fun transferRateToDecimal(rate: Long): String {
    if (rate == 0L) return "0"

    require(rate >= ONE_BILLION) {
        "Error decoding, negative transfer rate"
    }

    return scaledToDecimalString(rate - ONE_BILLION)
}

/**
 * Converts a string percent (e.g. `"50%"`) to the billionths TransferRate format.
 *
 * @param percent a string ending with `%` (e.g. `"50%"`, `"0.5%"`).
 * @return the transfer rate in billionths format.
 * @throws IllegalArgumentException if the percent is not valid.
 */
public fun percentToTransferRate(percent: String): Long = decimalToTransferRate(percentToDecimal(percent))

/**
 * Converts a decimal string to the billionths Quality format (QualityIn/QualityOut).
 *
 * - `"1"` → `0` (special case: no quality adjustment, 1:1 exchange)
 * - `"0.5"` → `500000000`
 *
 * @param decimal a string decimal value.
 * @return the quality in billionths format, or `0` if the result equals [ONE_BILLION].
 * @throws IllegalArgumentException if the value is negative, not a number, or exceeds precision.
 */
public fun decimalToQuality(decimal: String): Long {
    val scaled = decimalStringToScaled(decimal)

    require(scaled >= 0) {
        "Cannot have negative Quality"
    }

    return if (scaled == ONE_BILLION) 0L else scaled
}

/**
 * Converts a Quality in billionths format to a decimal string.
 *
 * - `0` → `"1"` (special case: no quality adjustment, 1:1 exchange)
 * - `500000000` → `"0.5"`
 *
 * @param quality the quality in billionths format.
 * @return the decimal string representation.
 * @throws IllegalArgumentException if quality is negative.
 */
public fun qualityToDecimal(quality: Long): String {
    require(quality >= 0) {
        "Negative quality not allowed"
    }

    if (quality == 0L) return "1"

    return scaledToDecimalString(quality)
}

/**
 * Converts a string percent (e.g. `"50%"`) to the billionths Quality format.
 *
 * @param percent a string ending with `%` (e.g. `"50%"`, `"0.034%"`).
 * @return the quality in billionths format.
 * @throws IllegalArgumentException if the percent is not valid.
 */
public fun percentToQuality(percent: String): Long = decimalToQuality(percentToDecimal(percent))

/**
 * Parses a percent string like `"50%"` to a decimal string like `"0.5"`.
 */
private fun percentToDecimal(percent: String): String {
    require(percent.endsWith('%')) {
        "Value $percent must end with %"
    }

    val parts = percent.split('%').filter { it.isNotEmpty() }
    require(parts.size == 1) {
        "Value $percent contains too many % signs"
    }

    return scaledToDecimalString(decimalStringToScaled(parts[0]) / 100)
}

/**
 * Converts a decimal string to a scaled Long value (multiplied by [ONE_BILLION]).
 *
 * Parses the string by splitting on `.` to avoid floating-point precision issues.
 * Supports up to 9 decimal places.
 *
 * @throws IllegalArgumentException on invalid input or precision overflow.
 */
private fun decimalStringToScaled(decimal: String): Long {
    val trimmed = decimal.trim()
    require(trimmed.isNotEmpty()) { "Value is not a number" }

    val negative = trimmed.startsWith('-')
    val abs = if (negative) trimmed.substring(1) else trimmed

    val dotIndex = abs.indexOf('.')

    val intPart: String
    val fracPart: String

    if (dotIndex < 0) {
        intPart = abs
        fracPart = ""
    } else {
        intPart = abs.substring(0, dotIndex)
        fracPart = abs.substring(dotIndex + 1)
    }

    require(fracPart.length <= MAX_DECIMAL_PLACES) {
        "Decimal exceeds maximum precision."
    }

    val intValue =
        if (intPart.isEmpty()) {
            0L
        } else {
            intPart.toLongOrNull() ?: throw IllegalArgumentException("Value is not a number")
        }

    val fracValue =
        if (fracPart.isEmpty()) {
            0L
        } else {
            val padded = fracPart.padEnd(MAX_DECIMAL_PLACES, '0')
            padded.toLongOrNull() ?: throw IllegalArgumentException("Value is not a number")
        }

    val result = intValue * ONE_BILLION + fracValue
    return if (negative) -result else result
}

/**
 * Converts a scaled Long value (×10⁹) back to a decimal string.
 */
private fun scaledToDecimalString(scaled: Long): String {
    if (scaled == 0L) return "0"

    val negative = scaled < 0
    val abs = if (negative) -scaled else scaled

    val intPart = abs / ONE_BILLION
    val fracPart = abs % ONE_BILLION

    val result =
        if (fracPart == 0L) {
            intPart.toString()
        } else {
            val fracStr = fracPart.toString().padStart(MAX_DECIMAL_PLACES, '0').trimEnd('0')
            "$intPart.$fracStr"
        }

    return if (negative) "-$result" else result
}
