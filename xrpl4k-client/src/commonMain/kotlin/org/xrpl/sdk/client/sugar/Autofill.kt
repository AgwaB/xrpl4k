package org.xrpl.sdk.client.sugar

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.fee
import org.xrpl.sdk.client.rpc.ledgerCurrent
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrNull
import org.xrpl.sdk.core.type.XrpDrops
import kotlin.math.roundToLong

/**
 * Auto-fills an [XrplTransaction.Unsigned] with fee, sequence, and lastLedgerSequence
 * from the network.
 *
 * Fetches fee, account info, and current ledger index in parallel for performance.
 *
 * **Sequence number race**: When multiple clients (or concurrent coroutines) share the same
 * XRPL account, the sequence number fetched by `autofill()` may become stale between the
 * `accountInfo` call and `submit()`, resulting in a `tefPAST_SEQ` error. This is inherent to
 * the XRPL protocol and affects all SDKs. For high-throughput use cases with shared accounts,
 * callers should implement application-level sequence management (e.g., local atomic counter
 * seeded from `accountInfo` once, then incremented locally per submission).
 *
 * @param tx the unsigned transaction to fill.
 * @return a [XrplResult] containing the filled transaction or a failure.
 */
public suspend fun XrplClient.autofill(tx: XrplTransaction.Unsigned): XrplResult<XrplTransaction.Filled> =
    coroutineScope {
        val feeDeferred = async { fee() }
        val accountInfoDeferred = async { accountInfo(tx.account) }
        val ledgerDeferred = async { ledgerCurrent() }

        val feeResult = feeDeferred.await()
        val accountInfoResult = accountInfoDeferred.await()
        val ledgerResult = ledgerDeferred.await()

        val feeInfo =
            feeResult.getOrNull()
                ?: return@coroutineScope XrplResult.Failure(
                    (feeResult as XrplResult.Failure).error,
                )

        val accountInfo =
            accountInfoResult.getOrNull()
                ?: return@coroutineScope XrplResult.Failure(
                    (accountInfoResult as XrplResult.Failure).error,
                )

        val currentLedger =
            ledgerResult.getOrNull()
                ?: return@coroutineScope XrplResult.Failure(
                    (ledgerResult as XrplResult.Failure).error,
                )

        // Calculate fee with cushion, capped at maxFeeXrp
        val baseFeeDrops = feeInfo.openLedgerFee.value
        val cushionedFee = (baseFeeDrops * config.feeCushion).roundToLong()
        val maxFeeDrops = (config.maxFeeXrp * 1_000_000).toLong()

        if (cushionedFee > maxFeeDrops) {
            return@coroutineScope XrplResult.Failure(
                XrplFailure.ValidationError(
                    "Calculated fee ($cushionedFee drops) exceeds " +
                        "maxFeeXrp (${config.maxFeeXrp} XRP = $maxFeeDrops drops)",
                ),
            )
        }

        val fee = XrpDrops(cushionedFee)
        val sequence = accountInfo.sequence
        val lastLedgerSequence = currentLedger.value.toUInt() + 20u
        val networkId = config.network.networkId

        val filled =
            XrplTransaction.Filled.create(
                transactionType = tx.transactionType,
                account = tx.account,
                fields = tx.fields,
                memos = tx.memos,
                sourceTag = tx.sourceTag,
                fee = fee,
                sequence = sequence,
                lastLedgerSequence = lastLedgerSequence,
                networkId = networkId,
                flags = tx.flags,
            )

        XrplResult.Success(filled)
    }
