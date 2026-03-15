@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.util

import org.xrpl.sdk.core.type.AccountId

/**
 * Parsed components of an NFTokenID.
 *
 * An NFTokenID is a 64-hex-character (32-byte) identifier that encodes:
 * - [flags]: NFToken flags (UInt16 at bytes 0-1)
 * - [transferFee]: Transfer fee in basis points, 0-50000 (UInt16 at bytes 2-3)
 * - [issuer]: The 20-byte Account ID of the token issuer (bytes 4-23)
 * - [taxon]: The unscrambled taxon (UInt32 at bytes 24-27, XOR-descrambled)
 * - [sequence]: The token mint sequence number (UInt32 at bytes 28-31)
 *
 * Reference: XLS-20d specification and xrpl.js `parseNFTokenID.ts`.
 */
public data class ParsedNFTokenID(
    val nfTokenId: String,
    val flags: UInt,
    val transferFee: UInt,
    val issuer: AccountId,
    val taxon: UInt,
    val sequence: UInt,
)

/**
 * Parses a 64-character hex NFTokenID string into its component fields.
 *
 * The taxon is unscrambled using the XLS-20d linear congruential generator:
 * ```
 * scramble = (384160001 * sequence + 2459) mod 2^32
 * actualTaxon = storedTaxon XOR scramble
 * ```
 *
 * @param nfTokenId A 64-character hexadecimal string.
 * @return The parsed components.
 * @throws IllegalArgumentException if the string is not exactly 64 hex characters.
 */
public fun parseNFTokenID(nfTokenId: String): ParsedNFTokenID {
    require(nfTokenId.length == 64) {
        "NFTokenID must be exactly 64 hex characters. Got length ${nfTokenId.length}."
    }
    require(nfTokenId.isHex()) {
        "NFTokenID must contain only hexadecimal characters."
    }

    val flags = nfTokenId.substring(0, 4).toUInt(16)
    val transferFee = nfTokenId.substring(4, 8).toUInt(16)
    val issuer = AccountId(nfTokenId.substring(8, 48))
    val sequence = nfTokenId.substring(56, 64).toUInt(16)
    val scrambledTaxon = nfTokenId.substring(48, 56).toUInt(16)
    val taxon = unscrambleTaxon(scrambledTaxon, sequence)

    return ParsedNFTokenID(
        nfTokenId = nfTokenId,
        flags = flags,
        transferFee = transferFee,
        issuer = issuer,
        taxon = taxon,
        sequence = sequence,
    )
}

/**
 * Unscrambles a taxon using the XLS-20d linear congruential generator.
 *
 * Per the Hull-Dobell theorem, `f(x) = (m * x + c) mod n` yields a permutation
 * of `[0, n)` when `n` is a power of 2, `m` is congruent to 1 mod 4, and `c` is odd.
 * XLS-20d fixes `m = 384160001` and `c = 2459`, with `n = 2^32`.
 *
 * The XOR operation is its own inverse, so this function both scrambles and unscrambles.
 */
private fun unscrambleTaxon(
    taxon: UInt,
    tokenSeq: UInt,
): UInt {
    val seed: ULong = 384160001uL
    val increment: ULong = 2459uL
    val max: ULong = 4294967296uL // 2^32

    val scramble = ((seed * tokenSeq.toULong()) % max + increment) % max
    return taxon xor scramble.toUInt()
}
