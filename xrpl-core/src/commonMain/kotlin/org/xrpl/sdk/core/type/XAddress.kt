package org.xrpl.sdk.core.type

import kotlin.jvm.JvmInline

/**
 * X-Address format for XRPL accounts.
 *
 * X-Addresses encode both a classic address and an optional destination tag in a single string.
 * Mainnet X-Addresses start with `'X'` and testnet X-Addresses start with `'T'`.
 * Both are exactly 47 characters long and use the Base58 character set.
 *
 * Note: Full X-Address decoding (extracting the classic address and tag) requires Base58Check
 * support from the `xrpl-crypto` module. The [toClassicAddress] and [tag] members will be
 * fully implemented when that module is available.
 *
 * @property value The raw X-Address string.
 */
@JvmInline
public value class XAddress(public val value: String) {
    init {
        require(value.startsWith('X') || value.startsWith('T')) {
            "X-Address must start with 'X' (mainnet) or 'T' (testnet). " +
                "Got '${value.firstOrNull() ?: ""}'. " +
                "Use an X-Address (e.g., \"X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqh\")."
        }
        require(value.length == 47) {
            "X-Address must be exactly 47 characters. Got ${value.length}. " +
                "Verify the X-Address is complete and correctly formatted."
        }
        require(value.all { it in Address.BASE58_ALPHABET }) {
            "X-Address contains invalid character(s). " +
                "Only Base58 characters are allowed (no 0, O, I, l)."
        }
    }

    /**
     * Converts this X-Address to a classic [Address].
     *
     * @throws UnsupportedOperationException always. Use extension function
     *   `XAddress.classicAddress()` from `xrpl-crypto` module instead.
     */
    @Deprecated(
        message = "Use XAddress.classicAddress() extension from xrpl-crypto module.",
        level = DeprecationLevel.WARNING,
    )
    public fun toClassicAddress(): Address {
        throw UnsupportedOperationException(
            "Full X-Address decoding requires xrpl-crypto module. " +
                "Use XAddress.classicAddress() extension function instead.",
        )
    }

    /**
     * The destination tag encoded in this X-Address, or `null` if none is present.
     *
     * @throws UnsupportedOperationException always. Use extension function
     *   `XAddress.destinationTag()` from `xrpl-crypto` module instead.
     */
    @Deprecated(
        message = "Use XAddress.destinationTag() extension from xrpl-crypto module.",
        level = DeprecationLevel.WARNING,
    )
    public val tag: UInt?
        get() = throw UnsupportedOperationException(
            "Full X-Address decoding requires xrpl-crypto module. " +
                "Use XAddress.destinationTag() extension function instead.",
        )
}
