package org.xrpl.sdk.crypto.codec

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XAddress
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * XRPL address and secret validation utilities.
 *
 * KMP 멀티플랫폼 지원을 위해 CryptoProvider 파라미터가 추가되었으나,
 * 기본값이 제공되므로 대부분의 경우 생략 가능.
 */

/**
 * Returns `true` if [address] is a valid XRPL classic address (starts with `'r'`).
 *
 * Validates Base58Check encoding, prefix byte, payload length, and checksum.
 */
public fun isValidClassicAddress(
    address: String,
    provider: CryptoProvider = platformCryptoProvider(),
): Boolean {
    if (address.isEmpty()) return false
    return runCatching {
        AddressCodec.decodeAddress(Address(address), provider)
    }.isSuccess
}

/**
 * Returns `true` if [xAddress] is a valid X-Address.
 *
 * Validates Base58Check encoding, prefix bytes, payload structure, and checksum.
 */
public fun isValidXAddress(
    xAddress: String,
    provider: CryptoProvider = platformCryptoProvider(),
): Boolean {
    if (xAddress.isEmpty()) return false
    return runCatching {
        XAddressCodec.decode(XAddress(xAddress), provider)
    }.isSuccess
}

/**
 * Returns `true` if [address] is a valid XRPL address — either classic or X-Address.
 */
public fun isValidAddress(
    address: String,
    provider: CryptoProvider = platformCryptoProvider(),
): Boolean = isValidClassicAddress(address, provider) || isValidXAddress(address, provider)

/**
 * Returns `true` if [secret] is a valid XRPL seed/secret string.
 *
 * Only validates seed format and checksum — does **not** derive a keypair.
 */
public fun isValidSecret(
    secret: String,
    provider: CryptoProvider = platformCryptoProvider(),
): Boolean {
    if (secret.isEmpty()) return false
    return runCatching {
        AddressCodec.decodeSeed(secret, provider)
    }.isSuccess
}
