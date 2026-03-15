@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.transaction.LoanSetFields
import org.xrpl.sdk.core.model.transaction.Signer
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider
import org.xrpl.sdk.crypto.signing.SingleSignature

/**
 * Signs a LoanSet transaction as the counterparty.
 *
 * Mirrors the xrpl.js `signLoanSetByCounterparty` logic:
 * 1. Serializes the transaction for signing using BinaryCodec.
 * 2. Signs the result with this wallet's key pair.
 *
 * The counterparty signature is returned as a [SingleSignature] that can
 * later be placed in the `CounterpartySignature` field of the LoanSet tx.
 *
 * @param tx A filled LoanSet transaction.
 * @param provider The crypto provider for hashing and signing.
 * @return A [SingleSignature] with the counterparty signing result.
 * @throws IllegalArgumentException if [tx] does not have [LoanSetFields].
 */
@ExperimentalXrplApi
public fun Wallet.signLoanSetByCounterparty(
    tx: XrplTransaction.Filled,
    provider: CryptoProvider = platformCryptoProvider(),
): SingleSignature {
    require(tx.fields is LoanSetFields) {
        "Transaction must have LoanSetFields. Got: ${tx.fields::class.simpleName}"
    }

    val codecMap = FilledTransactionSerializer.toCodecMap(tx)
    val txJson = FilledTransactionSerializer.mapToJsonString(codecMap)
    val signingHex = BinaryCodec.encodeForSigning(txJson)
    val signingBytes = signingHex.hexToByteArray()

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
 * Combines multiple counterparty signatures for a LoanSet transaction
 * into a sorted list suitable for the `CounterpartySignature.Signers` field.
 *
 * Each [SingleSignature] represents one counterparty signer's contribution.
 * The XRPL protocol requires counterparty signers to be sorted by account.
 *
 * @param tx The original filled LoanSet transaction.
 * @param signatures The individual counterparty signatures to combine.
 * @return A list of signer maps in the `CounterpartySignature.Signers` wire format, sorted by account.
 * @throws IllegalArgumentException if [tx] does not have [LoanSetFields] or no signatures given.
 */
@ExperimentalXrplApi
public fun combineLoanSetCounterpartySigners(
    tx: XrplTransaction.Filled,
    signatures: List<SingleSignature>,
): List<Map<String, Any?>> {
    require(tx.fields is LoanSetFields) {
        "Transaction must have LoanSetFields. Got: ${tx.fields::class.simpleName}"
    }
    require(signatures.isNotEmpty()) { "At least one signature is required." }

    return signatures
        .sortedBy { it.signer.account.value }
        .map { sig ->
            mapOf(
                "Signer" to
                    mapOf(
                        "Account" to sig.signer.account.value,
                        "SigningPubKey" to sig.signer.signingPubKey,
                        "TxnSignature" to sig.signer.txnSignature,
                    ),
            )
        }
}
