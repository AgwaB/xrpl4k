package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// NftBuyOffers / NftSellOffers

@Serializable
internal data class NftOffersRequest(
    @SerialName("nft_id") val nftId: String,
    val limit: Int? = null,
    val marker: JsonElement? = null,
)

@Serializable
internal data class NftOffersResponseDto(
    @SerialName("nft_id") val nftId: String,
    val offers: List<NftOfferDto> = emptyList(),
    val marker: JsonElement? = null,
)

@Serializable
internal data class NftOfferDto(
    val amount: JsonElement,
    val flags: Long = 0,
    @SerialName("nft_offer_index") val nftOfferIndex: String? = null,
    val owner: String,
    val destination: String? = null,
    val expiration: Long? = null,
)

// NftInfo

@Serializable
internal data class NftInfoRequest(
    @SerialName("nft_id") val nftId: String,
)

@Serializable
internal data class NftInfoResponseDto(
    @SerialName("nft_id") val nftId: String,
    val owner: String,
    val flags: Long = 0,
    val uri: String? = null,
    @SerialName("is_burned") val isBurned: Boolean = false,
    @SerialName("nft_taxon") val nftTaxon: Long? = null,
    @SerialName("nft_serial") val nftSerial: Long? = null,
    val issuer: String? = null,
    @SerialName("transfer_fee") val transferFee: Long? = null,
)

// NftHistory

@Serializable
internal data class NftHistoryRequest(
    @SerialName("nft_id") val nftId: String,
    val limit: Int? = null,
    val marker: JsonElement? = null,
)

@Serializable
internal data class NftHistoryResponseDto(
    @SerialName("nft_id") val nftId: String,
    val transactions: List<NftHistoryEntryDto> = emptyList(),
    val marker: JsonElement? = null,
)

@Serializable
internal data class NftHistoryEntryDto(
    val tx: JsonElement? = null,
    val meta: JsonElement? = null,
    val validated: Boolean? = null,
)

// NftsByIssuer (Clio-only)

@Serializable
internal data class NftsByIssuerRequest(
    val issuer: String,
    @SerialName("nft_taxon") val nftTaxon: Long? = null,
    val marker: JsonElement? = null,
    val limit: Int? = null,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class NftsByIssuerResponseDto(
    val issuer: String,
    val nfts: List<NftsByIssuerTokenDto> = emptyList(),
    val marker: JsonElement? = null,
    val limit: Int? = null,
    @SerialName("nft_taxon") val nftTaxon: Long? = null,
)

@Serializable
internal data class NftsByIssuerTokenDto(
    @SerialName("nft_id") val nftId: String,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
    val owner: String? = null,
    @SerialName("is_burned") val isBurned: Boolean = false,
    val flags: Long = 0,
    @SerialName("transfer_fee") val transferFee: Long? = null,
    val issuer: String? = null,
    @SerialName("nft_taxon") val nftTaxon: Long? = null,
    @SerialName("nft_serial") val nftSerial: Long? = null,
    val uri: String? = null,
)
