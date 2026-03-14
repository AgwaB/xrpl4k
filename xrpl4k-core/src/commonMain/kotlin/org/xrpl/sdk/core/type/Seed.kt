package org.xrpl.sdk.core.type

import org.xrpl.sdk.core.util.isHex
import kotlin.jvm.JvmInline

/**
 * A 128-bit (16-byte) seed value represented as a 32-character uppercase hex string.
 *
 * Seeds are used to derive XRPL key pairs. Input is case-insensitive; the stored [value]
 * is always normalized to uppercase.
 *
 * @property value The 32-character uppercase hex string.
 */
@JvmInline
public value class Seed private constructor(public val value: String) {
    public companion object {
        /**
         * Creates a [Seed] from the given hex string.
         *
         * @param hex A 32-character hexadecimal string (case-insensitive).
         * @throws IllegalArgumentException if the string is not exactly 32 valid hex characters.
         */
        public operator fun invoke(hex: String): Seed {
            require(hex.length == 32) {
                "Seed must be exactly 32 hex characters (16 bytes). Got ${hex.length}. " +
                    "Provide a valid 128-bit seed in hexadecimal."
            }
            require(hex.isHex()) {
                "Seed contains invalid hex character(s). Got \"${hex.take(10)}...\". " +
                    "Only characters 0-9, a-f, A-F are allowed."
            }
            return Seed(hex.uppercase())
        }
    }
}
