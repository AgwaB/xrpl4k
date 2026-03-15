@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.model.transaction.BatchFields
import org.xrpl.sdk.core.model.transaction.Signer
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.hashing.sha512HalfWithPrefix
import org.xrpl.sdk.crypto.platformCryptoProvider
import org.xrpl.sdk.crypto.signing.SingleSignature

/**
 * Signs a specific inner transaction within a Batch transaction.
 *
 * Mirrors the xrpl.js `signMultiBatch` logic:
 * 1. Computes the transaction hash of each inner RawTransaction.
 * 2. Encodes `{flags, txIDs}` via [BinaryCodec.encodeForSigningBatch].
 * 3. Signs the resulting bytes with this wallet's key pair.
 *
 * @param batchTx A filled Batch transaction containing [BatchFields].
 * @param provider The crypto provider for hashing and signing.
 * @return A [SingleSignature] with the batch signing result.
 * @throws IllegalArgumentException if [batchTx] does not have [BatchFields].
 */
public fun Wallet.signBatchTransaction(
    batchTx: XrplTransaction.Filled,
    provider: CryptoProvider = platformCryptoProvider(),
): SingleSignature {
    val batchFields =
        batchTx.fields as? BatchFields
            ?: throw IllegalArgumentException(
                "Transaction must have BatchFields. Got: ${batchTx.fields::class.simpleName}",
            )

    val flags = batchFields.flags?.toLong() ?: 0L

    // Compute transaction IDs from each RawTransaction map.
    // Each RawTransaction must be a fully-signed inner transaction map.
    // The txID = SHA-512Half(TRANSACTION_ID_PREFIX + encode(rawTx))
    val txIdPrefix = byteArrayOf(0x54, 0x58, 0x4E, 0x00)
    val txIDs =
        batchFields.rawTransactions.map { rawTx ->
            val innerJson = FilledTransactionSerializer.mapToJsonString(rawTx)
            val innerBlob = BinaryCodec.encode(innerJson)
            val innerBytes = innerBlob.hexToByteArray()
            sha512HalfWithPrefix(txIdPrefix, innerBytes, provider).toHexString().uppercase()
        }

    // Encode for batch signing
    val batchSignJson = buildBatchSignJson(flags, txIDs)
    val signingHex = BinaryCodec.encodeForSigningBatch(batchSignJson)
    val signingBytes = signingHex.hexToByteArray()

    // Sign
    val signatureBytes =
        when (algorithm) {
            KeyAlgorithm.Ed25519 -> sign(signingBytes)
            KeyAlgorithm.Secp256k1 -> sign(provider.sha512Half(signingBytes))
        }
    val signatureHex = signatureBytes.toHexString().uppercase()

    return SingleSignature(
        Signer(
            account = address,
            txnSignature = signatureHex,
            signingPubKey = publicKey.value,
        ),
    )
}

/**
 * Combines multiple batch signatures into a sorted list suitable for
 * the `BatchSigners` field of a Batch transaction.
 *
 * Each [SingleSignature] represents one signer's contribution. The
 * XRPL protocol requires batch signers to be sorted by account.
 *
 * @param signatures The individual batch signatures to combine.
 * @return A list of signer maps in the `BatchSigners` wire format, sorted by account.
 */
public fun combineBatchSigners(signatures: List<SingleSignature>): List<Map<String, Any?>> {
    require(signatures.isNotEmpty()) { "At least one signature is required." }

    return signatures
        .sortedBy { it.signer.account.value }
        .map { sig ->
            mapOf(
                "BatchSigner" to
                    mapOf(
                        "Account" to sig.signer.account.value,
                        "SigningPubKey" to sig.signer.signingPubKey,
                        "TxnSignature" to sig.signer.txnSignature,
                    ),
            )
        }
}

/**
 * Builds the JSON input for [BinaryCodec.encodeForSigningBatch].
 */
private fun buildBatchSignJson(
    flags: Long,
    txIDs: List<String>,
): String {
    val txIDsArray = txIDs.joinToString(",") { "\"$it\"" }
    return """{"flags":$flags,"txIDs":[$txIDsArray]}"""
}
