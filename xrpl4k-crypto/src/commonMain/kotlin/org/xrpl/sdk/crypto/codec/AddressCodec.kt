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

    /** Node/validation public key prefix (0x1C / version 28). */
    private val NODE_PUBLIC_PREFIX: ByteArray = byteArrayOf(0x1C)

    private const val SEED_LENGTH = 16
    private const val ACCOUNT_ID_LENGTH = 20
    private const val NODE_PUBLIC_KEY_LENGTH = 33

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

    /**
     * Encode a 33-byte node/validation public key to Base58Check format.
     *
     * Uses prefix `0x1C` (version 28). Encoded strings start with `'n'`.
     *
     * Reference: xrpl.js `encodeNodePublic` in `xrp-codec.ts`
     */
    public fun encodeNodePublic(
        publicKey: ByteArray,
        provider: CryptoProvider = platformCryptoProvider(),
    ): String {
        require(publicKey.size == NODE_PUBLIC_KEY_LENGTH) {
            "Node public key must be $NODE_PUBLIC_KEY_LENGTH bytes (compressed). Got ${publicKey.size}."
        }
        return Base58Check.encode(NODE_PUBLIC_PREFIX, publicKey, provider)
    }

    /**
     * Decode a Base58Check-encoded node public key string to 33 raw bytes.
     *
     * Reference: xrpl.js `decodeNodePublic` in `xrp-codec.ts`
     */
    public fun decodeNodePublic(
        encoded: String,
        provider: CryptoProvider = platformCryptoProvider(),
    ): ByteArray {
        val (_, payload) =
            Base58Check.decodeChecked(
                encoded,
                listOf(NODE_PUBLIC_PREFIX),
                expectedPayloadLength = NODE_PUBLIC_KEY_LENGTH,
                provider,
            )
        return payload
    }

    /**
     * Derive a classic XRPL address from a node public key.
     *
     * The algorithm mirrors xrpl.js `deriveNodeAddress`:
     * 1. Decode the Base58Check node public key to raw bytes (the "public generator").
     * 2. Derive an account public key from the public generator using secp256k1 point addition.
     * 3. Hash with SHA-256 then RIPEMD-160 to produce the Account ID.
     * 4. Encode as a classic r-address.
     *
     * Reference: xrpl.js `ripple-keypairs/src/index.ts` — `deriveNodeAddress`
     */
    public fun deriveNodeAddress(
        nodePublic: String,
        provider: CryptoProvider = platformCryptoProvider(),
    ): Address {
        val generatorBytes = decodeNodePublic(nodePublic, provider)
        val accountPublicBytes = accountPublicFromPublicGenerator(generatorBytes, provider)
        val sha256Hash = provider.sha256(accountPublicBytes)
        val ripemdHash = provider.ripemd160(sha256Hash)
        val accountId = AccountId.fromBytes(ripemdHash)
        return encodeAddress(accountId, provider)
    }

    /**
     * Derives an account public key from a secp256k1 public generator.
     *
     * This mirrors `accountPublicFromPublicGenerator` in xrpl.js:
     * 1. Compute scalar = deriveScalar(publicGenBytes, discriminator=0)
     * 2. Compute offset point = G * scalar
     * 3. Result = publicGenPoint + offsetPoint (compressed)
     */
    internal fun accountPublicFromPublicGenerator(
        publicGenBytes: ByteArray,
        provider: CryptoProvider,
    ): ByteArray {
        val scalar = deriveScalar(publicGenBytes, discriminator = 0, provider)
        val offsetPublicKey = provider.secp256k1PublicKey(scalar)
        return provider.secp256k1AddPublicKeys(publicGenBytes, offsetPublicKey)
    }

    /**
     * Derives a 32-byte scalar from input bytes with a discriminator, matching
     * the secp256k1 derivation in xrpl.js.
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

    private fun deriveScalar(
        bytes: ByteArray,
        discriminator: Int,
        provider: CryptoProvider,
    ): ByteArray {
        for (i in 0..Int.MAX_VALUE) {
            val extraSize = 4 + 4 // discriminator + counter
            val input = ByteArray(bytes.size + extraSize)
            bytes.copyInto(input)
            var offset = bytes.size
            input[offset++] = (discriminator ushr 24).toByte()
            input[offset++] = (discriminator ushr 16).toByte()
            input[offset++] = (discriminator ushr 8).toByte()
            input[offset++] = discriminator.toByte()
            input[offset++] = (i ushr 24).toByte()
            input[offset++] = (i ushr 16).toByte()
            input[offset++] = (i ushr 8).toByte()
            input[offset] = i.toByte()

            val hash = provider.sha512Half(input)
            if (!hash.all { it == 0.toByte() } && isLessThanOrder(hash)) {
                return hash
            }
        }
        error("Could not derive valid scalar after 2^31 attempts")
    }

    private fun isLessThanOrder(a: ByteArray): Boolean {
        for (i in a.indices) {
            val ai = a[i].toInt() and 0xFF
            val bi = SECP256K1_ORDER[i].toInt() and 0xFF
            if (ai < bi) return true
            if (ai > bi) return false
        }
        return false
    }
}
