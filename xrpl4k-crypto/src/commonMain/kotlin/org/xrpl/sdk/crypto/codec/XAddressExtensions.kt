package org.xrpl.sdk.crypto.codec

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XAddress
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Decodes this X-Address to its classic address component.
 *
 * @param provider CryptoProvider for checksum operations.
 * @return The classic r-address.
 */
public fun XAddress.classicAddress(provider: CryptoProvider = platformCryptoProvider()): Address =
    XAddressCodec.decode(this, provider).classicAddress

/**
 * Returns the destination tag encoded in this X-Address, or `null`.
 *
 * @param provider CryptoProvider for checksum operations.
 */
public fun XAddress.destinationTag(provider: CryptoProvider = platformCryptoProvider()): UInt? =
    XAddressCodec.decode(this, provider).tag

/**
 * Returns `true` if this is a testnet X-Address.
 *
 * @param provider CryptoProvider for checksum operations.
 */
public fun XAddress.isTest(provider: CryptoProvider = platformCryptoProvider()): Boolean =
    XAddressCodec.decode(this, provider).isTest
