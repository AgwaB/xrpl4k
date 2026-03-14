package org.xrpl.sdk.core.type

import org.xrpl.sdk.core.util.isHex
import kotlin.jvm.JvmInline

/**
 * An XRPL currency code.
 *
 * Currency codes come in two forms:
 * - **Standard (3-character)**: Three ASCII printable characters (codes 0x20-0x7E).
 *   The code `"XRP"` is reserved and cannot be used as a token currency code.
 * - **Non-standard (40-character hex)**: A 160-bit hex string for custom currencies.
 *
 * @property value The currency code string.
 */
@JvmInline
public value class CurrencyCode(public val value: String) {
    init {
        when (value.length) {
            STANDARD_LENGTH -> {
                require(value != "XRP") {
                    "\"XRP\" is reserved and cannot be used as a token currency code. " +
                        "Use XrpDrops for XRP amounts."
                }
                require(value.all { it.code in 0x20..0x7E }) {
                    "Standard currency code must contain only ASCII printable characters (0x20-0x7E). " +
                        "Got \"$value\". Verify the currency code."
                }
            }
            HEX_LENGTH -> {
                require(value.isHex()) {
                    "Non-standard currency code must be a valid 40-character hex string. " +
                        "Got \"${value.take(10)}...\". Only characters 0-9, a-f, A-F are allowed."
                }
            }
            else -> {
                throw IllegalArgumentException(
                    "Currency code must be 3 characters (standard) or 40 characters (hex). " +
                        "Got ${value.length} characters. " +
                        "Use a 3-char ISO code or a 40-char hex string.",
                )
            }
        }
    }

    private companion object {
        const val STANDARD_LENGTH: Int = 3
        const val HEX_LENGTH: Int = 40
    }
}
