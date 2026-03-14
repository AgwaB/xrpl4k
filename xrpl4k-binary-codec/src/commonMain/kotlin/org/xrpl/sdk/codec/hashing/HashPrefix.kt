package org.xrpl.sdk.codec.hashing

/**
 * Hash prefixes used in XRPL for different hash computations.
 * Each prefix is a 4-byte value prepended before hashing.
 *
 * These prefixes are inserted before the source material used to
 * generate various hashes. This is done to put each hash in its own
 * "space." This way, two different types of objects with the
 * same binary data will produce different hashes.
 *
 * Each prefix is a 4-byte value with the last byte set to zero
 * and the first three bytes formed from the ASCII equivalent of
 * some arbitrary string. For example "TXN".
 */
@Suppress("MagicNumber")
public enum class HashPrefix(public val bytes: ByteArray) {
    /** Transaction plus signature to give transaction ID — 'TXN\0' */
    TRANSACTION_ID(byteArrayOf(0x54, 0x58, 0x4E, 0x00)),

    /** Transaction plus metadata — 'SND\0' */
    TRANSACTION_NODE(byteArrayOf(0x53, 0x4E, 0x44, 0x00)),

    /** Inner node in tree — 'MIN\0' */
    INNER_NODE(byteArrayOf(0x4D, 0x49, 0x4E, 0x00)),

    /** Leaf node in tree — 'MLN\0' */
    LEAF_NODE(byteArrayOf(0x4D, 0x4C, 0x4E, 0x00)),

    /** Inner transaction to sign — 'STX\0' */
    TRANSACTION_SIGN(byteArrayOf(0x53, 0x54, 0x58, 0x00)),

    /** Inner transaction to sign (TESTNET) — 'stx\0' */
    TRANSACTION_SIGN_TESTNET(byteArrayOf(0x73, 0x74, 0x78, 0x00)),

    /** Inner transaction to multisign — 'SMT\0' */
    TRANSACTION_MULTISIGN(byteArrayOf(0x53, 0x4D, 0x54, 0x00)),

    /** Ledger header — 'LWR\0' */
    LEDGER(byteArrayOf(0x4C, 0x57, 0x52, 0x00)),
}
