package org.xrpl.sdk.crypto.internal

/**
 * Constant-time byte array comparison using XOR accumulator.
 * Prevents timing side-channel attacks when comparing secret data.
 */
internal fun constantTimeEquals(
    a: ByteArray,
    b: ByteArray,
): Boolean {
    if (a.size != b.size) return false
    var result = 0
    for (i in a.indices) {
        result = result or (a[i].toInt() xor b[i].toInt())
    }
    return result == 0
}
