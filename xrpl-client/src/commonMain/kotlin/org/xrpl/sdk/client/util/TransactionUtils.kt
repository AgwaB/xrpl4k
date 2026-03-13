@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider

/**
 * A balance change for a single account resulting from a transaction.
 *
 * @property currency The currency code ("XRP" or a 3-letter/160-bit hex IOU code).
 * @property value The signed change amount as a decimal string (negative = debit, positive = credit).
 * @property counterparty The issuer address for IOU balances; `null` for XRP.
 */
public class BalanceChange(
    public val currency: String,
    public val value: String,
    public val counterparty: Address? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is BalanceChange &&
            currency == other.currency &&
            value == other.value &&
            counterparty == other.counterparty

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (counterparty?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "BalanceChange(currency=$currency, value=$value, counterparty=$counterparty)"
}

/**
 * Computes the transaction hash from a signed transaction blob.
 *
 * The hash is calculated as SHA-512Half of the concatenation of the
 * `TRANSACTION_ID` hash prefix (`0x54584E00`) and the decoded blob bytes.
 * SHA-512Half returns the first 32 bytes of the SHA-512 digest.
 *
 * @param txBlob Hex-encoded signed transaction blob.
 * @param provider Cryptographic provider used for SHA-512Half.
 * @return The 256-bit transaction hash as a [TxHash].
 */
public fun hashSignedTx(
    txBlob: String,
    provider: CryptoProvider,
): TxHash {
    val hashPrefix = byteArrayOf(0x54, 0x58, 0x4E, 0x00)
    val txBlobBytes = txBlob.hexToByteArray()
    val hashInput = hashPrefix + txBlobBytes
    val hash = provider.sha512Half(hashInput).toHexString().uppercase()
    return TxHash(hash)
}

/**
 * Parses transaction metadata to extract balance changes per account.
 *
 * Iterates over the `AffectedNodes` array in [metadata] and extracts:
 * - **AccountRoot** nodes: XRP balance change derived from the difference
 *   between `PreviousFields.Balance` and `FinalFields.Balance`.
 * - **RippleState** nodes: IOU balance change derived from the difference
 *   between `PreviousFields.Balance` and `FinalFields.Balance`, attributed
 *   to both the low-limit and high-limit accounts.
 *
 * Only nodes that contain a `PreviousFields.Balance` entry produce entries
 * in the result; created or deleted nodes without a previous balance are skipped.
 *
 * @param metadata The transaction metadata JSON object (e.g. from a `tx` RPC response).
 * @return A map from [Address] to the list of [BalanceChange] entries for that account.
 */
public fun parseBalanceChanges(metadata: JsonObject): Map<Address, List<BalanceChange>> {
    val affectedNodes = metadata["AffectedNodes"] as? JsonArray ?: return emptyMap()
    val result = mutableMapOf<Address, MutableList<BalanceChange>>()

    fun addChange(
        account: Address,
        change: BalanceChange,
    ) {
        result.getOrPut(account) { mutableListOf() }.add(change)
    }

    for (nodeElement in affectedNodes) {
        val nodeWrapper = nodeElement as? JsonObject ?: continue

        // Each element is a wrapper like {"ModifiedNode": {...}} or {"CreatedNode": {...}}
        val nodeContent =
            (
                nodeWrapper["ModifiedNode"]
                    ?: nodeWrapper["CreatedNode"]
                    ?: nodeWrapper["DeletedNode"]
            ) as? JsonObject ?: continue

        val ledgerEntryType =
            nodeContent["LedgerEntryType"]
                ?.jsonPrimitive
                ?.content ?: continue

        when (ledgerEntryType) {
            "AccountRoot" -> {
                val finalFields = nodeContent["FinalFields"]?.jsonObject ?: continue
                val previousFields = nodeContent["PreviousFields"]?.jsonObject ?: continue

                val accountStr = finalFields["Account"]?.jsonPrimitive?.content ?: continue
                val previousBalance = previousFields["Balance"]?.jsonPrimitive?.content ?: continue
                val finalBalance = finalFields["Balance"]?.jsonPrimitive?.content ?: continue

                val prev = previousBalance.toLongOrNull() ?: continue
                val final = finalBalance.toLongOrNull() ?: continue
                val delta = final - prev

                val address = runCatching { Address(accountStr) }.getOrNull() ?: continue
                addChange(address, BalanceChange(currency = "XRP", value = delta.toString()))
            }

            "RippleState" -> {
                val finalFields = nodeContent["FinalFields"]?.jsonObject ?: continue
                val previousFields = nodeContent["PreviousFields"]?.jsonObject ?: continue

                val previousBalanceStr = previousFields["Balance"]?.jsonPrimitive?.content ?: continue
                val finalBalanceStr = finalFields["Balance"]?.jsonPrimitive?.content ?: continue

                val prevValue = previousBalanceStr.toDoubleOrNull() ?: continue
                val finalValue = finalBalanceStr.toDoubleOrNull() ?: continue
                val delta = finalValue - prevValue

                // Extract currency from LowLimit (issuer is in the limit objects)
                val lowLimit = finalFields["LowLimit"]?.jsonObject ?: continue
                val highLimit = finalFields["HighLimit"]?.jsonObject ?: continue

                val lowAccountStr = lowLimit["issuer"]?.jsonPrimitive?.content ?: continue
                val highAccountStr = highLimit["issuer"]?.jsonPrimitive?.content ?: continue
                val currency = lowLimit["currency"]?.jsonPrimitive?.content ?: continue

                val lowAddress = runCatching { Address(lowAccountStr) }.getOrNull() ?: continue
                val highAddress = runCatching { Address(highAccountStr) }.getOrNull() ?: continue

                // Positive delta in RippleState means lowAccount gained
                addChange(
                    lowAddress,
                    BalanceChange(
                        currency = currency,
                        value = delta.toString(),
                        counterparty = highAddress,
                    ),
                )
                addChange(
                    highAddress,
                    BalanceChange(
                        currency = currency,
                        value = (-delta).toString(),
                        counterparty = lowAddress,
                    ),
                )
            }
        }
    }

    return result
}

/**
 * Extracts the NFTokenID from the metadata of an NFTokenMint transaction.
 *
 * Searches the `AffectedNodes` array for a modified or created `NFTokenPage`
 * node and finds the NFToken entry present in `FinalFields.NFTokens` but absent
 * from `PreviousFields.NFTokens` (i.e. the newly minted token).
 *
 * @param metadata The transaction metadata JSON object.
 * @return The NFTokenID hex string, or `null` if it cannot be found.
 */
public fun getNFTokenID(metadata: JsonObject): String? {
    val affectedNodes = metadata["AffectedNodes"] as? JsonArray ?: return null

    for (nodeElement in affectedNodes) {
        val nodeWrapper = nodeElement as? JsonObject ?: continue

        val nodeContent =
            (nodeWrapper["ModifiedNode"] ?: nodeWrapper["CreatedNode"]) as? JsonObject ?: continue

        val ledgerEntryType =
            nodeContent["LedgerEntryType"]?.jsonPrimitive?.content ?: continue
        if (ledgerEntryType != "NFTokenPage") continue

        val finalFields = nodeContent["FinalFields"]?.jsonObject ?: continue
        val finalTokens =
            finalFields["NFTokens"]
                ?.jsonArray
                ?.mapNotNull { it.jsonObject["NFToken"]?.jsonObject?.get("NFTokenID")?.jsonPrimitive?.content }
                ?.toSet() ?: continue

        val previousFields = nodeContent["PreviousFields"]?.jsonObject
        val previousTokens =
            previousFields
                ?.get("NFTokens")
                ?.jsonArray
                ?.mapNotNull { it.jsonObject["NFToken"]?.jsonObject?.get("NFTokenID")?.jsonPrimitive?.content }
                ?.toSet() ?: emptySet()

        val newTokenIds = finalTokens - previousTokens
        if (newTokenIds.isNotEmpty()) {
            return newTokenIds.first()
        }
    }

    // Also handle CreatedNode with no PreviousFields (new page with single token)
    for (nodeElement in affectedNodes) {
        val nodeWrapper = nodeElement as? JsonObject ?: continue
        val nodeContent = nodeWrapper["CreatedNode"] as? JsonObject ?: continue

        val ledgerEntryType =
            nodeContent["LedgerEntryType"]?.jsonPrimitive?.content ?: continue
        if (ledgerEntryType != "NFTokenPage") continue

        val newFields = nodeContent["NewFields"]?.jsonObject ?: continue
        return newFields["NFTokens"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("NFToken")
            ?.jsonObject
            ?.get("NFTokenID")
            ?.jsonPrimitive
            ?.content
    }

    return null
}

/**
 * Verifies the cryptographic signature of a signed transaction blob.
 *
 * The approach:
 * 1. Decode [txBlob] with [BinaryCodec.decode] to obtain the transaction JSON.
 * 2. Extract `SigningPubKey` and `TxnSignature` from the decoded JSON.
 * 3. Re-encode the transaction for signing via [BinaryCodec.encodeForSigning]
 *    (which strips the signature fields and prepends the signing prefix).
 * 4. Determine the key algorithm from the `SigningPubKey` prefix:
 *    - `ED` (prefix `0xED`) → Ed25519: verify the raw signing bytes directly.
 *    - Otherwise → secp256k1: verify the SHA-512Half of the signing bytes.
 * 5. Delegate to [CryptoProvider.ed25519Verify] or [CryptoProvider.secp256k1Verify].
 *
 * @param txBlob Hex-encoded signed transaction blob.
 * @param provider Cryptographic provider for signature verification.
 * @return `true` if the signature is valid, `false` otherwise.
 * @throws IllegalArgumentException if the blob cannot be decoded or required fields are missing.
 */
public fun verifyTransaction(
    txBlob: String,
    provider: CryptoProvider,
): Boolean {
    val decodedJson = BinaryCodec.decode(txBlob)

    // Parse fields from the decoded JSON string manually
    val signingPubKeyHex = extractJsonField(decodedJson, "SigningPubKey") ?: return false
    val txnSignatureHex = extractJsonField(decodedJson, "TxnSignature") ?: return false

    if (signingPubKeyHex.isEmpty() || txnSignatureHex.isEmpty()) return false

    // Re-encode for signing (strips TxnSignature and Signers, prepends signing prefix)
    val signingHex = BinaryCodec.encodeForSigning(decodedJson)
    val signingBytes = signingHex.hexToByteArray()
    val signatureBytes = txnSignatureHex.hexToByteArray()
    val publicKeyBytes = signingPubKeyHex.hexToByteArray()

    return if (signingPubKeyHex.uppercase().startsWith("ED")) {
        // Ed25519: public key is 33 bytes with 0xED prefix; strip the prefix for verification
        val rawPublicKey = publicKeyBytes.copyOfRange(1, publicKeyBytes.size)
        provider.ed25519Verify(signingBytes, signatureBytes, rawPublicKey)
    } else {
        // secp256k1: verify against SHA-512Half of the signing bytes
        provider.secp256k1Verify(provider.sha512Half(signingBytes), signatureBytes, publicKeyBytes)
    }
}

/**
 * Extracts a string field value from a JSON string using simple text search.
 *
 * This avoids a full JSON parse dependency in commonMain for a simple key lookup.
 */
private fun extractJsonField(
    json: String,
    fieldName: String,
): String? {
    val key = "\"$fieldName\""
    val keyIndex = json.indexOf(key)
    if (keyIndex < 0) return null

    val colonIndex = json.indexOf(':', keyIndex + key.length)
    if (colonIndex < 0) return null

    val valueStart =
        json.indexOfFirst { false }.let {
            var i = colonIndex + 1
            while (i < json.length && json[i].isWhitespace()) i++
            i
        }
    if (valueStart >= json.length) return null

    return when (json[valueStart]) {
        '"' -> {
            val end = json.indexOf('"', valueStart + 1)
            if (end < 0) null else json.substring(valueStart + 1, end)
        }
        else -> {
            val end =
                json.indexOfFirst { false }.let {
                    var i = valueStart
                    while (i < json.length && json[i] != ',' && json[i] != '}') i++
                    i
                }
            json.substring(valueStart, end).trim()
        }
    }
}
