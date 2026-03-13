package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// AmmInfo

@Serializable
internal data class AmmInfoRequest(
    val asset: JsonElement,
    val asset2: JsonElement? = null,
)

@Serializable
internal data class AmmInfoResponseDto(
    val amm: AmmDto? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
)

@Serializable
internal data class AmmDto(
    val account: String? = null,
    val amount: JsonElement? = null,
    val amount2: JsonElement? = null,
    @SerialName("trading_fee") val tradingFee: Long? = null,
    @SerialName("lp_token") val lpToken: JsonElement? = null,
    @SerialName("auction_slot") val auctionSlot: JsonElement? = null,
    @SerialName("vote_slots") val voteSlots: List<JsonElement>? = null,
)
