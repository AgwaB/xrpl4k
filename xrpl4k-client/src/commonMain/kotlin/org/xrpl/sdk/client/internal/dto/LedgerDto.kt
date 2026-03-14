package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.internal.RpcRequest

// ---------------------------------------------------------------------------
// Request DTOs
// ---------------------------------------------------------------------------

@Serializable
internal data class LedgerRequest(
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val transactions: Boolean = false,
    val expand: Boolean = false,
    @SerialName("owner_funds") val ownerFunds: Boolean = false,
    val binary: Boolean = false,
) : RpcRequest

@Serializable
internal data class LedgerDataRequest(
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val binary: Boolean = false,
    val limit: Int? = null,
    val marker: JsonElement? = null,
) : RpcRequest

@Serializable
internal data class LedgerEntryRequest(
    @SerialName("ledger_index") val ledgerIndex: String? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val binary: Boolean = false,
    val index: String? = null,
    val account_root: String? = null,
    val offer: JsonElement? = null,
    val ripple_state: JsonElement? = null,
    val check: String? = null,
    val escrow: JsonElement? = null,
    val payment_channel: String? = null,
    val deposit_preauth: JsonElement? = null,
    val ticket: JsonElement? = null,
    val nft_page: String? = null,
) : RpcRequest

// ---------------------------------------------------------------------------
// Response DTOs
// ---------------------------------------------------------------------------

@Serializable
internal data class LedgerInfoDto(
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: UInt? = null,
    @SerialName("account_hash") val accountHash: String? = null,
    @SerialName("transaction_hash") val transactionHash: String? = null,
    @SerialName("parent_hash") val parentHash: String? = null,
    @SerialName("parent_close_time") val parentCloseTime: Long? = null,
    @SerialName("close_time") val closeTime: Long? = null,
    @SerialName("close_time_human") val closeTimeHuman: String? = null,
    @SerialName("total_coins") val totalCoins: String? = null,
    val closed: Boolean = false,
    val transactions: List<JsonElement>? = null,
)

@Serializable
internal data class LedgerResponseDto(
    val ledger: LedgerInfoDto? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: UInt? = null,
    val validated: Boolean = false,
)

@Serializable
internal data class LedgerClosedResponseDto(
    @SerialName("ledger_hash") val ledgerHash: String,
    @SerialName("ledger_index") val ledgerIndex: UInt,
)

@Serializable
internal data class LedgerCurrentResponseDto(
    @SerialName("ledger_current_index") val ledgerCurrentIndex: UInt,
)

@Serializable
internal data class LedgerDataStateObjectDto(
    val index: String,
    @SerialName("ledger_entry_type") val ledgerEntryType: String? = null,
    val data: String? = null,
)

@Serializable
internal data class LedgerDataResponseDto(
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    @SerialName("ledger_index") val ledgerIndex: UInt? = null,
    val state: List<LedgerDataStateObjectDto> = emptyList(),
    val marker: JsonElement? = null,
)

@Serializable
internal data class LedgerEntryResponseDto(
    val index: String? = null,
    @SerialName("ledger_index") val ledgerIndex: UInt? = null,
    @SerialName("ledger_hash") val ledgerHash: String? = null,
    val node: JsonElement? = null,
    @SerialName("node_binary") val nodeBinary: String? = null,
    val validated: Boolean = false,
)
