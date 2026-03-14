package org.xrpl.sdk.core.type

/**
 * The cryptographic algorithm used for XRPL key pairs.
 *
 * XRPL supports two algorithms:
 * - [Ed25519]: Modern elliptic curve algorithm (RFC 8032). Public keys are prefixed with `0xED`.
 * - [Secp256k1]: Bitcoin-style ECDSA. Default for XRPL accounts created without specifying an algorithm.
 */
public sealed interface KeyAlgorithm {
    /** The lowercase algorithm name as used in XRPL protocol strings. */
    public val name: String

    /** Ed25519 algorithm (RFC 8032). */
    public data object Ed25519 : KeyAlgorithm {
        override val name: String = "ed25519"
    }

    /** secp256k1 ECDSA algorithm. */
    public data object Secp256k1 : KeyAlgorithm {
        override val name: String = "secp256k1"
    }
}
