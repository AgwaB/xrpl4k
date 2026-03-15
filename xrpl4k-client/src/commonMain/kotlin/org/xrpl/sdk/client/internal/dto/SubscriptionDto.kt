package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class SubscribeRequest(
    val streams: List<String>? = null,
    val accounts: List<String>? = null,
    @SerialName("accounts_proposed") val accountsProposed: List<String>? = null,
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
    @SerialName("accounts_proposed") val accountsProposed: List<String>? = null,
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
internal data class ValidationEventDto(
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
    val signature: String? = null,
    @SerialName("signing_time") val signingTime: Long? = null,
    @SerialName("validation_public_key") val validationPublicKey: String? = null,
    val flags: Long? = null,
    val full: Boolean? = null,
    val type: String? = null,
)

@Serializable
internal data class ConsensusEventDto(
    @SerialName("consensus") val consensus: String? = null,
    val type: String? = null,
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

@Serializable
internal data class PeerStatusEventDto(
    val action: String? = null,
    val date: String? = null,
    val address: String? = null,
    val type: String? = null,
)

@Serializable
internal data class ManifestEventDto(
    @SerialName("master_key") val masterKey: String? = null,
    @SerialName("signing_key") val signingKey: String? = null,
    @SerialName("seq") val seq: Long? = null,
    val type: String? = null,
)

@Serializable
internal data class ServerEventDto(
    @SerialName("server_status") val serverStatus: String? = null,
    @SerialName("load_factor") val loadFactor: Double? = null,
    @SerialName("base_fee") val baseFee: Long? = null,
    val type: String? = null,
)

@Serializable
internal data class PathFindCreateRequest(
    val subcommand: String = "create",
    @SerialName("source_account") val sourceAccount: String,
    @SerialName("destination_account") val destinationAccount: String,
    @SerialName("destination_amount") val destinationAmount: JsonElement,
)

@Serializable
internal data class PathFindCloseRequest(
    val subcommand: String = "close",
)

@Serializable
internal data class PathFindStreamEventDto(
    val alternatives: JsonElement? = null,
    @SerialName("source_account") val sourceAccount: String? = null,
    @SerialName("destination_account") val destinationAccount: String? = null,
    @SerialName("destination_amount") val destinationAmount: JsonElement? = null,
    @SerialName("full_reply") val fullReply: Boolean? = null,
    val type: String? = null,
    val id: Int? = null,
)
