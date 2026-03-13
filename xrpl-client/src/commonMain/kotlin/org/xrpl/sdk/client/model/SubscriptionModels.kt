package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash

/**
 * A ledger close event from a WebSocket subscription.
 */
public class LedgerEvent(
    public val ledgerIndex: LedgerIndex,
    public val ledgerHash: Hash256,
    public val txnCount: Int?,
    public val closeTime: Long?,
) {
    override fun equals(other: Any?): Boolean =
        other is LedgerEvent &&
            ledgerIndex == other.ledgerIndex &&
            ledgerHash == other.ledgerHash

    override fun hashCode(): Int {
        var result = ledgerIndex.hashCode()
        result = 31 * result + ledgerHash.hashCode()
        return result
    }

    override fun toString(): String =
        "LedgerEvent(ledgerIndex=$ledgerIndex, ledgerHash=$ledgerHash, txnCount=$txnCount)"
}

/**
 * A transaction event from a WebSocket subscription.
 */
public class TransactionEvent(
    public val hash: TxHash?,
    public val engineResult: String?,
    public val engineResultCode: Int?,
    public val ledgerIndex: LedgerIndex?,
    public val validated: Boolean,
    public val transaction: JsonElement?,
    public val meta: JsonElement?,
) {
    override fun equals(other: Any?): Boolean =
        other is TransactionEvent && hash == other.hash && validated == other.validated

    override fun hashCode(): Int {
        var result = hash?.hashCode() ?: 0
        result = 31 * result + validated.hashCode()
        return result
    }

    override fun toString(): String = "TransactionEvent(hash=$hash, engineResult=$engineResult, validated=$validated)"
}

/**
 * An account-affecting event from a WebSocket subscription.
 */
public class AccountEvent(
    public val hash: TxHash?,
    public val engineResult: String?,
    public val engineResultCode: Int?,
    public val ledgerIndex: LedgerIndex?,
    public val validated: Boolean,
    public val transaction: JsonElement?,
    public val meta: JsonElement?,
) {
    override fun equals(other: Any?): Boolean =
        other is AccountEvent && hash == other.hash && validated == other.validated

    override fun hashCode(): Int {
        var result = hash?.hashCode() ?: 0
        result = 31 * result + validated.hashCode()
        return result
    }

    override fun toString(): String = "AccountEvent(hash=$hash, engineResult=$engineResult, validated=$validated)"
}

/**
 * An order book change event from a WebSocket subscription.
 */
public class OrderBookEvent(
    public val ledgerIndex: LedgerIndex?,
    public val transaction: JsonElement?,
    public val meta: JsonElement?,
) {
    override fun equals(other: Any?): Boolean = other is OrderBookEvent && ledgerIndex == other.ledgerIndex

    override fun hashCode(): Int = ledgerIndex?.hashCode() ?: 0

    override fun toString(): String = "OrderBookEvent(ledgerIndex=$ledgerIndex)"
}
