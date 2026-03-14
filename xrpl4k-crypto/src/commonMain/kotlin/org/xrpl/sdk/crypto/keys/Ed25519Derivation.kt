package org.xrpl.sdk.crypto.keys

import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.PublicKey
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider

/**
 * Ed25519 key derivation for XRPL.
 *
 * Algorithm:
 * 1. privateKey = SHA-512Half(seed) -- 32 bytes
 * 2. publicKeyRaw = ed25519PublicKey(privateKey) -- 32 bytes
 * 3. publicKeyPrefixed = 0xED + publicKeyRaw -- 33 bytes
 * 4. Return KeyPair
 */
internal object Ed25519Derivation {
    private const val ED_PREFIX_BYTE: Int = 0xED

    fun derive(
        seed: ByteArray,
        provider: CryptoProvider,
    ): KeyPair {
        val privateKey = provider.sha512Half(seed)
        val publicKeyRaw = provider.ed25519PublicKey(privateKey)

        // Prefix with 0xED to match XRPL 33-byte compressed public key format
        val publicKeyPrefixed = ByteArray(33)
        publicKeyPrefixed[0] = ED_PREFIX_BYTE.toByte()
        publicKeyRaw.copyInto(publicKeyPrefixed, destinationOffset = 1)

        val publicKey = PublicKey(publicKeyPrefixed.toHexString().uppercase())
        return KeyPair(publicKey, privateKey, KeyAlgorithm.Ed25519)
    }
}
