@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XAddress
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * X-Address encoding and decoding.
 *
 * X-Addresses combine a classic address and an optional destination tag
 * into a single string. Mainnet addresses start with `'X'`, testnet with `'T'`.
 *
 * Format: `[prefix 2 bytes] [account ID 20 bytes] [flags 1 byte] [tag 8 bytes LE]`
 * Total payload: 31 bytes (before Base58Check encoding).
 *
 * Reference: [x-address-codec](https://github.com/ripple/ripple-address-codec)
 */
public object XAddressCodec {
    /** Mainnet X-Address prefix: `[0x05, 0x44]`. */
    private val MAINNET_PREFIX = byteArrayOf(0x05, 0x44)

    /** Testnet X-Address prefix: `[0x04, 0x93]`. */
    private val TESTNET_PREFIX = byteArrayOf(0x04, 0x93.toByte())

    private const val ACCOUNT_ID_LENGTH = 20
    private const val PAYLOAD_LENGTH = 31 // 20 (account ID) + 1 (flags) + 8 (tag) + 2 (prefix) = 31 total decoded

    /**
     * Encode a classic address + optional tag into an X-Address.
     *
     * @param address The classic r-address.
     * @param tag Optional destination tag. `null` means no tag.
     * @param isTest `true` for testnet, `false` for mainnet.
     * @param provider CryptoProvider for SHA-256 checksums.
     * @return The X-Address.
     */
    public fun encode(
        address: Address,
        tag: UInt? = null,
        isTest: Boolean = false,
        provider: CryptoProvider = platformCryptoProvider(),
    ): XAddress {
        val accountId = AddressCodec.decodeAddress(address, provider)
        val accountIdBytes = accountId.toByteArray()

        val prefix = if (isTest) TESTNET_PREFIX else MAINNET_PREFIX

        // flags: 0x00 = no tag, 0x01 = has tag
        val flags = if (tag != null) 0x01.toByte() else 0x00.toByte()

        // tag as 8-byte little-endian (UInt32 in lower 4 bytes, upper 4 bytes zero)
        val tagBytes = ByteArray(8)
        if (tag != null) {
            val t = tag.toLong()
            tagBytes[0] = (t and 0xFF).toByte()
            tagBytes[1] = ((t shr 8) and 0xFF).toByte()
            tagBytes[2] = ((t shr 16) and 0xFF).toByte()
            tagBytes[3] = ((t shr 24) and 0xFF).toByte()
            // bytes 4-7 remain 0 (reserved for 64-bit tags)
        }

        val payload = accountIdBytes + byteArrayOf(flags) + tagBytes
        val encoded = Base58Check.encode(prefix, payload, provider)
        return XAddress(encoded)
    }

    /**
     * Decode an X-Address into its components.
     *
     * @param xAddress The X-Address to decode.
     * @param provider CryptoProvider for SHA-256 checksum verification.
     * @return The decoded components (classic address, tag, isTest).
     * @throws IllegalArgumentException if the X-Address is invalid.
     */
    public fun decode(
        xAddress: XAddress,
        provider: CryptoProvider = platformCryptoProvider(),
    ): XAddressComponents {
        val expectedPayloadLength = ACCOUNT_ID_LENGTH + 1 + 8 // accountId + flags + tag

        val (prefix, payload) =
            Base58Check.decodeChecked(
                xAddress.value,
                listOf(MAINNET_PREFIX, TESTNET_PREFIX),
                expectedPayloadLength,
                provider,
            )

        val isTest = prefix.contentEquals(TESTNET_PREFIX)

        val accountIdBytes = payload.copyOfRange(0, ACCOUNT_ID_LENGTH)
        val flags = payload[ACCOUNT_ID_LENGTH].toInt() and 0xFF

        val tag =
            if (flags == 0x01) {
                val t0 = payload[ACCOUNT_ID_LENGTH + 1].toLong() and 0xFF
                val t1 = (payload[ACCOUNT_ID_LENGTH + 2].toLong() and 0xFF) shl 8
                val t2 = (payload[ACCOUNT_ID_LENGTH + 3].toLong() and 0xFF) shl 16
                val t3 = (payload[ACCOUNT_ID_LENGTH + 4].toLong() and 0xFF) shl 24
                (t0 or t1 or t2 or t3).toUInt()
            } else {
                null
            }

        val classicAddress =
            AddressCodec.encodeAddress(
                org.xrpl.sdk.core.type.AccountId.fromBytes(accountIdBytes),
                provider,
            )

        return XAddressComponents(classicAddress, tag, isTest)
    }
}
