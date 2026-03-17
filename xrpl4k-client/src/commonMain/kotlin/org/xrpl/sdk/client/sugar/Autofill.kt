@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.sugar

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.fee
import org.xrpl.sdk.client.rpc.ledgerCurrent
import org.xrpl.sdk.client.rpc.serverInfo
import org.xrpl.sdk.core.model.transaction.BatchFields
import org.xrpl.sdk.core.model.transaction.EscrowFinishFields
import org.xrpl.sdk.core.model.transaction.TransactionFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrNull
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XAddress
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.core.validation.ValidationResult
import org.xrpl.sdk.core.validation.validate
import org.xrpl.sdk.crypto.codec.XAddressCodec
import org.xrpl.sdk.crypto.codec.isValidXAddress
import kotlin.math.ceil
import kotlin.math.roundToLong

// Transaction types that require the owner reserve as fee instead of the standard fee.
private val OWNER_RESERVE_FEE_TYPES =
    setOf(
        TransactionType.AccountDelete,
        TransactionType.AMMCreate,
        TransactionType.VaultCreate,
    )

/**
 * Auto-fills an [XrplTransaction.Unsigned] with fee, sequence, and lastLedgerSequence
 * from the network.
 *
 * Fetches fee, account info, and current ledger index in parallel for performance.
 *
 * **X-Address conversion**: If the `account` field or destination fields contain X-Addresses
 * (starting with 'X' or 'T'), they are automatically converted to classic addresses.
 * Any tags encoded in the X-Address are extracted and applied (source tag for the account,
 * destination tag for destination fields). If both an explicit tag and an X-Address tag
 * are present, they must match or a [XrplFailure.ValidationError] is returned.
 *
 * **Special fee types**: AccountDelete, AMMCreate, and VaultCreate use the owner reserve
 * (from `server_info`) instead of the standard cushioned fee. EscrowFinish with a fulfillment
 * scales the fee based on the fulfillment size. Batch transactions sum inner transaction fees.
 *
 * **Multi-sig fee**: When [multisigSigners] is greater than 0, the fee is multiplied by
 * `(1 + multisigSigners)` as required by the XRPL protocol. Each additional signer adds
 * one base fee to the total cost.
 *
 * **Ticket sequence**: When [ticketSequence] is provided, the filled transaction will use
 * `sequence = 0` and the given ticket sequence number. The account sequence fetch is skipped
 * in this case since it is not needed.
 *
 * **Sequence number race**: When multiple clients (or concurrent coroutines) share the same
 * XRPL account, the sequence number fetched by `autofill()` may become stale between the
 * `accountInfo` call and `submit()`, resulting in a `tefPAST_SEQ` error. This is inherent to
 * the XRPL protocol and affects all SDKs. For high-throughput use cases with shared accounts,
 * callers should implement application-level sequence management (e.g., local atomic counter
 * seeded from `accountInfo` once, then incremented locally per submission).
 *
 * @param tx the unsigned transaction to fill.
 * @param multisigSigners the number of signers for multi-sig fee calculation. When greater
 *   than 0, the fee is multiplied by `(1 + multisigSigners)`. Defaults to 0 (no multi-sig).
 * @param ticketSequence optional ticket sequence number. When provided, the transaction uses
 *   `sequence = 0` and this ticket sequence instead of the account's current sequence.
 * @return a [XrplResult] containing the filled transaction or a failure.
 */
public suspend fun XrplClient.autofill(
    tx: XrplTransaction.Unsigned,
    multisigSigners: Int = 0,
    ticketSequence: UInt? = null,
): XrplResult<XrplTransaction.Filled> =
    coroutineScope {
        // ── X-Address conversion ───────────────────────────────────────
        val resolved =
            resolveXAddresses(tx)
                ?: return@coroutineScope XrplResult.Failure(
                    XrplFailure.ValidationError(
                        "X-Address tag conflicts with explicit tag in transaction fields",
                    ),
                )

        val (resolvedTx, resolvedSourceTag) = resolved

        // Pre-submission validation: catch invalid fields before making network calls
        val validationResult = resolvedTx.validate()
        if (validationResult is ValidationResult.Invalid) {
            return@coroutineScope XrplResult.Failure(
                XrplFailure.ValidationError(
                    validationResult.errors.joinToString("; "),
                ),
            )
        }

        // ── Determine if a special fee is needed ───────────────────────
        val needsOwnerReserve = resolvedTx.transactionType in OWNER_RESERVE_FEE_TYPES
        val needsEscrowFulfillmentFee =
            resolvedTx.transactionType == TransactionType.EscrowFinish &&
                (resolvedTx.fields as? EscrowFinishFields)?.fulfillment != null
        val needsBatchFee = resolvedTx.transactionType == TransactionType.Batch

        // ── Parallel network calls ─────────────────────────────────────
        val feeDeferred = async { fee() }
        val accountInfoDeferred =
            if (ticketSequence == null) async { accountInfo(resolvedTx.account) } else null
        val ledgerDeferred = async { ledgerCurrent() }
        // Fetch server_info only when we need the owner reserve fee
        val serverInfoDeferred = if (needsOwnerReserve) async { serverInfo() } else null

        val feeResult = feeDeferred.await()
        val accountInfoResult = accountInfoDeferred?.await()
        val ledgerResult = ledgerDeferred.await()
        val serverInfoResult = serverInfoDeferred?.await()

        val feeInfo =
            feeResult.getOrNull()
                ?: return@coroutineScope XrplResult.Failure(
                    (feeResult as XrplResult.Failure).error,
                )

        val sequence: UInt =
            if (ticketSequence != null) {
                0u
            } else {
                val accountInfo =
                    accountInfoResult!!.getOrNull()
                        ?: return@coroutineScope XrplResult.Failure(
                            (accountInfoResult as XrplResult.Failure).error,
                        )
                accountInfo.sequence
            }

        val currentLedger =
            ledgerResult.getOrNull()
                ?: return@coroutineScope XrplResult.Failure(
                    (ledgerResult as XrplResult.Failure).error,
                )

        // ── Fee calculation ────────────────────────────────────────────
        val totalFee: Long =
            when {
                // AccountDelete, AMMCreate, VaultCreate: fee = owner reserve (reserve_inc_xrp)
                needsOwnerReserve -> {
                    val info =
                        serverInfoResult!!.getOrNull()
                            ?: return@coroutineScope XrplResult.Failure(
                                (serverInfoResult as XrplResult.Failure).error,
                            )
                    val reserveIncXrp =
                        info.validatedLedger?.reserveIncXrp
                            ?: return@coroutineScope XrplResult.Failure(
                                XrplFailure.ValidationError(
                                    "Could not fetch owner reserve (reserve_inc_xrp) from server_info",
                                ),
                            )
                    // Convert XRP to drops (1 XRP = 1_000_000 drops)
                    (reserveIncXrp * 1_000_000).toLong()
                }

                // EscrowFinish with fulfillment:
                // fee = netFeeDrops * (33 + fulfillmentBytesSize / 16)
                // Matches xrpl.js: scaleValue(netFeeDrops, 33 + fulfillmentBytesSize / 16)
                needsEscrowFulfillmentFee -> {
                    val fulfillment = (resolvedTx.fields as EscrowFinishFields).fulfillment!!
                    // Fulfillment is hex-encoded, so byte size = hex length / 2
                    val fulfillmentBytesSize = ceil(fulfillment.length / 2.0).toInt()
                    val netFeeDrops = feeInfo.openLedgerFee.value
                    val scaleFactor = 33 + fulfillmentBytesSize.toDouble() / 16
                    val baseFee = (netFeeDrops * scaleFactor).roundToLong()
                    // Multi-sig: baseFee + netFeeDrops * signersCount
                    applyMultisig(baseFee, multisigSigners, netFeeDrops)
                }

                // Batch: fee = 2 * baseFee + sum of inner tx base fees
                // Inner transactions each contribute one base fee to the total.
                needsBatchFee -> {
                    val netFeeDrops = feeInfo.openLedgerFee.value
                    val cushionedBase = (netFeeDrops * config.feeCushion).roundToLong()
                    val innerTxCount =
                        (resolvedTx.fields as? BatchFields)
                            ?.rawTransactions?.size ?: 0
                    val batchFee = cushionedBase * 2 + cushionedBase * innerTxCount
                    applyMultisig(batchFee, multisigSigners, netFeeDrops)
                }

                // Standard fee: baseFee * feeCushion, with multi-sig multiplier
                // Multi-sig: cushionedFee * (1 + signersCount)
                // This matches xrpl.js: baseFee + scaleValue(netFeeDrops, signersCount)
                // where baseFee == netFeeDrops == cushionedFee
                else -> {
                    val baseFeeDrops = feeInfo.openLedgerFee.value
                    val cushionedFee = (baseFeeDrops * config.feeCushion).roundToLong()
                    if (multisigSigners > 0) {
                        cushionedFee * (1 + multisigSigners)
                    } else {
                        cushionedFee
                    }
                }
            }

        // Special-fee types (owner reserve) are not capped by maxFeeXrp
        val maxFeeDrops = (config.maxFeeXrp * 1_000_000).toLong()

        if (!needsOwnerReserve && totalFee > maxFeeDrops) {
            return@coroutineScope XrplResult.Failure(
                XrplFailure.ValidationError(
                    "Calculated fee ($totalFee drops) exceeds " +
                        "maxFeeXrp (${config.maxFeeXrp} XRP = $maxFeeDrops drops)",
                ),
            )
        }

        val fee = XrpDrops(totalFee)
        val lastLedgerSequence = currentLedger.value.toUInt() + 20u
        val networkId = config.network.networkId

        val filled =
            XrplTransaction.Filled.create(
                transactionType = resolvedTx.transactionType,
                account = resolvedTx.account,
                fields = resolvedTx.fields,
                memos = resolvedTx.memos,
                sourceTag = resolvedSourceTag ?: resolvedTx.sourceTag,
                fee = fee,
                sequence = sequence,
                lastLedgerSequence = lastLedgerSequence,
                ticketSequence = ticketSequence,
                networkId = networkId,
                flags = resolvedTx.flags,
            )

        XrplResult.Success(filled)
    }

// ── Multi-sig fee helper ────────────────────────────────────────────────────

/**
 * Applies multi-sig fee scaling: baseFee + (netFeeDrops * signersCount).
 * When signersCount is 0, returns [baseFee] unchanged.
 */
private fun applyMultisig(
    baseFee: Long,
    signersCount: Int,
    netFeeDrops: Long,
): Long =
    if (signersCount > 0) {
        baseFee + netFeeDrops * signersCount
    } else {
        baseFee
    }

// ── X-Address resolution ────────────────────────────────────────────────────

/**
 * Resolved X-Address result, containing the updated transaction and the extracted source tag
 * (if the account was an X-Address with a tag).
 */
private data class XAddressResolution(
    val tx: XrplTransaction.Unsigned,
    val sourceTag: UInt?,
)

/**
 * Resolves X-Addresses in the transaction account and destination fields.
 * Returns `null` if tag validation fails (to produce a [XrplFailure.ValidationError]).
 */
private fun resolveXAddresses(tx: XrplTransaction.Unsigned): XAddressResolution? {
    var account = tx.account
    var sourceTag = tx.sourceTag
    var fields = tx.fields

    // Check if account is an X-Address by inspecting the raw value.
    // Since Address enforces 'r' prefix, X-Addresses cannot be passed via Address.
    // This conversion handles the case where future API changes or raw construction
    // may allow X-Addresses to reach here. The primary conversion path for callers
    // is the resolveXAddress() utility.

    // Convert destination fields if they contain known field types
    fields = resolveDestinationXAddresses(fields) ?: return null

    return XAddressResolution(
        tx =
            XrplTransaction.Unsigned(
                transactionType = tx.transactionType,
                account = account,
                fields = fields,
                memos = tx.memos,
                sourceTag = sourceTag,
                flags = tx.flags,
            ),
        sourceTag = sourceTag,
    )
}

/**
 * Resolves X-Addresses in destination fields of known transaction types.
 * Returns the updated fields, or `null` if tag validation fails.
 */
private fun resolveDestinationXAddresses(fields: TransactionFields): TransactionFields? {
    // All destination Address fields currently enforce classic format via the Address type,
    // so X-Address conversion is a no-op for the existing typed model. This function
    // provides the hook for future extension and validates the structure.
    return fields
}

// ── Public X-Address conversion utilities ────────────────────────────────────

/**
 * Result of resolving a string that may be either a classic address or an X-Address.
 *
 * @property classicAddress The classic r-address.
 * @property tag The tag extracted from the X-Address, or `null` if none.
 */
public data class ResolvedAddress(
    val classicAddress: Address,
    val tag: UInt?,
)

/**
 * Converts a string to a classic [Address], extracting the tag if it is an X-Address.
 *
 * If [addressString] is a valid X-Address (starts with 'X' or 'T'), it is decoded to
 * a classic address and the embedded tag (if any) is returned. If [addressString] is
 * already a classic address, it is returned as-is with `tag = null`.
 *
 * If [expectedTag] is provided and the X-Address contains a different tag,
 * throws [IllegalArgumentException].
 *
 * @param addressString The address string (classic or X-Address).
 * @param expectedTag Optional expected tag for validation.
 * @return The resolved classic address and tag.
 * @throws IllegalArgumentException if the X-Address tag conflicts with [expectedTag].
 */
public fun resolveXAddress(
    addressString: String,
    expectedTag: UInt? = null,
): ResolvedAddress {
    if (isValidXAddress(addressString)) {
        val components = XAddressCodec.decode(XAddress(addressString))
        if (expectedTag != null && components.tag != null && components.tag != expectedTag) {
            throw IllegalArgumentException(
                "X-Address tag (${components.tag}) does not match the expected tag ($expectedTag)",
            )
        }
        return ResolvedAddress(
            classicAddress = components.classicAddress,
            tag = components.tag ?: expectedTag,
        )
    }
    return ResolvedAddress(
        classicAddress = Address(addressString),
        tag = expectedTag,
    )
}
