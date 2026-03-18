@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.util

import kotlinx.serialization.json.Json
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
import org.xrpl.sdk.crypto.hashing.sha512HalfWithPrefix

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
    val hash = sha512HalfWithPrefix(hashPrefix, txBlobBytes, provider).toHexString().uppercase()
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

                // RippleState Balance is an IOU object: {"value":"...","currency":"...","issuer":"..."}
                val previousBalanceStr =
                    (previousFields["Balance"] as? JsonObject)
                        ?.get("value")?.jsonPrimitive?.content ?: continue
                val finalBalanceStr =
                    (finalFields["Balance"] as? JsonObject)
                        ?.get("value")?.jsonPrimitive?.content ?: continue

                val delta = subtractDecimalStrings(finalBalanceStr, previousBalanceStr) ?: continue

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
                        value = delta,
                        counterparty = highAddress,
                    ),
                )
                addChange(
                    highAddress,
                    BalanceChange(
                        currency = currency,
                        value = negateDecimalString(delta),
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
    val jsonObj = Json.parseToJsonElement(decodedJson).jsonObject

    val signingPubKeyHex = jsonObj["SigningPubKey"]?.jsonPrimitive?.content ?: return false
    val txnSignatureHex = jsonObj["TxnSignature"]?.jsonPrimitive?.content ?: return false

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
 * Subtracts decimal string [b] from [a], returning the result as a decimal string.
 * Uses pure string-based arithmetic to preserve full precision for XRPL IOU amounts
 * (up to 15 significant digits with exponents up to e80), avoiding both Long overflow
 * and floating-point precision loss.
 *
 * @return The difference as a decimal string, or `null` if either input cannot be parsed.
 */
private fun subtractDecimalStrings(
    a: String,
    b: String,
): String? {
    // Parse sign and absolute value
    val aNeg = a.startsWith('-')
    val bNeg = b.startsWith('-')
    val aAbs = if (aNeg) a.substring(1) else a
    val bAbs = if (bNeg) b.substring(1) else b

    // Validate: digits and at most one dot
    fun isValidDecimal(s: String): Boolean =
        s.isNotEmpty() && s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1
    if (!isValidDecimal(aAbs) || !isValidDecimal(bAbs)) return null

    // Split into integer and fractional parts, align fractional lengths
    val aParts = aAbs.split('.')
    val bParts = bAbs.split('.')
    val aFrac = aParts.getOrElse(1) { "" }
    val bFrac = bParts.getOrElse(1) { "" }
    val maxFrac = maxOf(aFrac.length, bFrac.length)

    // Build scaled integer strings (no decimal point)
    val aScaled = aParts[0] + aFrac.padEnd(maxFrac, '0')
    val bScaled = bParts[0] + bFrac.padEnd(maxFrac, '0')

    // a - b = aSign*aScaled - bSign*bScaled
    // Convert to: if signs same => subtract magnitudes; if different => add magnitudes
    val aPositive = !aNeg
    val bPositive = !bNeg

    // We compute a - b, so the effective operation is aSign*|a| + (-bSign)*|b|
    // Effective: aPositive * aScaled - bPositive * bScaled
    val resultScaled: String
    val resultNeg: Boolean

    if (aPositive && bPositive) {
        // a - b
        val cmp = compareNumericStrings(aScaled, bScaled)
        when {
            cmp == 0 -> {
                resultScaled = "0"
                resultNeg = false
            }
            cmp > 0 -> {
                resultScaled = subtractBigUnsigned(aScaled, bScaled)
                resultNeg = false
            }
            else -> {
                resultScaled = subtractBigUnsigned(bScaled, aScaled)
                resultNeg = true
            }
        }
    } else if (!aPositive && !bPositive) {
        // (-|a|) - (-|b|) = |b| - |a|
        val cmp = compareNumericStrings(bScaled, aScaled)
        when {
            cmp == 0 -> {
                resultScaled = "0"
                resultNeg = false
            }
            cmp > 0 -> {
                resultScaled = subtractBigUnsigned(bScaled, aScaled)
                resultNeg = false
            }
            else -> {
                resultScaled = subtractBigUnsigned(aScaled, bScaled)
                resultNeg = true
            }
        }
    } else if (aPositive) {
        // |a| - (-|b|) = |a| + |b|
        resultScaled = addBigUnsigned(aScaled, bScaled)
        resultNeg = false
    } else {
        // (-|a|) - |b| = -(|a| + |b|)
        resultScaled = addBigUnsigned(aScaled, bScaled)
        resultNeg = resultScaled != "0"
    }

    // Re-insert decimal point
    val result =
        if (maxFrac == 0) {
            resultScaled.trimStart('0').ifEmpty { "0" }
        } else {
            val padded = resultScaled.padStart(maxFrac + 1, '0')
            val intPart = padded.substring(0, padded.length - maxFrac).trimStart('0').ifEmpty { "0" }
            val fracPart = padded.substring(padded.length - maxFrac).trimEnd('0')
            if (fracPart.isEmpty()) intPart else "$intPart.$fracPart"
        }

    return if (resultNeg && result != "0") "-$result" else result
}

/** Compares two non-negative integer strings by numeric value. */
private fun compareNumericStrings(
    a: String,
    b: String,
): Int {
    val aT = a.trimStart('0').ifEmpty { "0" }
    val bT = b.trimStart('0').ifEmpty { "0" }
    if (aT.length != bT.length) return aT.length.compareTo(bT.length)
    return aT.compareTo(bT)
}

/** Adds two non-negative integer strings. */
private fun addBigUnsigned(
    a: String,
    b: String,
): String {
    val maxLen = maxOf(a.length, b.length)
    val aP = a.padStart(maxLen, '0')
    val bP = b.padStart(maxLen, '0')
    val result = CharArray(maxLen + 1)
    var carry = 0
    for (i in maxLen - 1 downTo 0) {
        val sum = (aP[i] - '0') + (bP[i] - '0') + carry
        result[i + 1] = '0' + (sum % 10)
        carry = sum / 10
    }
    result[0] = '0' + carry
    return result.concatToString().trimStart('0').ifEmpty { "0" }
}

/** Subtracts two non-negative integer strings; caller guarantees a >= b. */
private fun subtractBigUnsigned(
    a: String,
    b: String,
): String {
    val maxLen = maxOf(a.length, b.length)
    val aP = a.padStart(maxLen, '0')
    val bP = b.padStart(maxLen, '0')
    val result = CharArray(maxLen)
    var borrow = 0
    for (i in maxLen - 1 downTo 0) {
        var diff = (aP[i] - '0') - (bP[i] - '0') - borrow
        if (diff < 0) {
            diff += 10
            borrow = 1
        } else {
            borrow = 0
        }
        result[i] = '0' + diff
    }
    return result.concatToString().trimStart('0').ifEmpty { "0" }
}

private fun negateDecimalString(s: String): String =
    when {
        s.startsWith('-') -> s.substring(1)
        s == "0" -> s
        else -> "-$s"
    }
