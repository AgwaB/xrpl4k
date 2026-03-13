@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.platformCryptoProvider
import org.xrpl.sdk.crypto.signing.SingleSignature

/**
 * Combines multiple single signatures into a multi-signed transaction.
 *
 * Protocol requirements enforced:
 * - SigningPubKey = "" on the outer transaction
 * - Signers sorted by Account ID hex ascending
 */
public fun combineSignatures(
    transaction: XrplTransaction.Filled,
    signatures: List<SingleSignature>,
    provider: CryptoProvider = platformCryptoProvider(),
): XrplTransaction.Signed {
    require(signatures.isNotEmpty()) { "At least one signature is required." }

    val codecMap = FilledTransactionSerializer.toCodecMap(transaction).toMutableMap()

    // Set SigningPubKey = "" for multi-signed transactions
    codecMap["SigningPubKey"] = ""
    // No TxnSignature on the outer transaction
    codecMap.remove("TxnSignature")

    // Sort signers by Account ID hex ascending
    val sortedSigners =
        signatures
            .map { it.signer }
            .sortedBy { AddressCodec.decodeAddress(it.account, provider).hex }

    // Add Signers array
    codecMap["Signers"] =
        sortedSigners.map { signer ->
            mapOf(
                "Signer" to
                    mapOf(
                        "Account" to AddressCodec.decodeAddress(signer.account, provider).hex,
                        "TxnSignature" to signer.txnSignature,
                        "SigningPubKey" to signer.signingPubKey,
                    ),
            )
        }

    val fullJson = FilledTransactionSerializer.mapToJsonString(codecMap)
    val txBlob = BinaryCodec.encode(fullJson)

    // Hash: SHA-512Half(0x54584E00 + txBlob bytes)
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
