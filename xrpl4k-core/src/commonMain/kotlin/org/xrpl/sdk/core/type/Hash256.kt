package org.xrpl.sdk.core.type

import org.xrpl.sdk.core.util.isHex
import kotlin.jvm.JvmInline

/**
 * A generic 256-bit hash represented as a 64-character uppercase hex string.
 *
 * Used for ledger hashes, account state hashes, and other 256-bit identifiers in the XRPL protocol.
 * Input is case-insensitive; the stored [value] is always normalized to uppercase.
 *
 * @property value The 64-character uppercase hex string.
 */
@JvmInline
public value class Hash256 private constructor(public val value: String) {
    public companion object {
        /**
         * Creates a [Hash256] from the given hex string.
         *
         * @param hex A 64-character hexadecimal string (case-insensitive).
         * @throws IllegalArgumentException if the string is not exactly 64 valid hex characters.
         */
        public operator fun invoke(hex: String): Hash256 {
            require(hex.length == 64) {
                "Hash256 must be exactly 64 hex characters. Got ${hex.length}. " +
                    "Provide a full 256-bit hash value."
            }
            require(hex.isHex()) {
                "Hash256 contains invalid hex character(s). Got \"${hex.take(10)}...\". " +
                    "Only characters 0-9, a-f, A-F are allowed."
            }
            return Hash256(hex.uppercase())
        }
    }
}
