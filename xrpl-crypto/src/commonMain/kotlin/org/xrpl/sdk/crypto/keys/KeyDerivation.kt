package org.xrpl.sdk.crypto.keys

import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Derives key pairs from seeds using XRPL-specific algorithms.
 */
public object KeyDerivation {
    /**
     * Derive a key pair from a 16-byte seed.
     * Caller must call close() on the returned KeyPair when done.
     */
    public fun deriveKeyPair(
        seed: ByteArray,
        algorithm: KeyAlgorithm,
        provider: CryptoProvider = platformCryptoProvider(),
    ): KeyPair {
        require(seed.size == 16) { "Seed must be exactly 16 bytes. Got ${seed.size}." }
        return when (algorithm) {
            KeyAlgorithm.Ed25519 -> Ed25519Derivation.derive(seed, provider)
            KeyAlgorithm.Secp256k1 -> Secp256k1Derivation.derive(seed, provider)
        }
    }

    /**
     * Generate a random 16-byte seed.
     */
    public fun generateSeed(provider: CryptoProvider = platformCryptoProvider()): ByteArray = provider.secureRandom(16)
}
