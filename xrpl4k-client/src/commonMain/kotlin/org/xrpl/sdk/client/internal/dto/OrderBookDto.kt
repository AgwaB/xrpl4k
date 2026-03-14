package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// BookOffers

@Serializable
internal data class BookOffersRequest(
    @SerialName("taker_gets") val takerGets: JsonElement,
    @SerialName("taker_pays") val takerPays: JsonElement,
    val taker: String? = null,
    val limit: Int? = null,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
)

@Serializable
internal data class BookOffersResponseDto(
    val offers: List<BookOfferDto> = emptyList(),
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class BookOfferDto(
    @SerialName("Account") val account: String,
    @SerialName("taker_gets") val takerGets: JsonElement,
    @SerialName("taker_pays") val takerPays: JsonElement,
    val quality: String? = null,
    @SerialName("Flags") val flags: Long = 0,
    @SerialName("Sequence") val sequence: Long? = null,
    @SerialName("owner_funds") val ownerFunds: String? = null,
)

// BookChanges

@Serializable
internal data class BookChangesRequest(
    @SerialName("ledger_index") val ledgerIndex: String? = null,
)

@Serializable
internal data class BookChangesResponseDto(
    val changes: List<BookChangeDto> = emptyList(),
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class BookChangeDto(
    @SerialName("currency_a") val currencyA: String,
    @SerialName("currency_b") val currencyB: String,
    @SerialName("volume_a") val volumeA: String? = null,
    @SerialName("volume_b") val volumeB: String? = null,
    @SerialName("high") val high: String? = null,
    @SerialName("low") val low: String? = null,
    @SerialName("open") val open: String? = null,
    @SerialName("close") val close: String? = null,
)
