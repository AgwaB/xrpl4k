package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Result of a [submit] RPC call.
 *
 * @property engineResult The engine result code string (e.g., "tesSUCCESS").
 * @property engineResultCode The numeric engine result code.
 * @property engineResultMessage A human-readable message for the engine result.
 * @property txHash The hash of the submitted transaction, if available.
 * @property accepted Whether the transaction was accepted into the open ledger.
 * @property applied Whether the transaction was applied to the open ledger.
 * @property broadcast Whether the transaction was broadcast to network peers.
 * @property kept Whether the transaction was kept in the queue.
 * @property queued Whether the transaction was added to the transaction queue.
 * @property openLedgerCost The current minimum transaction cost for entering the open ledger.
 * @property validatedLedgerIndex The sequence number of the newest validated ledger.
 */
public class SubmitResult(
    public val engineResult: String,
    public val engineResultCode: Int,
    public val engineResultMessage: String,
    public val txHash: TxHash?,
    public val accepted: Boolean,
    public val applied: Boolean,
    public val broadcast: Boolean,
    public val kept: Boolean,
    public val queued: Boolean,
    public val openLedgerCost: XrpDrops?,
    public val validatedLedgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubmitResult) return false
        return engineResult == other.engineResult &&
            engineResultCode == other.engineResultCode &&
            engineResultMessage == other.engineResultMessage &&
            txHash == other.txHash &&
            accepted == other.accepted &&
            applied == other.applied &&
            broadcast == other.broadcast &&
            kept == other.kept &&
            queued == other.queued &&
            openLedgerCost == other.openLedgerCost &&
            validatedLedgerIndex == other.validatedLedgerIndex
    }

    override fun hashCode(): Int {
        var result = engineResult.hashCode()
        result = 31 * result + engineResultCode
        result = 31 * result + engineResultMessage.hashCode()
        result = 31 * result + (txHash?.hashCode() ?: 0)
        result = 31 * result + accepted.hashCode()
        result = 31 * result + applied.hashCode()
        result = 31 * result + broadcast.hashCode()
        result = 31 * result + kept.hashCode()
        result = 31 * result + queued.hashCode()
        result = 31 * result + (openLedgerCost?.hashCode() ?: 0)
        result = 31 * result + (validatedLedgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "SubmitResult(" +
            "engineResult=$engineResult, " +
            "engineResultCode=$engineResultCode, " +
            "engineResultMessage=$engineResultMessage, " +
            "txHash=$txHash, " +
            "accepted=$accepted, " +
            "applied=$applied, " +
            "broadcast=$broadcast, " +
            "kept=$kept, " +
            "queued=$queued, " +
            "openLedgerCost=$openLedgerCost, " +
            "validatedLedgerIndex=$validatedLedgerIndex" +
            ")"
}

/**
 * Result of a [tx] RPC call — contains a transaction and its metadata.
 *
 * @property hash The transaction hash.
 * @property ledgerIndex The ledger sequence number in which this transaction was included.
 * @property meta Transaction metadata as raw JSON.
 * @property validated Whether this transaction is in a validated ledger.
 * @property txJson The full transaction JSON.
 * @property closeTimeIso The close time of the ledger in ISO 8601 format.
 * @property engineResult The engine result string extracted from metadata (e.g., "tesSUCCESS").
 * @property engineResultCode The numeric engine result code extracted from metadata.
 */
public class TransactionResult(
    public val hash: TxHash?,
    public val ledgerIndex: LedgerIndex?,
    public val meta: JsonElement?,
    public val validated: Boolean,
    public val txJson: JsonElement?,
    public val closeTimeIso: String?,
    public val engineResult: String? = null,
    public val engineResultCode: Int? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionResult) return false
        return hash == other.hash &&
            ledgerIndex == other.ledgerIndex &&
            meta == other.meta &&
            validated == other.validated &&
            txJson == other.txJson &&
            closeTimeIso == other.closeTimeIso &&
            engineResult == other.engineResult &&
            engineResultCode == other.engineResultCode
    }

    override fun hashCode(): Int {
        var result = hash?.hashCode() ?: 0
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + (meta?.hashCode() ?: 0)
        result = 31 * result + validated.hashCode()
        result = 31 * result + (txJson?.hashCode() ?: 0)
        result = 31 * result + (closeTimeIso?.hashCode() ?: 0)
        result = 31 * result + (engineResult?.hashCode() ?: 0)
        result = 31 * result + (engineResultCode?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "TransactionResult(" +
            "hash=$hash, " +
            "ledgerIndex=$ledgerIndex, " +
            "meta=$meta, " +
            "validated=$validated, " +
            "txJson=$txJson, " +
            "closeTimeIso=$closeTimeIso, " +
            "engineResult=$engineResult, " +
            "engineResultCode=$engineResultCode" +
            ")"
}

/**
 * A validated transaction — returned by [transactionEntry] or [submitAndWait].
 *
 * @property hash The transaction hash.
 * @property ledgerIndex The ledger sequence number containing this transaction.
 * @property engineResult The engine result string (e.g., "tesSUCCESS").
 * @property engineResultCode The numeric engine result code.
 * @property meta Transaction metadata as raw JSON.
 * @property txJson The full transaction JSON (populated by [transactionEntry]).
 * @property metadata Transaction metadata in the [transactionEntry] format.
 * @property ledgerHash The hash of the ledger containing this transaction.
 */
public class ValidatedTransaction(
    public val hash: TxHash? = null,
    public val ledgerIndex: LedgerIndex? = null,
    public val engineResult: String? = null,
    public val engineResultCode: Int? = null,
    public val meta: JsonElement? = null,
    public val txJson: JsonElement? = null,
    public val metadata: JsonElement? = null,
    public val ledgerHash: org.xrpl.sdk.core.type.Hash256? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValidatedTransaction) return false
        return hash == other.hash &&
            ledgerIndex == other.ledgerIndex &&
            engineResult == other.engineResult &&
            engineResultCode == other.engineResultCode &&
            meta == other.meta &&
            txJson == other.txJson &&
            metadata == other.metadata &&
            ledgerHash == other.ledgerHash
    }

    override fun hashCode(): Int {
        var result = hash?.hashCode() ?: 0
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + (engineResult?.hashCode() ?: 0)
        result = 31 * result + (engineResultCode?.hashCode() ?: 0)
        result = 31 * result + (meta?.hashCode() ?: 0)
        result = 31 * result + (txJson?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (ledgerHash?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ValidatedTransaction(" +
            "hash=$hash, " +
            "ledgerIndex=$ledgerIndex, " +
            "engineResult=$engineResult, " +
            "engineResultCode=$engineResultCode, " +
            "meta=$meta, " +
            "txJson=$txJson, " +
            "metadata=$metadata, " +
            "ledgerHash=$ledgerHash" +
            ")"
}

/**
 * Result of a [simulate] RPC call — the simulated outcome of a transaction.
 *
 * @property engineResult The engine result code string (e.g., "tesSUCCESS").
 * @property engineResultCode The numeric engine result code.
 * @property engineResultMessage A human-readable message for the engine result.
 * @property txJson The full transaction JSON as it would appear after autofill.
 * @property meta Simulated transaction metadata as raw JSON.
 */
public class SimulateResult(
    public val engineResult: String,
    public val engineResultCode: Int,
    public val engineResultMessage: String,
    public val txJson: JsonElement?,
    public val meta: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimulateResult) return false
        return engineResult == other.engineResult &&
            engineResultCode == other.engineResultCode &&
            engineResultMessage == other.engineResultMessage &&
            txJson == other.txJson &&
            meta == other.meta
    }

    override fun hashCode(): Int {
        var result = engineResult.hashCode()
        result = 31 * result + engineResultCode
        result = 31 * result + engineResultMessage.hashCode()
        result = 31 * result + (txJson?.hashCode() ?: 0)
        result = 31 * result + (meta?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "SimulateResult(" +
            "engineResult=$engineResult, " +
            "engineResultCode=$engineResultCode, " +
            "engineResultMessage=$engineResultMessage, " +
            "txJson=$txJson, " +
            "meta=$meta" +
            ")"
}
