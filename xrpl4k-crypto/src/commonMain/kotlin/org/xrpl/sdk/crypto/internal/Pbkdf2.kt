@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.internal

import org.xrpl.sdk.crypto.CryptoProvider

/**
 * PBKDF2-HMAC-SHA512 key derivation (RFC 2898).
 * Used internally for BIP39 mnemonic -> seed conversion.
 */
internal fun pbkdf2HmacSha512(
    password: ByteArray,
    salt: ByteArray,
    iterations: Int,
    keyLength: Int,
    provider: CryptoProvider,
): ByteArray {
    val hashLength = 64 // SHA-512 output size
    val blocksNeeded = (keyLength + hashLength - 1) / hashLength
    val result = ByteArray(blocksNeeded * hashLength)

    for (blockIndex in 1..blocksNeeded) {
        // U1 = HMAC-SHA512(password, salt || INT_32_BE(blockIndex))
        val saltWithIndex = ByteArray(salt.size + 4)
        salt.copyInto(saltWithIndex)
        saltWithIndex[salt.size] = (blockIndex ushr 24).toByte()
        saltWithIndex[salt.size + 1] = (blockIndex ushr 16).toByte()
        saltWithIndex[salt.size + 2] = (blockIndex ushr 8).toByte()
        saltWithIndex[salt.size + 3] = blockIndex.toByte()

        var u = provider.hmacSha512(password, saltWithIndex)
        val block = u.copyOf()

        // U2..Un: XOR each iteration result into block
        for (iter in 2..iterations) {
            u = provider.hmacSha512(password, u)
            for (j in block.indices) {
                block[j] = (block[j].toInt() xor u[j].toInt()).toByte()
            }
        }

        block.copyInto(result, (blockIndex - 1) * hashLength)
    }

    return result.copyOfRange(0, keyLength)
}
