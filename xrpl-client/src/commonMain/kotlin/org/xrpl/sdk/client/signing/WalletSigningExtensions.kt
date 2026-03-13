package org.xrpl.sdk.client.signing

import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.platformCryptoProvider
import org.xrpl.sdk.crypto.signing.SingleSignature

/**
 * Signs a filled transaction using this wallet's key pair.
 * Delegates to [Wallet.signWith] which handles private key safely.
 */
public fun Wallet.signTransaction(
    transaction: XrplTransaction.Filled,
    provider: CryptoProvider = platformCryptoProvider(),
): XrplTransaction.Signed = signWith(InMemorySigner(provider), transaction)

/**
 * Multi-signs a filled transaction using this wallet's key pair.
 */
public fun Wallet.multiSignTransaction(
    transaction: XrplTransaction.Filled,
    provider: CryptoProvider = platformCryptoProvider(),
): SingleSignature {
    val accountId = AddressCodec.accountIdFromPublicKey(publicKey, provider)
    return multiSignWith(InMemorySigner(provider), transaction, accountId)
}
