@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.internal

import org.xrpl.sdk.crypto.CryptoProvider

/**
 * BIP32 HD key derivation result.
 */
internal class Bip32Key(
    val privateKey: ByteArray,
    val chainCode: ByteArray,
)

/**
 * Derives a BIP32 master key from a BIP39 seed.
 *
 * HMAC-SHA512(key="Bitcoin seed", data=seed)
 * Left 32 bytes = master private key, right 32 bytes = chain code.
 */
internal fun deriveFromSeed(seed: ByteArray, provider: CryptoProvider): Bip32Key {
    val result = provider.hmacSha512("Bitcoin seed".encodeToByteArray(), seed)
    return Bip32Key(result.copyOfRange(0, 32), result.copyOfRange(32, 64))
}

/**
 * Derives a BIP32 child key from a parent key at the given index.
 *
 * Hardened indices have bit 0x80000000 set.
 */
internal fun deriveChild(parent: Bip32Key, index: Long, provider: CryptoProvider): Bip32Key {
    val isHardened = index >= 0x80000000L
    val data: ByteArray

    if (isHardened) {
        // data = 0x00 || parentPrivateKey(32 bytes) || index(4 bytes BE)
        data = ByteArray(1 + 32 + 4)
        data[0] = 0x00
        parent.privateKey.copyInto(data, 1)
        writeUInt32BE(data, 33, index)
    } else {
        // data = parentPublicKey(33 bytes compressed) || index(4 bytes BE)
        val parentPublicKey = provider.secp256k1PublicKey(parent.privateKey)
        data = ByteArray(33 + 4)
        parentPublicKey.copyInto(data, 0)
        writeUInt32BE(data, 33, index)
    }

    val hmac = provider.hmacSha512(parent.chainCode, data)
    val il = hmac.copyOfRange(0, 32)
    val ir = hmac.copyOfRange(32, 64)

    // childPrivateKey = (parse256(IL) + parentKey) mod n
    val childPrivateKey = provider.secp256k1AddPrivateKeys(il, parent.privateKey)

    return Bip32Key(childPrivateKey, ir)
}

/**
 * Derives a BIP32 key along a derivation path like "m/44'/144'/0'/0/0".
 *
 * Apostrophe (') denotes hardened derivation (index + 0x80000000).
 */
internal fun derivePath(masterKey: Bip32Key, path: String, provider: CryptoProvider): Bip32Key {
    val components = path.trim().split("/")
    require(components.isNotEmpty() && components[0] == "m") {
        "Derivation path must start with 'm'"
    }

    var current = masterKey
    for (i in 1 until components.size) {
        val component = components[i]
        val hardened = component.endsWith("'")
        val indexStr = if (hardened) component.dropLast(1) else component
        val index = indexStr.toLong() + if (hardened) 0x80000000L else 0L
        current = deriveChild(current, index, provider)
    }
    return current
}

private fun writeUInt32BE(dest: ByteArray, offset: Int, value: Long) {
    dest[offset] = (value ushr 24).toByte()
    dest[offset + 1] = (value ushr 16).toByte()
    dest[offset + 2] = (value ushr 8).toByte()
    dest[offset + 3] = value.toByte()
}
