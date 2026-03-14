package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.internal.RpcRequest

// ---------------------------------------------------------------------------
// Request DTOs
// ---------------------------------------------------------------------------

@Serializable
internal data class SubmitRequest(
    @SerialName("tx_blob") val txBlob: String,
) : RpcRequest

@Serializable
internal data class SubmitMultisignedRequest(
    val tx_json: JsonElement,
) : RpcRequest

@Serializable
internal data class TxRequest(
    val transaction: String,
    val binary: Boolean = false,
    @SerialName("min_ledger") val minLedger: UInt? = null,
    @SerialName("max_ledger") val maxLedger: UInt? = null,
) : RpcRequest

@Serializable
internal data class TransactionEntryRequest(
    @SerialName("tx_hash") val txHash: String,
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
) : RpcRequest

@Serializable
internal data class SimulateRequest(
    val tx_json: JsonElement? = null,
    @SerialName("tx_blob") val txBlob: String? = null,
    val binary: Boolean = false,
) : RpcRequest

// ---------------------------------------------------------------------------
// Response DTOs
// ---------------------------------------------------------------------------

@Serializable
internal data class SubmitResponseDto(
    @SerialName("engine_result") val engineResult: String = "",
    @SerialName("engine_result_code") val engineResultCode: Int = 0,
    @SerialName("engine_result_message") val engineResultMessage: String = "",
    @SerialName("tx_blob") val txBlob: String? = null,
    @SerialName("tx_json") val txJson: JsonElement? = null,
    val accepted: Boolean = false,
    @SerialName("account_sequence_available") val accountSequenceAvailable: UInt? = null,
    @SerialName("account_sequence_next") val accountSequenceNext: UInt? = null,
    @SerialName("applied") val applied: Boolean = false,
    @SerialName("broadcast") val broadcast: Boolean = false,
    @SerialName("kept") val kept: Boolean = false,
    @SerialName("queued") val queued: Boolean = false,
    @SerialName("open_ledger_cost") val openLedgerCost: String? = null,
    @SerialName("validated_ledger_index") val validatedLedgerIndex: UInt? = null,
)

@Serializable
internal data class TxResponseDto(
    val hash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: UInt? = null,
    val meta: JsonElement? = null,
    val validated: Boolean = false,
    @SerialName("tx_json") val txJson: JsonElement? = null,
    @SerialName("close_time_iso") val closeTimeIso: String? = null,
    @SerialName("inLedger") val inLedger: UInt? = null,
    @SerialName("engine_result") val engineResult: String? = null,
    @SerialName("engine_result_code") val engineResultCode: Int? = null,
)

@Serializable
internal data class TransactionEntryResponseDto(
    @SerialName("tx_json") val txJson: JsonElement? = null,
    val metadata: JsonElement? = null,
    @SerialName("ledger_index") val ledgerIndex: UInt? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
)

@Serializable
internal data class SimulateResponseDto(
    @SerialName("engine_result") val engineResult: String = "",
    @SerialName("engine_result_code") val engineResultCode: Int = 0,
    @SerialName("engine_result_message") val engineResultMessage: String = "",
    @SerialName("tx_json") val txJson: JsonElement? = null,
    val meta: JsonElement? = null,
)
