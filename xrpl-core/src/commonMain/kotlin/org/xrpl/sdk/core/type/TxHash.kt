package org.xrpl.sdk.core.type

import org.xrpl.sdk.core.util.isHex
import kotlin.jvm.JvmInline

/**
 * A 256-bit XRPL transaction hash represented as a 64-character uppercase hex string.
 *
 * Input is case-insensitive; the stored [value] is always normalized to uppercase.
 *
 * @property value The 64-character uppercase hex string.
 */
@JvmInline
public value class TxHash private constructor(public val value: String) {
    public companion object {
        /**
         * Creates a [TxHash] from the given hex string.
         *
         * @param hex A 64-character hexadecimal string (case-insensitive).
         * @throws IllegalArgumentException if the string is not exactly 64 valid hex characters.
         */
        public operator fun invoke(hex: String): TxHash {
            require(hex.length == 64) {
                "TxHash must be exactly 64 hex characters. Got ${hex.length}. " +
                    "Provide a full 256-bit transaction hash."
            }
            require(hex.isHex()) {
                "TxHash contains invalid hex character(s). Got \"${hex.take(10)}...\". " +
                    "Only characters 0-9, a-f, A-F are allowed."
            }
            return TxHash(hex.uppercase())
        }
    }
}
