package org.xrpl.sdk.codec.hashing

import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.hashing.sha512HalfWithPrefix

/**
 * Encodes an integer as a big-endian hex string with the specified byte length.
 */
private fun intToHex(
    value: Long,
    byteLength: Int,
): String = value.toString(16).padStart(byteLength * 2, '0')

/**
 * Computes the hash of a ledger header.
 *
 * The ledger header hash is SHA-512Half of the LEDGER prefix concatenated with:
 * - ledger sequence (4 bytes)
 * - total coins (8 bytes)
 * - parent hash (32 bytes)
 * - transaction hash (32 bytes)
 * - state/account hash (32 bytes)
 * - parent close time (4 bytes)
 * - close time (4 bytes)
 * - close time resolution (1 byte)
 * - close flags (1 byte)
 *
 * @return 64-char lowercase hex hash string.
 */
@Suppress("LongParameterList")
public fun hashLedgerHeader(
    ledgerSequence: Long,
    totalCoins: Long,
    parentHash: String,
    transactionHash: String,
    stateHash: String,
    parentCloseTime: Long,
    closeTime: Long,
    closeTimeResolution: Int,
    closeFlags: Int,
    provider: CryptoProvider,
): String {
    val hex =
        buildString {
            append(intToHex(ledgerSequence, 4))
            append(intToHex(totalCoins, 8))
            append(parentHash)
            append(transactionHash)
            append(stateHash)
            append(intToHex(parentCloseTime, 4))
            append(intToHex(closeTime, 4))
            append(intToHex(closeTimeResolution.toLong(), 1))
            append(intToHex(closeFlags.toLong(), 1))
        }

    return sha512HalfWithPrefix(HashPrefix.LEDGER.bytes, hex.hexToByteArray(), provider)
        .toHexString()
}

/**
 * Computes the transaction tree hash from a list of transaction blobs with metadata.
 *
 * Each pair is (txBlob, metaBlob) as hex strings.
 * The tag for each entry is the transaction hash (SHA-512Half of TRANSACTION_ID + txBlob).
 * The data for each entry is the length-prefixed txBlob + length-prefixed metaBlob.
 *
 * @return 64-char lowercase hex hash string of the SHAMap root.
 */
public fun hashTxTree(
    transactions: List<Pair<String, String>>,
    provider: CryptoProvider,
): String {
    val shaMap = ShaMap(provider)
    for ((txBlob, metaBlob) in transactions) {
        // Compute transaction hash = sha512Half(TRANSACTION_ID prefix + txBlob bytes)
        val txHash =
            sha512HalfWithPrefix(
                HashPrefix.TRANSACTION_ID.bytes,
                txBlob.hexToByteArray(),
                provider,
            ).toHexString()

        val data = addLengthPrefix(txBlob) + addLengthPrefix(metaBlob)
        shaMap.addItem(txHash, data, ShaMapNodeType.TRANSACTION_METADATA)
    }
    return shaMap.hash
}

/**
 * Computes the state tree hash from a list of state entry blobs.
 *
 * Each pair is (index, dataBlob) where index is a 64-char hex string.
 *
 * @return 64-char lowercase hex hash string of the SHAMap root.
 */
public fun hashStateTree(
    entries: List<Pair<String, String>>,
    provider: CryptoProvider,
): String {
    val shaMap = ShaMap(provider)
    for ((index, dataBlob) in entries) {
        shaMap.addItem(index, dataBlob, ShaMapNodeType.ACCOUNT_STATE)
    }
    return shaMap.hash
}

/**
 * Adds a variable-length prefix to a hex-encoded blob, following the XRPL VL encoding scheme.
 *
 * The encoding uses:
 * - 1 byte for lengths 0-192
 * - 2 bytes for lengths 193-12480
 * - 3 bytes for lengths 12481-918744
 */
@Suppress("MagicNumber")
private fun addLengthPrefix(hex: String): String {
    val length = hex.length / 2
    val prefix =
        when {
            length <= 192 -> {
                byteArrayOf(length.toByte())
            }
            length <= 12480 -> {
                val adjusted = length - 193
                byteArrayOf(
                    (193 + (adjusted ushr 8)).toByte(),
                    (adjusted and 0xFF).toByte(),
                )
            }
            length <= 918744 -> {
                val adjusted = length - 12481
                byteArrayOf(
                    (241 + (adjusted ushr 16)).toByte(),
                    ((adjusted ushr 8) and 0xFF).toByte(),
                    (adjusted and 0xFF).toByte(),
                )
            }
            else -> error("Variable integer overflow.")
        }
    return prefix.toHexString() + hex
}
