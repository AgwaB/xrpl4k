package org.xrpl.sdk.crypto.signing

import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.crypto.internal.constantTimeEquals

/**
 * In-memory private key material.
 *
 * Implements [AutoCloseable] to zero key bytes when done.
 * Use with `key.use { }` for scoped lifecycle.
 */
public class PrivateKey(
    internal val bytes: ByteArray,
    public val algorithm: KeyAlgorithm,
) : PrivateKeyable, AutoCloseable {
    override fun close() {
        bytes.fill(0)
    }

    /**
     * Provides scoped access to the raw key bytes.
     *
     * Use this instead of accessing [bytes] directly from other modules.
     * The bytes must NOT be stored beyond the scope of [block].
     */
    public fun <T> useBytes(block: (ByteArray) -> T): T = block(bytes)

    /** Never includes key bytes in string representation. */
    override fun toString(): String = "PrivateKey(algorithm=$algorithm)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrivateKey) return false
        return algorithm == other.algorithm && constantTimeEquals(bytes, other.bytes)
    }

    override fun hashCode(): Int = algorithm.hashCode()
}
