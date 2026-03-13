package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.LedgerClosedResponseDto
import org.xrpl.sdk.client.internal.dto.LedgerCurrentResponseDto
import org.xrpl.sdk.client.internal.dto.LedgerDataRequest
import org.xrpl.sdk.client.internal.dto.LedgerDataResponseDto
import org.xrpl.sdk.client.internal.dto.LedgerEntryRequest
import org.xrpl.sdk.client.internal.dto.LedgerEntryResponseDto
import org.xrpl.sdk.client.internal.dto.LedgerRequest
import org.xrpl.sdk.client.internal.dto.LedgerResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.LedgerClosedResult
import org.xrpl.sdk.client.model.LedgerDataResult
import org.xrpl.sdk.client.model.LedgerEntryResult
import org.xrpl.sdk.client.model.LedgerInfo
import org.xrpl.sdk.client.model.LedgerResult
import org.xrpl.sdk.client.model.LedgerSpecifier
import org.xrpl.sdk.client.model.LedgerStateObject
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Retrieves information about a specific ledger version.
 *
 * @param ledger which ledger version to query (default: [LedgerSpecifier.Validated]).
 * @param transactions whether to include the list of transactions.
 * @param expand whether to expand transactions to full JSON objects.
 * @param ownerFunds whether to include owner_funds fields in offer transactions.
 * @param binary whether to return transactions as binary hex blobs.
 * @return [XrplResult] containing [LedgerResult] or a categorized failure.
 */
public suspend fun XrplClient.ledger(
    ledger: LedgerSpecifier = LedgerSpecifier.Validated,
    transactions: Boolean = false,
    expand: Boolean = false,
    ownerFunds: Boolean = false,
    binary: Boolean = false,
): XrplResult<LedgerResult> {
    val (paramKey, paramValue) = ledger.toParamPair()
    val request =
        LedgerRequest(
            ledgerIndex = if (paramKey == "ledger_index") paramValue else null,
            ledgerHash = if (paramKey == "ledger_hash") paramValue else null,
            transactions = transactions,
            expand = expand,
            ownerFunds = ownerFunds,
            binary = binary,
        )
    return executeRpc(
        method = "ledger",
        request = request,
        requestSerializer = LedgerRequest.serializer(),
        responseDeserializer = LedgerResponseDto.serializer(),
    ) { dto ->
        LedgerResult(
            ledger =
                dto.ledger?.let { info ->
                    LedgerInfo(
                        ledgerHash = info.ledgerHash?.takeIf { it.length == 64 }?.let { Hash256(it) },
                        ledgerIndex = info.ledgerIndex?.let { LedgerIndex(it) },
                        accountHash = info.accountHash,
                        transactionHash = info.transactionHash,
                        parentHash = info.parentHash,
                        closeTime = info.closeTime,
                        closeTimeHuman = info.closeTimeHuman,
                        totalCoins = info.totalCoins?.toLongOrNull()?.let { XrpDrops(it) },
                        closed = info.closed,
                        transactions = info.transactions,
                    )
                },
            ledgerHash = dto.ledgerHash?.takeIf { it.length == 64 }?.let { Hash256(it) },
            ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it) },
            validated = dto.validated,
        )
    }
}

/**
 * Returns the hash and sequence number of the most recently closed ledger.
 *
 * @return [XrplResult] containing [LedgerClosedResult] or a categorized failure.
 */
public suspend fun XrplClient.ledgerClosed(): XrplResult<LedgerClosedResult> =
    executeRpc(
        method = "ledger_closed",
        responseDeserializer = LedgerClosedResponseDto.serializer(),
    ) { dto ->
        LedgerClosedResult(
            ledgerHash = Hash256(dto.ledgerHash),
            ledgerIndex = LedgerIndex(dto.ledgerIndex),
        )
    }

/**
 * Returns the sequence number of the current in-progress (open) ledger.
 *
 * @return [XrplResult] containing the current [LedgerIndex] or a categorized failure.
 */
public suspend fun XrplClient.ledgerCurrent(): XrplResult<LedgerIndex> =
    executeRpc(
        method = "ledger_current",
        responseDeserializer = LedgerCurrentResponseDto.serializer(),
    ) { dto ->
        LedgerIndex(dto.ledgerCurrentIndex)
    }

/**
 * Returns raw ledger state objects from the specified ledger version.
 *
 * @param ledger which ledger version to query (default: [LedgerSpecifier.Validated]).
 * @param binary whether to return objects as binary hex blobs.
 * @param limit maximum number of objects to return per page.
 * @param marker pagination marker returned from a previous call.
 * @return [XrplResult] containing [LedgerDataResult] or a categorized failure.
 */
public suspend fun XrplClient.ledgerData(
    ledger: LedgerSpecifier = LedgerSpecifier.Validated,
    binary: Boolean = false,
    limit: Int? = null,
    marker: JsonElement? = null,
): XrplResult<LedgerDataResult> {
    val (paramKey, paramValue) = ledger.toParamPair()
    val request =
        LedgerDataRequest(
            ledgerIndex = if (paramKey == "ledger_index") paramValue else null,
            ledgerHash = if (paramKey == "ledger_hash") paramValue else null,
            binary = binary,
            limit = limit,
            marker = marker,
        )
    return executeRpc(
        method = "ledger_data",
        request = request,
        requestSerializer = LedgerDataRequest.serializer(),
        responseDeserializer = LedgerDataResponseDto.serializer(),
    ) { dto ->
        LedgerDataResult(
            ledgerHash = dto.ledgerHash?.takeIf { it.length == 64 }?.let { Hash256(it) },
            ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it) },
            state =
                dto.state.map { obj ->
                    LedgerStateObject(
                        index = obj.index,
                        ledgerEntryType = obj.ledgerEntryType,
                        data = obj.data,
                    )
                },
            marker = dto.marker,
        )
    }
}

/**
 * Retrieves a single ledger object by its ID or type-specific identifier.
 *
 * @param ledger which ledger version to query (default: [LedgerSpecifier.Validated]).
 * @param binary whether to return the object as a binary hex blob.
 * @param index the object's index (ledger entry ID) as a 64-char hex string.
 * @param accountRoot the account address to look up the AccountRoot object for.
 * @param check the check ID to look up.
 * @param paymentChannel the payment channel ID to look up.
 * @param nftPage the NFT page ID to look up.
 * @param offer the offer descriptor (JSON element with account and seq fields).
 * @param rippleState the RippleState descriptor (JSON element with accounts and currency).
 * @param escrow the escrow descriptor (JSON element with owner and seq fields).
 * @param depositPreauth the deposit preauth descriptor.
 * @param ticket the ticket descriptor.
 * @return [XrplResult] containing [LedgerEntryResult] or a categorized failure.
 */
public suspend fun XrplClient.ledgerEntry(
    ledger: LedgerSpecifier = LedgerSpecifier.Validated,
    binary: Boolean = false,
    index: String? = null,
    accountRoot: String? = null,
    check: String? = null,
    paymentChannel: String? = null,
    nftPage: String? = null,
    offer: JsonElement? = null,
    rippleState: JsonElement? = null,
    escrow: JsonElement? = null,
    depositPreauth: JsonElement? = null,
    ticket: JsonElement? = null,
): XrplResult<LedgerEntryResult> {
    val (paramKey, paramValue) = ledger.toParamPair()
    val request =
        LedgerEntryRequest(
            ledgerIndex = if (paramKey == "ledger_index") paramValue else null,
            ledgerHash = if (paramKey == "ledger_hash") paramValue else null,
            binary = binary,
            index = index,
            account_root = accountRoot,
            check = check,
            payment_channel = paymentChannel,
            nft_page = nftPage,
            offer = offer,
            ripple_state = rippleState,
            escrow = escrow,
            deposit_preauth = depositPreauth,
            ticket = ticket,
        )
    return executeRpc(
        method = "ledger_entry",
        request = request,
        requestSerializer = LedgerEntryRequest.serializer(),
        responseDeserializer = LedgerEntryResponseDto.serializer(),
    ) { dto ->
        LedgerEntryResult(
            index = dto.index,
            ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it) },
            ledgerHash = dto.ledgerHash?.takeIf { it.length == 64 }?.let { Hash256(it) },
            node = dto.node,
            nodeBinary = dto.nodeBinary,
            validated = dto.validated,
        )
    }
}
