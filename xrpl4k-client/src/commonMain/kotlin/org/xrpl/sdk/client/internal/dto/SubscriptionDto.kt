package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class SubscribeRequest(
    val streams: List<String>? = null,
    val accounts: List<String>? = null,
    val books: List<OrderBookSpec>? = null,
)

@Serializable
internal data class OrderBookSpec(
    @SerialName("taker_gets") val takerGets: CurrencySpec,
    @SerialName("taker_pays") val takerPays: CurrencySpec,
    @SerialName("taker") val taker: String? = null,
    @SerialName("snapshot") val snapshot: Boolean? = null,
    @SerialName("both") val both: Boolean? = null,
)

@Serializable
internal data class CurrencySpec(
    val currency: String,
    val issuer: String? = null,
)

@Serializable
internal data class SubscribeResponse(
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
    @SerialName("fee_base") val feeBase: Long? = null,
    @SerialName("fee_ref") val feeRef: Long? = null,
    @SerialName("validated_ledgers") val validatedLedgers: String? = null,
)

@Serializable
internal data class UnsubscribeRequest(
    val streams: List<String>? = null,
    val accounts: List<String>? = null,
)

// Subscription event DTOs

@Serializable
internal data class LedgerEventDto(
    @SerialName("ledger_index") val ledgerIndex: Long,
    @SerialName("ledger_hash") val ledgerHash: String,
    @SerialName("txn_count") val txnCount: Int? = null,
    @SerialName("ledger_time") val ledgerTime: Long? = null,
    @SerialName("fee_base") val feeBase: Long? = null,
    @SerialName("reserve_base") val reserveBase: Long? = null,
    @SerialName("reserve_inc") val reserveInc: Long? = null,
    val type: String? = null,
    val validated: Boolean? = null,
)

@Serializable
internal data class TransactionEventDto(
    val transaction: JsonElement? = null,
    val meta: JsonElement? = null,
    @SerialName("engine_result") val engineResult: String? = null,
    @SerialName("engine_result_code") val engineResultCode: Int? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
    val validated: Boolean? = null,
    val type: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)
