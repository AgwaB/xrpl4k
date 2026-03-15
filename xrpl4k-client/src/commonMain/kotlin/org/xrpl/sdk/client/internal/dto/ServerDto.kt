package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.internal.RpcRequest

// ---------------------------------------------------------------------------
// Request DTOs
// ---------------------------------------------------------------------------

@Serializable
internal data class ManifestRequest(
    @SerialName("public_key") val publicKey: String,
) : RpcRequest

@Serializable
internal data class FeatureRequest(
    val feature: String? = null,
) : RpcRequest

// ---------------------------------------------------------------------------
// Response DTOs
// ---------------------------------------------------------------------------

@Serializable
internal data class ServerInfoStateDto(
    @SerialName("build_version") val buildVersion: String? = null,
    @SerialName("complete_ledgers") val completeLedgers: String? = null,
    @SerialName("hostid") val hostId: String? = null,
    @SerialName("io_latency_ms") val ioLatencyMs: Long? = null,
    @SerialName("jq_trans_overflow") val jqTransOverflow: String? = null,
    @SerialName("last_close") val lastClose: LastCloseDto? = null,
    @SerialName("load_factor") val loadFactor: Double? = null,
    @SerialName("peers") val peers: Int? = null,
    @SerialName("pubkey_node") val pubkeyNode: String? = null,
    @SerialName("server_state") val serverState: String? = null,
    @SerialName("server_state_duration_us") val serverStateDurationUs: Long? = null,
    @SerialName("uptime") val uptime: Long? = null,
    @SerialName("validated_ledger") val validatedLedger: ValidatedLedgerDto? = null,
    @SerialName("validation_quorum") val validationQuorum: Int? = null,
    @SerialName("validator_list_expires") val validatorListExpires: String? = null,
    @SerialName("network_id") val networkId: UInt? = null,
)

@Serializable
internal data class LastCloseDto(
    @SerialName("converge_time") val convergeTime: Double? = null,
    @SerialName("proposers") val proposers: Int? = null,
)

@Serializable
internal data class ValidatedLedgerDto(
    @SerialName("age") val age: Long? = null,
    @SerialName("base_fee_xrp") val baseFeeXrp: Double? = null,
    @SerialName("hash") val hash: String? = null,
    @SerialName("reserve_base_xrp") val reserveBaseXrp: Double? = null,
    @SerialName("reserve_inc_xrp") val reserveIncXrp: Double? = null,
    @SerialName("seq") val seq: UInt? = null,
)

@Serializable
internal data class ServerInfoResponseDto(
    val info: ServerInfoStateDto? = null,
)

@Serializable
internal data class ServerStateResponseDto(
    val state: ServerInfoStateDto? = null,
)

@Serializable
internal data class FeeDropsDto(
    @SerialName("base_fee") val baseFee: String? = null,
    @SerialName("median_fee") val medianFee: String? = null,
    @SerialName("minimum_fee") val minimumFee: String? = null,
    @SerialName("open_ledger_fee") val openLedgerFee: String? = null,
)

@Serializable
internal data class FeeResponseDto(
    @SerialName("current_ledger_size") val currentLedgerSize: String? = null,
    @SerialName("current_queue_size") val currentQueueSize: String? = null,
    val drops: FeeDropsDto? = null,
    @SerialName("expected_ledger_size") val expectedLedgerSize: String? = null,
    @SerialName("ledger_current_index") val ledgerCurrentIndex: UInt? = null,
    val levels: JsonElement? = null,
    @SerialName("max_queue_size") val maxQueueSize: String? = null,
)

@Serializable
internal data class ManifestResponseDto(
    @SerialName("manifest") val manifest: String? = null,
    @SerialName("requested") val requested: String? = null,
)

@Serializable
internal data class FeatureEntryDto(
    val enabled: Boolean = false,
    val name: String? = null,
    val supported: Boolean = false,
    val vetoed: kotlinx.serialization.json.JsonElement? = null,
)

@Serializable
internal data class FeatureResponseDto(
    val features: Map<String, FeatureEntryDto> = emptyMap(),
)

@Serializable
internal data class VersionInfoDto(
    val first: String? = null,
    val good: String? = null,
    val last: String? = null,
)

@Serializable
internal data class VersionResponseDto(
    val version: VersionInfoDto? = null,
)

@Serializable
internal data class ServerDefinitionsResponseDto(
    val fields: JsonElement? = null,
    @SerialName("ledger_entry_types") val ledgerEntryTypes: JsonElement? = null,
    @SerialName("transaction_results") val transactionResults: JsonElement? = null,
    @SerialName("transaction_types") val transactionTypes: JsonElement? = null,
    val types: JsonElement? = null,
)

// GetAggregatePrice

@Serializable
internal data class GetAggregatePriceRequest(
    @SerialName("base_asset") val baseAsset: String,
    @SerialName("quote_asset") val quoteAsset: String,
    val oracles: List<OracleSpecDto>,
    val trim: Int? = null,
    @SerialName("trim_threshold") val trimThreshold: Int? = null,
)

@Serializable
internal data class OracleSpecDto(
    val account: String,
    @SerialName("oracle_document_id") val oracleDocumentId: Long,
)

@Serializable
internal data class GetAggregatePriceResponseDto(
    @SerialName("entire_set") val entireSet: AggregatePriceSetDto? = null,
    @SerialName("trimmed_set") val trimmedSet: AggregatePriceSetDto? = null,
    val median: String? = null,
    val time: Long? = null,
    @SerialName("ledger_current_index") val ledgerCurrentIndex: Long? = null,
    val validated: Boolean? = null,
)

@Serializable
internal data class AggregatePriceSetDto(
    val mean: String? = null,
    val size: Int? = null,
    @SerialName("standard_deviation") val standardDeviation: String? = null,
)

// VaultInfo

@Serializable
internal data class VaultInfoRequest(
    @SerialName("vault_id") val vaultId: String? = null,
    val owner: String? = null,
    val seq: Long? = null,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class VaultInfoResponseDto(
    val vault: JsonElement? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: Long? = null,
    val validated: Boolean? = null,
)
