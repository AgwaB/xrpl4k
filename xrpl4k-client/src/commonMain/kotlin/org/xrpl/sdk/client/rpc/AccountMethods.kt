package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.AccountChannelsRequest
import org.xrpl.sdk.client.internal.dto.AccountChannelsResponse
import org.xrpl.sdk.client.internal.dto.AccountCurrenciesRequest
import org.xrpl.sdk.client.internal.dto.AccountCurrenciesResponse
import org.xrpl.sdk.client.internal.dto.AccountInfoRequest
import org.xrpl.sdk.client.internal.dto.AccountInfoResponse
import org.xrpl.sdk.client.internal.dto.AccountLinesRequest
import org.xrpl.sdk.client.internal.dto.AccountLinesResponse
import org.xrpl.sdk.client.internal.dto.AccountNftsRequest
import org.xrpl.sdk.client.internal.dto.AccountNftsResponse
import org.xrpl.sdk.client.internal.dto.AccountObjectsRequest
import org.xrpl.sdk.client.internal.dto.AccountObjectsResponse
import org.xrpl.sdk.client.internal.dto.AccountOffersRequest
import org.xrpl.sdk.client.internal.dto.AccountOffersResponse
import org.xrpl.sdk.client.internal.dto.AccountTxRequest
import org.xrpl.sdk.client.internal.dto.AccountTxResponse
import org.xrpl.sdk.client.internal.dto.DepositAuthorizedRequest
import org.xrpl.sdk.client.internal.dto.DepositAuthorizedResponse
import org.xrpl.sdk.client.internal.dto.GatewayBalancesRequest
import org.xrpl.sdk.client.internal.dto.GatewayBalancesResponse
import org.xrpl.sdk.client.internal.dto.NorippleCheckRequest
import org.xrpl.sdk.client.internal.dto.NorippleCheckResponse
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.AccountChannelsResult
import org.xrpl.sdk.client.model.AccountCurrenciesResult
import org.xrpl.sdk.client.model.AccountInfo
import org.xrpl.sdk.client.model.AccountLinesResult
import org.xrpl.sdk.client.model.AccountNftsResult
import org.xrpl.sdk.client.model.AccountObjectsResult
import org.xrpl.sdk.client.model.AccountOffer
import org.xrpl.sdk.client.model.AccountOffersResult
import org.xrpl.sdk.client.model.AccountTxEntry
import org.xrpl.sdk.client.model.AccountTxResult
import org.xrpl.sdk.client.model.DepositAuthorizedResult
import org.xrpl.sdk.client.model.GatewayBalancesResult
import org.xrpl.sdk.client.model.IssuedCurrencyBalance
import org.xrpl.sdk.client.model.LedgerSpecifier
import org.xrpl.sdk.client.model.Nft
import org.xrpl.sdk.client.model.NorippleCheckResult
import org.xrpl.sdk.client.model.PaymentChannel
import org.xrpl.sdk.client.model.TrustLine
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Retrieves account information for the given address.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @return [XrplResult] containing [AccountInfo] on success.
 */
public suspend fun XrplClient.accountInfo(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<AccountInfo> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_info",
        request = AccountInfoRequest(account = account.value, ledgerIndex = ledgerIndex, ledgerHash = ledgerHash),
        requestSerializer = AccountInfoRequest.serializer(),
        responseDeserializer = AccountInfoResponse.serializer(),
    ) { resp ->
        val data = resp.accountData
        AccountInfo(
            account = Address(data.account),
            balance = XrpDrops(data.balance.toLong()),
            sequence = data.sequence.toUInt(),
            ownerCount = data.ownerCount.toUInt(),
            flags = data.flags.toUInt(),
            previousAffectingTransactionId = data.previousTxnId?.let { TxHash(it) },
            previousAffectingTransactionLedgerSequence = data.previousTxnLgrSeq?.toUInt(),
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
            domain = data.domain,
            regularKey = data.regularKey?.let { Address(it) },
            signerLists = data.signerLists,
        )
    }
}

/**
 * Retrieves the trust lines (IOUs) associated with an account.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @return [XrplResult] containing [AccountLinesResult] on success.
 */
public suspend fun XrplClient.accountLines(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    marker: JsonElement? = null,
    limit: Int? = null,
): XrplResult<AccountLinesResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_lines",
        request =
            AccountLinesRequest(
                account = account.value,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
                marker = marker,
                limit = limit,
            ),
        requestSerializer = AccountLinesRequest.serializer(),
        responseDeserializer = AccountLinesResponse.serializer(),
    ) { resp ->
        AccountLinesResult(
            account = Address(resp.account),
            lines =
                resp.lines.map { line ->
                    TrustLine(
                        account = Address(line.account),
                        balance = line.balance,
                        currency = line.currency,
                        limit = line.limit,
                        limitPeer = line.limitPeer,
                        noRipple = line.noRipple,
                        noRipplePeer = line.noRipplePeer,
                    )
                },
            marker = resp.marker,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Valid object types for the `account_objects` RPC method's `type` filter parameter.
 *
 * Using this enum instead of raw strings prevents silent failures from typos.
 */
public enum class AccountObjectType(public val value: String) {
    Check("check"),
    DepositPreauth("deposit_preauth"),
    Escrow("escrow"),
    NftOffer("nft_offer"),
    Offer("offer"),
    PaymentChannel("payment_channel"),
    SignerList("signer_list"),
    State("state"),
    Ticket("ticket"),
}

/**
 * Retrieves ledger objects owned by an account.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @param type Optional filter for object type using [AccountObjectType] for type-safety.
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @return [XrplResult] containing [AccountObjectsResult] on success.
 */
public suspend fun XrplClient.accountObjects(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    type: AccountObjectType?,
    marker: JsonElement? = null,
    limit: Int? = null,
): XrplResult<AccountObjectsResult> =
    accountObjects(
        account = account,
        ledgerSpecifier = ledgerSpecifier,
        type = type?.value,
        marker = marker,
        limit = limit,
    )

/**
 * Retrieves ledger objects owned by an account.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @param type Optional filter for object type (e.g., "offer", "escrow", "payment_channel").
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @return [XrplResult] containing [AccountObjectsResult] on success.
 */
public suspend fun XrplClient.accountObjects(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    type: String? = null,
    marker: JsonElement? = null,
    limit: Int? = null,
): XrplResult<AccountObjectsResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_objects",
        request =
            AccountObjectsRequest(
                account = account.value,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
                type = type,
                marker = marker,
                limit = limit,
            ),
        requestSerializer = AccountObjectsRequest.serializer(),
        responseDeserializer = AccountObjectsResponse.serializer(),
    ) { resp ->
        AccountObjectsResult(
            account = Address(resp.account),
            accountObjects = resp.accountObjects,
            marker = resp.marker,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Retrieves open offers placed by an account.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @return [XrplResult] containing [AccountOffersResult] on success.
 */
public suspend fun XrplClient.accountOffers(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    marker: JsonElement? = null,
    limit: Int? = null,
): XrplResult<AccountOffersResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_offers",
        request =
            AccountOffersRequest(
                account = account.value,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
                marker = marker,
                limit = limit,
            ),
        requestSerializer = AccountOffersRequest.serializer(),
        responseDeserializer = AccountOffersResponse.serializer(),
    ) { resp ->
        AccountOffersResult(
            account = Address(resp.account),
            offers =
                resp.offers.map { offer ->
                    AccountOffer(
                        flags = offer.flags.toUInt(),
                        seq = offer.seq.toUInt(),
                        takerGets = offer.takerGets,
                        takerPays = offer.takerPays,
                    )
                },
            marker = resp.marker,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Retrieves transactions that affected an account.
 *
 * @param account The account address to query.
 * @param ledgerIndexMin Minimum ledger index to search (inclusive). Use -1 for earliest available.
 * @param ledgerIndexMax Maximum ledger index to search (inclusive). Use -1 for latest available.
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @param forward If true, returns oldest transactions first.
 * @return [XrplResult] containing [AccountTxResult] on success.
 */
public suspend fun XrplClient.accountTx(
    account: Address,
    ledgerIndexMin: Long = -1,
    ledgerIndexMax: Long = -1,
    marker: JsonElement? = null,
    limit: Int? = null,
    forward: Boolean? = null,
): XrplResult<AccountTxResult> =
    executeRpc(
        method = "account_tx",
        request =
            AccountTxRequest(
                account = account.value,
                ledgerIndexMin = ledgerIndexMin,
                ledgerIndexMax = ledgerIndexMax,
                marker = marker,
                limit = limit,
                forward = forward,
            ),
        requestSerializer = AccountTxRequest.serializer(),
        responseDeserializer = AccountTxResponse.serializer(),
    ) { resp ->
        AccountTxResult(
            account = Address(resp.account),
            transactions =
                resp.transactions.map { entry ->
                    AccountTxEntry(
                        tx = entry.tx,
                        meta = entry.meta,
                        validated = entry.validated,
                    )
                },
            marker = resp.marker,
            ledgerIndexMin = resp.ledgerIndexMin?.let { LedgerIndex(it.toUInt()) },
            ledgerIndexMax = resp.ledgerIndexMax?.let { LedgerIndex(it.toUInt()) },
        )
    }

/**
 * Retrieves payment channels where an account is the source.
 *
 * @param account The source account address.
 * @param destinationAccount Optional filter for a specific destination account.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @return [XrplResult] containing [AccountChannelsResult] on success.
 */
public suspend fun XrplClient.accountChannels(
    account: Address,
    destinationAccount: Address? = null,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    marker: JsonElement? = null,
    limit: Int? = null,
): XrplResult<AccountChannelsResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_channels",
        request =
            AccountChannelsRequest(
                account = account.value,
                destinationAccount = destinationAccount?.value,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
                marker = marker,
                limit = limit,
            ),
        requestSerializer = AccountChannelsRequest.serializer(),
        responseDeserializer = AccountChannelsResponse.serializer(),
    ) { resp ->
        AccountChannelsResult(
            account = Address(resp.account),
            channels =
                resp.channels.map { ch ->
                    PaymentChannel(
                        channelId = ch.channelId,
                        account = Address(ch.account),
                        destinationAccount = Address(ch.destinationAccount),
                        amount = XrpDrops(ch.amount.toLong()),
                        balance = XrpDrops(ch.balance.toLong()),
                        settleDelay = ch.settleDelay.toUInt(),
                        publicKey = ch.publicKey,
                    )
                },
            marker = resp.marker,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Retrieves the currencies an account can send and receive.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @return [XrplResult] containing [AccountCurrenciesResult] on success.
 */
public suspend fun XrplClient.accountCurrencies(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<AccountCurrenciesResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_currencies",
        request = AccountCurrenciesRequest(account = account.value, ledgerIndex = ledgerIndex, ledgerHash = ledgerHash),
        requestSerializer = AccountCurrenciesRequest.serializer(),
        responseDeserializer = AccountCurrenciesResponse.serializer(),
    ) { resp ->
        AccountCurrenciesResult(
            receiveCurrencies = resp.receiveCurrencies,
            sendCurrencies = resp.sendCurrencies,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Retrieves NFTs owned by an account.
 *
 * @param account The account address to query.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @param marker Pagination marker from a previous response.
 * @param limit Maximum number of results to return.
 * @return [XrplResult] containing [AccountNftsResult] on success.
 */
public suspend fun XrplClient.accountNfts(
    account: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
    marker: JsonElement? = null,
    limit: Int? = null,
): XrplResult<AccountNftsResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "account_nfts",
        request =
            AccountNftsRequest(
                account = account.value,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
                marker = marker,
                limit = limit,
            ),
        requestSerializer = AccountNftsRequest.serializer(),
        responseDeserializer = AccountNftsResponse.serializer(),
    ) { resp ->
        AccountNftsResult(
            account = Address(resp.account),
            accountNfts =
                resp.accountNfts.map { nft ->
                    Nft(
                        flags = nft.flags.toUInt(),
                        issuer = Address(nft.issuer),
                        nftokenId = nft.nftokenId,
                        nftokenTaxon = nft.nftokenTaxon.toULong(),
                        nftSerial = nft.nft_serial?.toULong(),
                        uri = nft.uri,
                    )
                },
            marker = resp.marker,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Retrieves the balances issued by a gateway account.
 *
 * @param account The gateway account address.
 * @param hotWallets Optional list of hot wallet addresses to separate from obligations.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @return [XrplResult] containing [GatewayBalancesResult] on success.
 */
public suspend fun XrplClient.gatewayBalances(
    account: Address,
    hotWallets: List<Address>? = null,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<GatewayBalancesResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "gateway_balances",
        request =
            GatewayBalancesRequest(
                account = account.value,
                hotWallet = hotWallets?.map { it.value },
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
            ),
        requestSerializer = GatewayBalancesRequest.serializer(),
        responseDeserializer = GatewayBalancesResponse.serializer(),
    ) { resp ->
        GatewayBalancesResult(
            account = Address(resp.account),
            obligations = resp.obligations ?: emptyMap(),
            balances =
                resp.balances?.mapValues { (_, list) ->
                    list.map { b -> IssuedCurrencyBalance(currency = b.currency, value = b.value) }
                } ?: emptyMap(),
            assets =
                resp.assets?.mapValues { (_, list) ->
                    list.map { b -> IssuedCurrencyBalance(currency = b.currency, value = b.value) }
                } ?: emptyMap(),
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Checks whether an account has the No Ripple flag configured correctly for its trust lines.
 *
 * @param account The account address to check.
 * @param role The account role: "gateway" or "user".
 * @param includeTransactions If true, include suggested fix transactions in the result.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @return [XrplResult] containing [NorippleCheckResult] on success.
 */
public suspend fun XrplClient.norippleCheck(
    account: Address,
    role: String,
    includeTransactions: Boolean = false,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<NorippleCheckResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "noripple_check",
        request =
            NorippleCheckRequest(
                account = account.value,
                role = role,
                transactions = includeTransactions,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
            ),
        requestSerializer = NorippleCheckRequest.serializer(),
        responseDeserializer = NorippleCheckResponse.serializer(),
    ) { resp ->
        NorippleCheckResult(
            problems = resp.problems,
            transactions = resp.transactions,
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Checks whether one account is authorized to send payments directly to another.
 *
 * @param sourceAccount The sender of a possible payment.
 * @param destinationAccount The recipient of a possible payment.
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @return [XrplResult] containing [DepositAuthorizedResult] on success.
 */
public suspend fun XrplClient.depositAuthorized(
    sourceAccount: Address,
    destinationAccount: Address,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<DepositAuthorizedResult> {
    val (ledgerIndex, ledgerHash) = ledgerSpecifier.toLedgerParams()
    return executeRpc(
        method = "deposit_authorized",
        request =
            DepositAuthorizedRequest(
                sourceAccount = sourceAccount.value,
                destinationAccount = destinationAccount.value,
                ledgerIndex = ledgerIndex,
                ledgerHash = ledgerHash,
            ),
        requestSerializer = DepositAuthorizedRequest.serializer(),
        responseDeserializer = DepositAuthorizedResponse.serializer(),
    ) { resp ->
        DepositAuthorizedResult(
            depositAuthorized = resp.depositAuthorized,
            sourceAccount = Address(resp.sourceAccount),
            destinationAccount = Address(resp.destinationAccount),
            ledgerIndex = resp.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
        )
    }
}

/**
 * Returns a (ledgerIndex, ledgerHash) pair for building RPC request DTOs.
 *
 * For [LedgerSpecifier.Hash], only `ledger_hash` is set; `ledger_index` is null so it is
 * omitted from the serialized request (XrplJson uses `encodeDefaults = false`).
 */
private fun LedgerSpecifier.toLedgerParams(): Pair<String?, String?> =
    when (this) {
        is LedgerSpecifier.Validated -> "validated" to null
        is LedgerSpecifier.Current -> "current" to null
        is LedgerSpecifier.Closed -> "closed" to null
        is LedgerSpecifier.Index -> index.toString() to null
        is LedgerSpecifier.Hash -> null to hash.value
    }
