package org.xrpl.sdk.client.rpc

import kotlinx.coroutines.flow.Flow
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.model.AccountTxEntry
import org.xrpl.sdk.client.model.LedgerSpecifier
import org.xrpl.sdk.client.model.TrustLine
import org.xrpl.sdk.client.pagination.cursorFlow
import org.xrpl.sdk.client.pagination.getOrThrowForFlow
import org.xrpl.sdk.core.type.Address

/**
 * Returns a [Flow] of all trust lines for the given account, automatically paginating.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @param account the account address.
 * @param ledgerSpecifier which ledger to query. Defaults to [LedgerSpecifier.Validated].
 * @param pageSize optional page size per request.
 */
public fun XrplClient.allAccountLines(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    pageSize: Int? = null,
): Flow<TrustLine> =
    cursorFlow { marker ->
        val result =
            accountLines(
                account = account,
                ledgerSpecifier = ledgerSpecifier,
                marker = marker,
                limit = pageSize,
            ).getOrThrowForFlow()
        result.lines to result.marker
    }

/**
 * Returns a [Flow] of all account transactions, automatically paginating.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @param account the account address.
 * @param ledgerIndexMin minimum ledger index (-1 for no minimum).
 * @param ledgerIndexMax maximum ledger index (-1 for no maximum).
 * @param pageSize optional page size per request.
 * @param forward if true, returns results in chronological order.
 */
public fun XrplClient.accountTransactions(
    account: Address,
    ledgerIndexMin: Long = -1,
    ledgerIndexMax: Long = -1,
    pageSize: Int? = null,
    forward: Boolean? = null,
): Flow<AccountTxEntry> =
    cursorFlow { marker ->
        val result =
            accountTx(
                account = account,
                ledgerIndexMin = ledgerIndexMin,
                ledgerIndexMax = ledgerIndexMax,
                marker = marker,
                limit = pageSize,
                forward = forward,
            ).getOrThrowForFlow()
        result.transactions to result.marker
    }
