package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Summary information about a specific ledger version.
 */
public class LedgerInfo(
    public val ledgerHash: Hash256?,
    public val ledgerIndex: LedgerIndex?,
    public val accountHash: String?,
    public val transactionHash: String?,
    public val parentHash: String?,
    public val closeTime: Long?,
    public val closeTimeHuman: String?,
    public val totalCoins: XrpDrops?,
    public val closed: Boolean,
    public val transactions: List<JsonElement>?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerInfo) return false
        return ledgerHash == other.ledgerHash &&
            ledgerIndex == other.ledgerIndex &&
            accountHash == other.accountHash &&
            transactionHash == other.transactionHash &&
            parentHash == other.parentHash &&
            closeTime == other.closeTime &&
            closeTimeHuman == other.closeTimeHuman &&
            totalCoins == other.totalCoins &&
            closed == other.closed &&
            transactions == other.transactions
    }

    override fun hashCode(): Int {
        var result = ledgerHash?.hashCode() ?: 0
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + (accountHash?.hashCode() ?: 0)
        result = 31 * result + (transactionHash?.hashCode() ?: 0)
        result = 31 * result + (parentHash?.hashCode() ?: 0)
        result = 31 * result + (closeTime?.hashCode() ?: 0)
        result = 31 * result + (closeTimeHuman?.hashCode() ?: 0)
        result = 31 * result + (totalCoins?.hashCode() ?: 0)
        result = 31 * result + closed.hashCode()
        result = 31 * result + (transactions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "LedgerInfo(" +
            "ledgerHash=$ledgerHash, " +
            "ledgerIndex=$ledgerIndex, " +
            "accountHash=$accountHash, " +
            "transactionHash=$transactionHash, " +
            "parentHash=$parentHash, " +
            "closeTime=$closeTime, " +
            "closeTimeHuman=$closeTimeHuman, " +
            "totalCoins=$totalCoins, " +
            "closed=$closed, " +
            "transactions=$transactions" +
            ")"
}

/**
 * Result of a [ledger] RPC call.
 */
public class LedgerResult(
    public val ledger: LedgerInfo?,
    public val ledgerHash: Hash256?,
    public val ledgerIndex: LedgerIndex?,
    public val validated: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerResult) return false
        return ledger == other.ledger &&
            ledgerHash == other.ledgerHash &&
            ledgerIndex == other.ledgerIndex &&
            validated == other.validated
    }

    override fun hashCode(): Int {
        var result = ledger?.hashCode() ?: 0
        result = 31 * result + (ledgerHash?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + validated.hashCode()
        return result
    }

    override fun toString(): String =
        "LedgerResult(" +
            "ledger=$ledger, " +
            "ledgerHash=$ledgerHash, " +
            "ledgerIndex=$ledgerIndex, " +
            "validated=$validated" +
            ")"
}

/**
 * Result of a [ledgerClosed] RPC call.
 */
public class LedgerClosedResult(
    public val ledgerHash: Hash256,
    public val ledgerIndex: LedgerIndex,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerClosedResult) return false
        return ledgerHash == other.ledgerHash && ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = ledgerHash.hashCode()
        result = 31 * result + ledgerIndex.hashCode()
        return result
    }

    override fun toString(): String = "LedgerClosedResult(ledgerHash=$ledgerHash, ledgerIndex=$ledgerIndex)"
}

/**
 * Result of a [ledgerCurrent] RPC call.
 */
public class LedgerCurrentResult(
    public val ledgerCurrentIndex: LedgerIndex,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerCurrentResult) return false
        return ledgerCurrentIndex == other.ledgerCurrentIndex
    }

    override fun hashCode(): Int = ledgerCurrentIndex.hashCode()

    override fun toString(): String = "LedgerCurrentResult(ledgerCurrentIndex=$ledgerCurrentIndex)"
}

/**
 * A single entry in the ledger state returned by [ledgerData].
 */
public class LedgerStateObject(
    public val index: String,
    public val ledgerEntryType: String?,
    public val data: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerStateObject) return false
        return index == other.index &&
            ledgerEntryType == other.ledgerEntryType &&
            data == other.data
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + (ledgerEntryType?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "LedgerStateObject(index=$index, ledgerEntryType=$ledgerEntryType, data=$data)"
}

/**
 * Result of a [ledgerData] RPC call.
 */
public class LedgerDataResult(
    public val ledgerHash: Hash256?,
    public val ledgerIndex: LedgerIndex?,
    public val state: List<LedgerStateObject>,
    public val marker: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerDataResult) return false
        return ledgerHash == other.ledgerHash &&
            ledgerIndex == other.ledgerIndex &&
            state == other.state &&
            marker == other.marker
    }

    override fun hashCode(): Int {
        var result = ledgerHash?.hashCode() ?: 0
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + state.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "LedgerDataResult(" +
            "ledgerHash=$ledgerHash, " +
            "ledgerIndex=$ledgerIndex, " +
            "state=$state, " +
            "marker=$marker" +
            ")"
}

/**
 * Result of a [ledgerEntry] RPC call.
 */
public class LedgerEntryResult(
    public val index: String?,
    public val ledgerIndex: LedgerIndex?,
    public val ledgerHash: Hash256?,
    public val node: JsonElement?,
    public val nodeBinary: String?,
    public val validated: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerEntryResult) return false
        return index == other.index &&
            ledgerIndex == other.ledgerIndex &&
            ledgerHash == other.ledgerHash &&
            node == other.node &&
            nodeBinary == other.nodeBinary &&
            validated == other.validated
    }

    override fun hashCode(): Int {
        var result = index?.hashCode() ?: 0
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + (ledgerHash?.hashCode() ?: 0)
        result = 31 * result + (node?.hashCode() ?: 0)
        result = 31 * result + (nodeBinary?.hashCode() ?: 0)
        result = 31 * result + validated.hashCode()
        return result
    }

    override fun toString(): String =
        "LedgerEntryResult(" +
            "index=$index, " +
            "ledgerIndex=$ledgerIndex, " +
            "ledgerHash=$ledgerHash, " +
            "node=$node, " +
            "nodeBinary=$nodeBinary, " +
            "validated=$validated" +
            ")"
}
