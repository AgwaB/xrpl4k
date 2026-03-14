package org.xrpl.sdk.core.type

import org.xrpl.sdk.core.util.isHex
import kotlin.jvm.JvmInline

/**
 * A compressed XRPL public key represented as a 66-character uppercase hex string (33 bytes).
 *
 * Input is case-insensitive; the stored [value] is always normalized to uppercase.
 *
 * @property value The 66-character uppercase hex string.
 */
@JvmInline
public value class PublicKey private constructor(public val value: String) {
    public companion object {
        /**
         * Creates a [PublicKey] from the given hex string.
         *
         * @param hex A 66-character hexadecimal string (case-insensitive).
         * @throws IllegalArgumentException if the string is not exactly 66 valid hex characters.
         */
        public operator fun invoke(hex: String): PublicKey {
            require(hex.length == 66) {
                "PublicKey must be exactly 66 hex characters (33 bytes compressed). Got ${hex.length}. " +
                    "Provide a valid compressed public key in hexadecimal."
            }
            require(hex.isHex()) {
                "PublicKey contains invalid hex character(s). Got \"${hex.take(10)}...\". " +
                    "Only characters 0-9, a-f, A-F are allowed."
            }
            return PublicKey(hex.uppercase())
        }
    }
}
