package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// AccountInfo
@Serializable
internal data class AccountInfoRequest(
    val account: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class AccountInfoResponse(
    @SerialName("account_data") val accountData: AccountData,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
    val validated: Boolean? = null,
)

@Serializable
internal data class AccountData(
    @SerialName("Account") val account: String,
    @SerialName("Balance") val balance: String,
    @SerialName("Sequence") val sequence: Long,
    @SerialName("OwnerCount") val ownerCount: Long,
    @SerialName("Flags") val flags: Long = 0,
    @SerialName("PreviousTxnID") val previousTxnId: String? = null,
    @SerialName("PreviousTxnLgrSeq") val previousTxnLgrSeq: Long? = null,
    val domain: String? = null,
    @SerialName("RegularKey") val regularKey: String? = null,
    @SerialName("signer_lists") val signerLists: JsonElement? = null,
)

// AccountLines
@Serializable
internal data class AccountLinesRequest(
    val account: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val marker: JsonElement? = null,
    val limit: Int? = null,
)

@Serializable
internal data class AccountLinesResponse(
    val account: String,
    val lines: List<TrustLineDto> = emptyList(),
    val marker: JsonElement? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class TrustLineDto(
    val account: String,
    val balance: String,
    val currency: String,
    val limit: String,
    @SerialName("limit_peer") val limitPeer: String = "0",
    @SerialName("no_ripple") val noRipple: Boolean? = null,
    @SerialName("no_ripple_peer") val noRipplePeer: Boolean? = null,
)

// AccountObjects
@Serializable
internal data class AccountObjectsRequest(
    val account: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val type: String? = null,
    val marker: JsonElement? = null,
    val limit: Int? = null,
)

@Serializable
internal data class AccountObjectsResponse(
    val account: String,
    @SerialName("account_objects") val accountObjects: List<JsonElement> = emptyList(),
    val marker: JsonElement? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

// AccountOffers
@Serializable
internal data class AccountOffersRequest(
    val account: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val marker: JsonElement? = null,
    val limit: Int? = null,
)

@Serializable
internal data class AccountOffersResponse(
    val account: String,
    val offers: List<AccountOfferDto> = emptyList(),
    val marker: JsonElement? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class AccountOfferDto(
    val flags: Long = 0,
    val seq: Long,
    @SerialName("taker_gets") val takerGets: JsonElement,
    @SerialName("taker_pays") val takerPays: JsonElement,
)

// AccountTx
@Serializable
internal data class AccountTxRequest(
    val account: String,
    @SerialName("ledger_index_min") val ledgerIndexMin: Long = -1,
    @SerialName("ledger_index_max") val ledgerIndexMax: Long = -1,
    val marker: JsonElement? = null,
    val limit: Int? = null,
    val forward: Boolean? = null,
)

@Serializable
internal data class AccountTxResponse(
    val account: String,
    val transactions: List<AccountTxEntry> = emptyList(),
    val marker: JsonElement? = null,
    @SerialName("ledger_index_min") val ledgerIndexMin: Long? = null,
    @SerialName("ledger_index_max") val ledgerIndexMax: Long? = null,
)

@Serializable
internal data class AccountTxEntry(
    val tx: JsonElement? = null,
    val meta: JsonElement? = null,
    val validated: Boolean? = null,
)

// AccountChannels
@Serializable
internal data class AccountChannelsRequest(
    val account: String,
    @SerialName("destination_account") val destinationAccount: String? = null,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val marker: JsonElement? = null,
    val limit: Int? = null,
)

@Serializable
internal data class AccountChannelsResponse(
    val account: String,
    val channels: List<ChannelDto> = emptyList(),
    val marker: JsonElement? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class ChannelDto(
    @SerialName("channel_id") val channelId: String,
    val account: String,
    @SerialName("destination_account") val destinationAccount: String,
    val amount: String,
    val balance: String,
    @SerialName("settle_delay") val settleDelay: Long,
    @SerialName("public_key") val publicKey: String? = null,
)

// AccountCurrencies
@Serializable
internal data class AccountCurrenciesRequest(
    val account: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class AccountCurrenciesResponse(
    @SerialName("receive_currencies") val receiveCurrencies: List<String> = emptyList(),
    @SerialName("send_currencies") val sendCurrencies: List<String> = emptyList(),
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

// AccountNfts
@Serializable
internal data class AccountNftsRequest(
    val account: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val marker: JsonElement? = null,
    val limit: Int? = null,
)

@Serializable
internal data class AccountNftsResponse(
    val account: String,
    @SerialName("account_nfts") val accountNfts: List<NftDto> = emptyList(),
    val marker: JsonElement? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class NftDto(
    @SerialName("Flags") val flags: Long = 0,
    @SerialName("Issuer") val issuer: String,
    @SerialName("NFTokenID") val nftokenId: String,
    @SerialName("NFTokenTaxon") val nftokenTaxon: Long,
    val nft_serial: Long? = null,
    val uri: String? = null,
)

// GatewayBalances
@Serializable
internal data class GatewayBalancesRequest(
    val account: String,
    @SerialName("hotwallet") val hotWallet: List<String>? = null,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class GatewayBalancesResponse(
    val account: String,
    val obligations: Map<String, String>? = null,
    val balances: Map<String, List<BalanceDto>>? = null,
    val assets: Map<String, List<BalanceDto>>? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class BalanceDto(
    val currency: String,
    val value: String,
)

// NorippleCheck
@Serializable
internal data class NorippleCheckRequest(
    val account: String,
    val role: String,
    val transactions: Boolean = false,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class NorippleCheckResponse(
    val problems: List<String> = emptyList(),
    val transactions: List<JsonElement>? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)
