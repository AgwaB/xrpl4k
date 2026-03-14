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

/**
 * Verifies a payment channel claim signature.
 *
 * @param channelId The 256-bit channel ID (64-char hex).
 * @param amount The amount in drops (decimal string).
 * @param signature The signature hex string to verify.
 * @param publicKey The signer's public key hex string (33 bytes, with algorithm prefix).
 * @return `true` if the signature is valid for the given channel claim.
 */
public fun verifyPaymentChannelClaim(
    channelId: String,
    amount: String,
    signature: String,
    publicKey: String,
    provider: CryptoProvider = platformCryptoProvider(),
): Boolean {
    val claimJson = """{"Channel":"$channelId","Amount":"$amount"}"""
    val signingData = BinaryCodec.encodeForSigningClaim(claimJson)
    val signingBytes = signingData.hexToByteArray()
    val signatureBytes = signature.hexToByteArray()
    val publicKeyBytes = publicKey.hexToByteArray()

    return if (publicKey.uppercase().startsWith("ED")) {
        // Ed25519: public key is 33 bytes with 0xED prefix; strip the prefix for verification
        val rawPublicKey = publicKeyBytes.copyOfRange(1, publicKeyBytes.size)
        provider.ed25519Verify(signingBytes, signatureBytes, rawPublicKey)
    } else {
        // secp256k1: verify against SHA-512Half of the signing bytes
        provider.secp256k1Verify(provider.sha512Half(signingBytes), signatureBytes, publicKeyBytes)
    }
}
