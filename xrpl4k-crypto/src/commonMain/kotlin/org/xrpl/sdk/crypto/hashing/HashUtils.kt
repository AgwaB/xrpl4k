package org.xrpl.sdk.crypto.hashing

import org.xrpl.sdk.crypto.CryptoProvider

/**
 * Computes SHA-512Half of the concatenation of [prefix] and [data].
 */
public fun sha512HalfWithPrefix(
    prefix: ByteArray,
    data: ByteArray,
    provider: CryptoProvider,
): ByteArray {
    val input = ByteArray(prefix.size + data.size)
    prefix.copyInto(input)
    data.copyInto(input, prefix.size)
    return provider.sha512Half(input)
}
