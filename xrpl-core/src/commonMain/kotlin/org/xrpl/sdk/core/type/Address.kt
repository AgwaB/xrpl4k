package org.xrpl.sdk.core.type

import kotlin.jvm.JvmInline

/**
 * Base58-encoded classic XRPL account address.
 *
 * A valid classic address starts with `'r'`, is 25-35 characters long, and contains only
 * characters from the Base58 alphabet (`123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz`).
 *
 * Note: Full Base58Check checksum validation is deferred until the `xrpl-crypto` module is available.
 * This class validates prefix, length, and character set only.
 *
 * @property value The raw address string.
 */
@JvmInline
public value class Address(public val value: String) {
    init {
        require(value.startsWith('r')) {
            "Address must start with 'r'. Got '${value.firstOrNull() ?: ""}'. " +
                "Use a classic r-address (e.g., \"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh\")."
        }
        require(value.length in 25..35) {
            "Address length must be 25-35 characters. Got ${value.length}. " +
                "Verify the address is a valid classic r-address."
        }
        require(value.all { it in BASE58_ALPHABET }) {
            "Address contains invalid character(s). " +
                "Only Base58 characters are allowed (no 0, O, I, l)."
        }
    }

    public companion object {
        /** The Base58 alphabet used by XRPL addresses. */
        private const val BASE58_CHARS: String =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

        internal val BASE58_ALPHABET: Set<Char> = BASE58_CHARS.toSet()
    }
}
