package org.xrpl.sdk.crypto.keys

import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.PublicKey
import org.xrpl.sdk.crypto.internal.constantTimeEquals

/**
 * XRPL key pair -- public key + private key bytes.
 * Implements AutoCloseable to zero the private key on close.
 */
public class KeyPair internal constructor(
    public val publicKey: PublicKey,
    internal val privateKeyBytes: ByteArray,
    public val algorithm: KeyAlgorithm,
) : AutoCloseable {
    override fun close() {
        privateKeyBytes.fill(0)
    }

    override fun toString(): String = "KeyPair(publicKey=$publicKey, algorithm=$algorithm)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyPair) return false
        return publicKey == other.publicKey &&
            algorithm == other.algorithm &&
            constantTimeEquals(privateKeyBytes, other.privateKeyBytes)
    }

    override fun hashCode(): Int = publicKey.hashCode() * 31 + algorithm.hashCode()
}
