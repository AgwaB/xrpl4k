@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.model.transaction.Signer
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.AccountId
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.platformCryptoProvider
import org.xrpl.sdk.crypto.signing.PrivateKey
import org.xrpl.sdk.crypto.signing.SingleSignature
import org.xrpl.sdk.crypto.signing.TransactionSigner

/**
 * In-memory transaction signer using raw private keys.
 * Lives in xrpl-client because it needs BinaryCodec.
 */
public class InMemorySigner(
    private val provider: CryptoProvider = platformCryptoProvider(),
) : TransactionSigner<PrivateKey> {
    override fun sign(
        key: PrivateKey,
        transaction: XrplTransaction.Filled,
    ): XrplTransaction.Signed {
        // 1. Serialize to codec map
        val codecMap = FilledTransactionSerializer.toCodecMap(transaction).toMutableMap()

        // 2. Set SigningPubKey from the key's public key
        val publicKeyHex = derivePublicKeyHex(key)
        codecMap["SigningPubKey"] = publicKeyHex

        // 3. Get signing bytes via BinaryCodec
        val jsonForSigning = FilledTransactionSerializer.mapToJsonString(codecMap)
        val signingHex = BinaryCodec.encodeForSigning(jsonForSigning)
        val signingBytes = signingHex.hexToByteArray()

        // 4. Sign
        val signatureBytes = signMessage(key, signingBytes)
        val signatureHex = signatureBytes.toHexString().uppercase()

        // 5. Add signature to transaction and encode full blob
        codecMap["TxnSignature"] = signatureHex
        val fullJson = FilledTransactionSerializer.mapToJsonString(codecMap)
        val txBlob = BinaryCodec.encode(fullJson)

        // 6. Calculate transaction hash: SHA-512Half(0x54584E00 + txBlob bytes)
        val hashPrefix = byteArrayOf(0x54, 0x58, 0x4E, 0x00)
        val txBlobBytes = txBlob.hexToByteArray()
        val hashInput = hashPrefix + txBlobBytes
        val hash = provider.sha512Half(hashInput).toHexString().uppercase()

        return XrplTransaction.Signed.create(
            transactionType = transaction.transactionType,
            txBlob = txBlob.uppercase(),
            hash = TxHash(hash),
        )
    }

    override fun multiSign(
        key: PrivateKey,
        transaction: XrplTransaction.Filled,
        signerAccountId: AccountId,
    ): SingleSignature {
        // 1. Serialize to codec map
        val codecMap = FilledTransactionSerializer.toCodecMap(transaction).toMutableMap()

        // 2. Set SigningPubKey = "" for multi-signing
        codecMap["SigningPubKey"] = ""

        // 3. Get multi-signing bytes
        val jsonForSigning = FilledTransactionSerializer.mapToJsonString(codecMap)
        val signingHex = BinaryCodec.encodeForMultiSigning(jsonForSigning, signerAccountId.hex)
        val signingBytes = signingHex.hexToByteArray()

        // 4. Sign
        val signatureBytes = signMessage(key, signingBytes)
        val signatureHex = signatureBytes.toHexString().uppercase()

        // 5. Derive public key
        val publicKeyHex = derivePublicKeyHex(key)

        // 6. Create signer entry using the classic address from the account ID
        val signerAddress = AddressCodec.encodeAddress(signerAccountId, provider)

        return SingleSignature(
            Signer(
                account = signerAddress,
                txnSignature = signatureHex,
                signingPubKey = publicKeyHex,
            ),
        )
    }

    private fun derivePublicKeyHex(key: PrivateKey): String =
        key.useBytes { bytes ->
            when (key.algorithm) {
                KeyAlgorithm.Ed25519 -> {
                    val pubKeyRaw = provider.ed25519PublicKey(bytes)
                    val prefixed = ByteArray(33)
                    prefixed[0] = 0xED.toByte()
                    pubKeyRaw.copyInto(prefixed, destinationOffset = 1)
                    prefixed.toHexString().uppercase()
                }
                KeyAlgorithm.Secp256k1 -> {
                    provider.secp256k1PublicKey(bytes).toHexString().uppercase()
                }
            }
        }

    private fun signMessage(
        key: PrivateKey,
        signingBytes: ByteArray,
    ): ByteArray =
        key.useBytes { bytes ->
            when (key.algorithm) {
                KeyAlgorithm.Ed25519 -> provider.ed25519Sign(signingBytes, bytes)
                KeyAlgorithm.Secp256k1 -> provider.secp256k1Sign(provider.sha512Half(signingBytes), bytes)
            }
        }
}
