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
    // We still iterate over the shorter array to avoid index-out-of-bounds
    // while keeping the timing roughly constant for same-sized inputs.
    var result = a.size xor b.size
    val minLen = minOf(a.size, b.size)
    for (i in 0 until minLen) {
        result = result or (a[i].toInt() xor b[i].toInt())
    }
    return result == 0
}
