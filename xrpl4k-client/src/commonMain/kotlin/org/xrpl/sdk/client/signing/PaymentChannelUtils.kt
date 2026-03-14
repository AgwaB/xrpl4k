@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Signs a payment channel claim.
 * Uses BinaryCodec.encodeForSigningClaim() to produce signing data.
 *
 * @param wallet The wallet to sign with.
 * @param channelId The 256-bit channel ID (64-char hex).
 * @param amount The amount in drops (decimal string).
 * @return The signature hex string (uppercase).
 */
public fun signPaymentChannelClaim(
    wallet: Wallet,
    channelId: String,
    amount: String,
    provider: CryptoProvider = platformCryptoProvider(),
): String {
    val claimJson = """{"Channel":"$channelId","Amount":"$amount"}"""
    val signingData = BinaryCodec.encodeForSigningClaim(claimJson)
    val signingBytes = signingData.hexToByteArray()

    val signatureBytes =
        when (wallet.algorithm) {
            KeyAlgorithm.Ed25519 -> wallet.sign(signingBytes)
            KeyAlgorithm.Secp256k1 -> wallet.sign(provider.sha512Half(signingBytes))
        }
    return signatureBytes.toHexString().uppercase()
}
