@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.keys

import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.PublicKey
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider

/**
 * secp256k1 key derivation for XRPL.
 *
 * XRPL uses a non-standard 3-step derivation:
 * 1. privateGen = deriveScalar(seed) -- no discriminator
 * 2. publicGen = secp256k1PublicKey(privateGen) -- compressed 33 bytes
 * 3. accountScalar = deriveScalar(publicGen, accountIndex=0) -- discriminator = 0
 * 4. masterPrivate = secp256k1AddPrivateKeys(privateGen, accountScalar) -- (privateGen + accountScalar) mod order
 * 5. masterPublic = secp256k1PublicKey(masterPrivate) -- compressed 33 bytes
 *
 * Reference: xrpl.js/packages/ripple-keypairs/src/signing-schemes/secp256k1/utils.ts
 */
internal object Secp256k1Derivation {
    /**
     * secp256k1 group order:
     * FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141
     */
    private val SECP256K1_ORDER =
        byteArrayOf(
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFE.toByte(),
            0xBA.toByte(), 0xAE.toByte(), 0xDC.toByte(), 0xE6.toByte(),
            0xAF.toByte(), 0x48.toByte(), 0xA0.toByte(), 0x3B.toByte(),
            0xBF.toByte(), 0xD2.toByte(), 0x5E.toByte(), 0x8C.toByte(),
            0xD0.toByte(), 0x36.toByte(), 0x41.toByte(), 0x41.toByte(),
        )

    fun derive(
        seed: ByteArray,
        provider: CryptoProvider,
    ): KeyPair {
        // Step 1: Derive the root private generator scalar (no discriminator)
        val privateGen = deriveScalar(seed, discriminator = null, provider)

        // Step 2: Compute the root public generator
        val publicGen = provider.secp256k1PublicKey(privateGen)

        // Step 3: Derive the account scalar with discriminator = 0 (account index)
        val accountScalar = deriveScalar(publicGen, discriminator = 0, provider)

        // Step 4: masterPrivate = (privateGen + accountScalar) mod order
        val masterPrivate = provider.secp256k1AddPrivateKeys(privateGen, accountScalar)

        // Step 5: Compute the master public key
        val masterPublic = provider.secp256k1PublicKey(masterPrivate)

        val publicKey = PublicKey(masterPublic.toHexString().uppercase())
        return KeyPair(publicKey, masterPrivate, KeyAlgorithm.Secp256k1)
    }

    /**
     * Derives a 32-byte scalar from input bytes, optionally with a discriminator.
     *
     * For each candidate i in 0..0xFFFFFFFF:
     *   input = bytes [+ discriminator as UInt32 BE] + i as UInt32 BE
     *   hash = SHA-512Half(input)
     *   if 0 < hash < secp256k1_order: return hash
     */
    private fun deriveScalar(
        bytes: ByteArray,
        discriminator: Int?,
        provider: CryptoProvider,
    ): ByteArray {
        for (i in 0..Int.MAX_VALUE) {
            val input = buildInput(bytes, discriminator, i)
            val hash = provider.sha512Half(input)

            if (!isAllZeros(hash) && isLessThan(hash, SECP256K1_ORDER)) {
                return hash
            }
        }
        // Practically unreachable
        error("Could not derive valid scalar after 2^32 attempts")
    }

    /**
     * Builds the input for deriveScalar: bytes [+ discrim BE32] + counter BE32.
     */
    private fun buildInput(
        bytes: ByteArray,
        discriminator: Int?,
        counter: Int,
    ): ByteArray {
        val extraSize = (if (discriminator != null) 4 else 0) + 4
        val result = ByteArray(bytes.size + extraSize)
        bytes.copyInto(result)
        var offset = bytes.size

        if (discriminator != null) {
            result[offset++] = (discriminator ushr 24).toByte()
            result[offset++] = (discriminator ushr 16).toByte()
            result[offset++] = (discriminator ushr 8).toByte()
            result[offset++] = discriminator.toByte()
        }

        result[offset++] = (counter ushr 24).toByte()
        result[offset++] = (counter ushr 16).toByte()
        result[offset++] = (counter ushr 8).toByte()
        result[offset] = counter.toByte()

        return result
    }

    /** Returns true if all bytes are zero. */
    private fun isAllZeros(bytes: ByteArray): Boolean {
        for (b in bytes) {
            if (b != 0.toByte()) return false
        }
        return true
    }

    /** Returns true if a < b, interpreting both as unsigned big-endian integers. */
    private fun isLessThan(
        a: ByteArray,
        b: ByteArray,
    ): Boolean {
        for (i in a.indices) {
            val ai = a[i].toInt() and 0xFF
            val bi = b[i].toInt() and 0xFF
            if (ai < bi) return true
            if (ai > bi) return false
        }
        return false // equal
    }
}
