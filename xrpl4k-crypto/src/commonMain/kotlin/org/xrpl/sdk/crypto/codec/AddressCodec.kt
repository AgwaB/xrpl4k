@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import org.xrpl.sdk.core.type.AccountId
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.PublicKey
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * XRPL address and seed codec.
 *
 * Supports dual seed prefixes per XRPL specification:
 * - secp256k1 family seeds: prefix `0x21` (1 byte), leading char `'s'`
 * - Ed25519 seeds: prefix `[0x01, 0xE1, 0x4B]` (3 bytes), leading chars `"sEd"`
 *
 * Reference: `xrpl.js/packages/ripple-address-codec/src/xrp-codec.ts`
 */
public object AddressCodec {
    /** Classic address prefix byte (0x00). */
    private val ADDRESS_PREFIX: ByteArray = byteArrayOf(0x00)

    /** secp256k1 family seed prefix (1 byte). */
    private val FAMILY_SEED_PREFIX: ByteArray = byteArrayOf(0x21)

    /** Ed25519 seed prefix (3 bytes). */
    private val ED25519_SEED_PREFIX: ByteArray = byteArrayOf(0x01, 0xE1.toByte(), 0x4B)

    /** Account public key prefix (0x23). */
    private val ACCOUNT_PUBLIC_KEY_PREFIX: ByteArray = byteArrayOf(0x23)

    private const val SEED_LENGTH = 16
    private const val ACCOUNT_ID_LENGTH = 20

    /**
     * Encode a 20-byte Account ID to a classic r-address.
     */
    public fun encodeAddress(
        accountId: AccountId,
        provider: CryptoProvider = platformCryptoProvider(),
    ): Address {
        val encoded = Base58Check.encode(ADDRESS_PREFIX, accountId.toByteArray(), provider)
        return Address(encoded)
    }

    /**
     * Decode a classic r-address to a 20-byte Account ID.
     */
    public fun decodeAddress(
        address: Address,
        provider: CryptoProvider = platformCryptoProvider(),
    ): AccountId {
        val (_, payload) =
            Base58Check.decodeChecked(
                address.value,
                listOf(ADDRESS_PREFIX),
                ACCOUNT_ID_LENGTH,
                provider,
            )
        return AccountId.fromBytes(payload)
    }

    /**
     * Encode a 16-byte seed to a Base58 seed string.
     *
     * - Ed25519 seeds are encoded with prefix `[0x01, 0xE1, 0x4B]` → starts with `"sEd"`
     * - secp256k1 seeds are encoded with prefix `0x21` → starts with `"s"` (not `"sEd"`)
     */
    public fun encodeSeed(
        seed: ByteArray,
        algorithm: KeyAlgorithm,
        provider: CryptoProvider = platformCryptoProvider(),
    ): String {
        require(seed.size == SEED_LENGTH) {
            "Seed must be exactly $SEED_LENGTH bytes. Got ${seed.size}."
        }
        val prefix =
            when (algorithm) {
                KeyAlgorithm.Ed25519 -> ED25519_SEED_PREFIX
                KeyAlgorithm.Secp256k1 -> FAMILY_SEED_PREFIX
            }
        return Base58Check.encode(prefix, seed, provider)
    }

    /**
     * Decode a Base58 seed string to (seed bytes, algorithm).
     *
     * Determines algorithm by matching the decoded prefix:
     * - If prefix matches `[0x01, 0xE1, 0x4B]` → Ed25519
     * - If prefix matches `[0x21]` → secp256k1
     *
     * @param encoded Base58-encoded seed string.
     * @return Pair of (16-byte seed, KeyAlgorithm).
     * @throws IllegalArgumentException if prefix doesn't match any known seed type.
     */
    public fun decodeSeed(
        encoded: String,
        provider: CryptoProvider = platformCryptoProvider(),
    ): Pair<ByteArray, KeyAlgorithm> {
        // Try Ed25519 first (3-byte prefix), then secp256k1 (1-byte prefix).
        // Order matters: Ed25519 prefix is longer so must be tried first.
        val (prefix, payload) =
            Base58Check.decodeChecked(
                encoded,
                listOf(ED25519_SEED_PREFIX, FAMILY_SEED_PREFIX),
                SEED_LENGTH,
                provider,
            )
        val algorithm =
            if (prefix.contentEquals(ED25519_SEED_PREFIX)) {
                KeyAlgorithm.Ed25519
            } else {
                KeyAlgorithm.Secp256k1
            }
        return payload to algorithm
    }

    /**
     * Derive Account ID from a public key: SHA-256 → RIPEMD-160 → 20 bytes.
     * Returns AccountId with lowercase hex string representation.
     */
    public fun accountIdFromPublicKey(
        publicKey: PublicKey,
        provider: CryptoProvider = platformCryptoProvider(),
    ): AccountId {
        val pubKeyBytes = publicKey.value.hexToByteArray()
        val sha256Hash = provider.sha256(pubKeyBytes)
        val ripemdHash = provider.ripemd160(sha256Hash)
        return AccountId.fromBytes(ripemdHash)
    }

    /**
     * Encode a 33-byte account public key to Base58.
     */
    public fun encodeAccountPublicKey(
        publicKey: ByteArray,
        provider: CryptoProvider = platformCryptoProvider(),
    ): String {
        require(publicKey.size == 33) {
            "Account public key must be 33 bytes (compressed). Got ${publicKey.size}."
        }
        return Base58Check.encode(ACCOUNT_PUBLIC_KEY_PREFIX, publicKey, provider)
    }

    /**
     * Decode a Base58 account public key to 33 bytes.
     */
    public fun decodeAccountPublicKey(
        encoded: String,
        provider: CryptoProvider = platformCryptoProvider(),
    ): ByteArray {
        val (_, payload) =
            Base58Check.decodeChecked(
                encoded,
                listOf(ACCOUNT_PUBLIC_KEY_PREFIX),
                expectedPayloadLength = 33,
                provider,
            )
        return payload
    }
}
