package org.xrpl.sdk.client.rpc

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.model.AccountOffer
import org.xrpl.sdk.client.model.AccountTxEntry
import org.xrpl.sdk.client.model.LedgerSpecifier
import org.xrpl.sdk.client.model.LedgerStateObject
import org.xrpl.sdk.client.model.PaymentChannel
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

/**
 * Returns a [Flow] of all ledger objects owned by the given account, automatically paginating.
 *
 * Each emitted element is the raw [JsonElement] of a ledger object. Use
 * [org.xrpl.sdk.client.model.AccountObjectsResult.objects] on the single-page result
 * if you need typed [org.xrpl.sdk.client.model.LedgerObject] instances.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @param account the account address.
 * @param type optional filter for object type (e.g., "offer", "escrow").
 * @param ledgerSpecifier which ledger to query. Defaults to [LedgerSpecifier.Validated].
 * @param pageSize optional page size per request.
 */
public fun XrplClient.allAccountObjects(
    account: Address,
    type: String? = null,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    pageSize: Int? = null,
): Flow<JsonElement> =
    cursorFlow { marker ->
        val result =
            accountObjects(
                account = account,
                ledgerSpecifier = ledgerSpecifier,
                type = type,
                marker = marker,
                limit = pageSize,
            ).getOrThrowForFlow()
        result.accountObjects to result.marker
    }

/**
 * Returns a [Flow] of all ledger objects owned by the given account, automatically paginating.
 *
 * This overload accepts a type-safe [AccountObjectType] filter.
 *
 * @param account the account address.
 * @param type optional filter for object type.
 * @param ledgerSpecifier which ledger to query. Defaults to [LedgerSpecifier.Validated].
 * @param pageSize optional page size per request.
 */
public fun XrplClient.allAccountObjects(
    account: Address,
    type: AccountObjectType?,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    pageSize: Int? = null,
): Flow<JsonElement> =
    allAccountObjects(
        account = account,
        type = type?.value,
        ledgerSpecifier = ledgerSpecifier,
        pageSize = pageSize,
    )

/**
 * Returns a [Flow] of all offers placed by the given account, automatically paginating.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @param account the account address.
 * @param ledgerSpecifier which ledger to query. Defaults to [LedgerSpecifier.Validated].
 * @param pageSize optional page size per request.
 */
public fun XrplClient.allAccountOffers(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    pageSize: Int? = null,
): Flow<AccountOffer> =
    cursorFlow { marker ->
        val result =
            accountOffers(
                account = account,
                ledgerSpecifier = ledgerSpecifier,
                marker = marker,
                limit = pageSize,
            ).getOrThrowForFlow()
        result.offers to result.marker
    }

/**
 * Returns a [Flow] of all payment channels where the given account is the source,
 * automatically paginating.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @param account the source account address.
 * @param destinationAccount optional filter for a specific destination account.
 * @param ledgerSpecifier which ledger to query. Defaults to [LedgerSpecifier.Validated].
 * @param pageSize optional page size per request.
 */
public fun XrplClient.allAccountChannels(
    account: Address,
    destinationAccount: Address? = null,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    pageSize: Int? = null,
): Flow<PaymentChannel> =
    cursorFlow { marker ->
        val result =
            accountChannels(
                account = account,
                destinationAccount = destinationAccount,
                ledgerSpecifier = ledgerSpecifier,
                marker = marker,
                limit = pageSize,
            ).getOrThrowForFlow()
        result.channels to result.marker
    }

/**
 * Returns a [Flow] of all ledger state objects, automatically paginating through
 * the entire ledger state tree.
 *
 * **Warning:** The full ledger state is very large. Consider using `take(N)` or
 * applying filters after collection to limit resource usage.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @param ledger which ledger version to query. Defaults to [LedgerSpecifier.Validated].
 * @param binary whether to return objects as binary hex blobs.
 * @param pageSize optional page size per request.
 */
public fun XrplClient.allLedgerData(
    ledger: LedgerSpecifier = LedgerSpecifier.Validated,
    binary: Boolean = false,
    pageSize: Int? = null,
): Flow<LedgerStateObject> =
    cursorFlow { marker ->
        val result =
            ledgerData(
                ledger = ledger,
                binary = binary,
                limit = pageSize,
                marker = marker,
            ).getOrThrowForFlow()
        result.state to result.marker
    }
