@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.hashing

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.codec.AddressCodec

/**
 * XRPL ledger namespace prefixes.
 *
 * Each ledger object type lives in its own hash namespace, identified by a
 * single ASCII character. The prefix is encoded as a 2-byte big-endian value
 * (0x00 followed by the ASCII byte) before hashing.
 *
 * Reference: rippled `src/libxrpl/protocol/Indexes.cpp` and xrpl.js `ledgerSpaces.ts`.
 */
private object LedgerSpace {
    const val ACCOUNT: Char = 'a'
    const val OFFER: Char = 'o'
    const val RIPPLE_STATE: Char = 'r'
    const val ESCROW: Char = 'u'
    const val SIGNER_LIST: Char = 'S'
    const val PAYCHAN: Char = 'x'
    const val VAULT: Char = 'V'
    const val LOAN_BROKER: Char = 'l'
    const val LOAN: Char = 'L'
}

/**
 * Builds a 2-byte ledger namespace prefix from a single ASCII character.
 *
 * The format is `0x00` followed by the character's ASCII code, matching
 * the xrpl.js `ledgerSpaceHex` function.
 */
private fun ledgerSpacePrefix(space: Char): ByteArray = byteArrayOf(0x00, space.code.toByte())

/**
 * Decodes a classic r-address to its 20-byte Account ID hex string.
 */
private fun addressToBytes(
    address: String,
    provider: CryptoProvider,
): ByteArray = AddressCodec.decodeAddress(Address(address), provider).toByteArray()

/**
 * Encodes a UInt32 as a 4-byte big-endian byte array.
 */
private fun uintToBytes(value: Long): ByteArray {
    val v = value and 0xFFFFFFFFL
    return byteArrayOf(
        (v shr 24).toByte(),
        (v shr 16).toByte(),
        (v shr 8).toByte(),
        v.toByte(),
    )
}

/**
 * Computes the SHA-512Half hash with a ledger namespace prefix and concatenated data segments.
 */
private fun sha512HalfLedger(
    space: Char,
    vararg parts: ByteArray,
    provider: CryptoProvider,
): String {
    val prefix = ledgerSpacePrefix(space)
    var totalSize = prefix.size
    for (part in parts) totalSize += part.size

    val input = ByteArray(totalSize)
    prefix.copyInto(input)
    var offset = prefix.size
    for (part in parts) {
        part.copyInto(input, offset)
        offset += part.size
    }

    return provider.sha512Half(input).toHexString()
}

/**
 * Computes the AccountRoot ledger object index (hash).
 *
 * `SHA-512Half(0x0061 + AccountID)`
 *
 * @param address Classic r-address (e.g., "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh").
 * @return 64-character lowercase hex hash.
 */
public fun hashAccountRoot(
    address: String,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.ACCOUNT,
        addressToBytes(address, provider),
        provider = provider,
    )

/**
 * Computes the SignerList ledger object index (hash).
 *
 * `SHA-512Half(0x0053 + AccountID + 00000000)`
 *
 * The trailing `00000000` is the SignerListID which is currently always 0.
 *
 * @param address Classic r-address of the SignerList owner.
 * @return 64-character lowercase hex hash.
 */
public fun hashSignerListId(
    address: String,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.SIGNER_LIST,
        addressToBytes(address, provider),
        uintToBytes(0),
        provider = provider,
    )

/**
 * Computes the Offer ledger object index (hash).
 *
 * `SHA-512Half(0x006F + AccountID + Sequence)`
 *
 * @param address Classic r-address of the account that placed the offer.
 * @param sequence The sequence number of the OfferCreate transaction.
 * @return 64-character lowercase hex hash.
 */
public fun hashOfferId(
    address: String,
    sequence: Long,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.OFFER,
        addressToBytes(address, provider),
        uintToBytes(sequence),
        provider = provider,
    )

/**
 * Computes the RippleState (Trustline) ledger object index (hash).
 *
 * `SHA-512Half(0x0072 + lowAccountID + highAccountID + CurrencyCode)`
 *
 * The two accounts are sorted so the numerically lower Account ID comes first.
 * A 3-character standard currency code is encoded as a 20-byte (160-bit) value
 * with the ASCII bytes at positions 12-14.
 *
 * @param address1 One of the two classic r-addresses in the trust line.
 * @param address2 The other classic r-address in the trust line.
 * @param currency Either a 3-character standard currency code (e.g., "USD")
 *                 or a 40-character hex non-standard currency code.
 * @return 64-character lowercase hex hash.
 */
public fun hashTrustline(
    address1: String,
    address2: String,
    currency: String,
    provider: CryptoProvider,
): String {
    val acct1Bytes = addressToBytes(address1, provider)
    val acct2Bytes = addressToBytes(address2, provider)

    // Sort accounts numerically (compare as unsigned byte arrays).
    val swap = compareByteArrays(acct1Bytes, acct2Bytes) > 0
    val lowBytes = if (swap) acct2Bytes else acct1Bytes
    val highBytes = if (swap) acct1Bytes else acct2Bytes

    val currencyBytes = currencyToBytes(currency)

    return sha512HalfLedger(
        LedgerSpace.RIPPLE_STATE,
        lowBytes,
        highBytes,
        currencyBytes,
        provider = provider,
    )
}

/**
 * Computes the Escrow ledger object index (hash).
 *
 * `SHA-512Half(0x0075 + AccountID + Sequence)`
 *
 * @param address Classic r-address of the escrow creator.
 * @param sequence The sequence number of the EscrowCreate transaction.
 * @return 64-character lowercase hex hash.
 */
public fun hashEscrow(
    address: String,
    sequence: Long,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.ESCROW,
        addressToBytes(address, provider),
        uintToBytes(sequence),
        provider = provider,
    )

/**
 * Computes the PaymentChannel ledger object index (hash).
 *
 * `SHA-512Half(0x0078 + AccountID + DestinationID + Sequence)`
 *
 * @param address Classic r-address of the channel source.
 * @param destination Classic r-address of the channel destination.
 * @param sequence The sequence number of the PaymentChannelCreate transaction.
 * @return 64-character lowercase hex hash.
 */
public fun hashPaymentChannel(
    address: String,
    destination: String,
    sequence: Long,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.PAYCHAN,
        addressToBytes(address, provider),
        addressToBytes(destination, provider),
        uintToBytes(sequence),
        provider = provider,
    )

/**
 * Encodes a currency to 20 bytes.
 *
 * - 3-character standard codes are placed at bytes 12-14 of a 20-byte zero-filled array.
 * - Non-standard codes are expected as 40-character hex strings and decoded directly.
 */
private fun currencyToBytes(currency: String): ByteArray {
    if (currency.length != 3) {
        return currency.hexToByteArray()
    }
    val bytes = ByteArray(20)
    bytes[12] = (currency[0].code and 0xFF).toByte()
    bytes[13] = (currency[1].code and 0xFF).toByte()
    bytes[14] = (currency[2].code and 0xFF).toByte()
    return bytes
}

/**
 * Compares two byte arrays as unsigned big-endian integers.
 * Returns negative if a < b, positive if a > b, zero if equal.
 */
private fun compareByteArrays(
    a: ByteArray,
    b: ByteArray,
): Int {
    val len = minOf(a.size, b.size)
    for (i in 0 until len) {
        val diff = (a[i].toInt() and 0xFF) - (b[i].toInt() and 0xFF)
        if (diff != 0) return diff
    }
    return a.size - b.size
}

/**
 * Computes the Vault ledger object index (hash).
 *
 * `SHA-512Half(0x0056 + AccountID + Sequence)`
 *
 * @param address Classic r-address of the Vault owner (account submitting VaultCreate).
 * @param sequence The sequence number of the VaultCreate transaction.
 * @return 64-character lowercase hex hash.
 */
public fun hashVault(
    address: String,
    sequence: Long,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.VAULT,
        addressToBytes(address, provider),
        uintToBytes(sequence),
        provider = provider,
    )

/**
 * Computes the LoanBroker ledger object index (hash).
 *
 * `SHA-512Half(0x006C + AccountID + Sequence)`
 *
 * @param address Classic r-address of the Lender (LoanBrokerSet submitter).
 * @param sequence The sequence number of the LoanBrokerSet transaction.
 * @return 64-character lowercase hex hash.
 */
public fun hashLoanBroker(
    address: String,
    sequence: Long,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.LOAN_BROKER,
        addressToBytes(address, provider),
        uintToBytes(sequence),
        provider = provider,
    )

/**
 * Computes the Loan ledger object index (hash).
 *
 * `SHA-512Half(0x004C + LoanBrokerID + LoanSequence)`
 *
 * Note: [loanBrokerId] is the 64-character hex LoanBroker object ID,
 * not an r-address.
 *
 * @param loanBrokerId The 64-char hex ID of the associated LoanBroker object.
 * @param loanSequence The sequence number of the Loan.
 * @return 64-character lowercase hex hash.
 */
public fun hashLoan(
    loanBrokerId: String,
    loanSequence: Long,
    provider: CryptoProvider,
): String =
    sha512HalfLedger(
        LedgerSpace.LOAN,
        loanBrokerId.hexToByteArray(),
        uintToBytes(loanSequence),
        provider = provider,
    )

// ---------------------------------------------------------------------------
// Transaction and Ledger hashing
// ---------------------------------------------------------------------------

/**
 * Computes SHA-512Half with an arbitrary 4-byte prefix and concatenated parts.
 */
private fun sha512HalfPrefixed(
    prefix: ByteArray,
    vararg parts: ByteArray,
    provider: CryptoProvider,
): String {
    var totalSize = prefix.size
    for (part in parts) totalSize += part.size

    val input = ByteArray(totalSize)
    prefix.copyInto(input)
    var offset = prefix.size
    for (part in parts) {
        part.copyInto(input, offset)
        offset += part.size
    }

    return provider.sha512Half(input).toHexString()
}

/**
 * Hashes a transaction blob with the single-signing prefix (`STX\0`).
 *
 * This is the hash-to-sign for a transaction.
 *
 * @param txBlob Hex-encoded serialized transaction (no signature prefix).
 * @return 64-character lowercase hex hash.
 */
public fun hashTx(
    txBlob: String,
    provider: CryptoProvider,
): String =
    sha512HalfPrefixed(
        HashPrefix.TRANSACTION_SIGN.bytes,
        txBlob.hexToByteArray(),
        provider = provider,
    )

/**
 * Hashes a signed transaction blob to produce the transaction ID.
 *
 * Uses the `TXN\0` prefix, matching xrpl.js `hashSignedTx`.
 *
 * @param txBlob Hex-encoded signed transaction blob.
 * @return 64-character lowercase hex hash (the transaction ID).
 */
public fun hashSignedTx(
    txBlob: String,
    provider: CryptoProvider,
): String =
    sha512HalfPrefixed(
        HashPrefix.TRANSACTION_ID.bytes,
        txBlob.hexToByteArray(),
        provider = provider,
    )

/**
 * Hashes a ledger header to produce the ledger hash.
 *
 * Uses the `LWR\0` prefix, matching xrpl.js `hashLedgerHeader`.
 *
 * The caller must construct the ledger header bytes in the canonical order:
 * `ledgerIndex(4) + totalCoins(8) + parentHash(32) + transactionHash(32) +
 *  accountHash(32) + parentCloseTime(4) + closeTime(4) +
 *  closeTimeResolution(1) + closeFlags(1)`
 *
 * @param ledgerHeaderHex Hex-encoded concatenation of ledger header fields.
 * @return 64-character lowercase hex hash.
 */
public fun hashLedgerHeader(
    ledgerHeaderHex: String,
    provider: CryptoProvider,
): String =
    sha512HalfPrefixed(
        HashPrefix.LEDGER.bytes,
        ledgerHeaderHex.hexToByteArray(),
        provider = provider,
    )
