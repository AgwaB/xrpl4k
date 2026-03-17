package org.xrpl.sdk.crypto.internal

/**
 * Constant-time byte array comparison using XOR accumulator.
 * Prevents timing side-channel attacks when comparing secret data.
 */
internal fun constantTimeEquals(
    a: ByteArray,
    b: ByteArray,
): Boolean {
    // XOR the sizes — if they differ, `result` will be non-zero.
    // Always iterate maxLen to ensure constant timing regardless of input lengths.
    var result = a.size xor b.size
    val maxLen = maxOf(a.size, b.size)
    for (i in 0 until maxLen) {
        val ai = if (i < a.size) a[i].toInt() else 0
        val bi = if (i < b.size) b[i].toInt() else 0
        result = result or (ai xor bi)
    }
    return result == 0
}
