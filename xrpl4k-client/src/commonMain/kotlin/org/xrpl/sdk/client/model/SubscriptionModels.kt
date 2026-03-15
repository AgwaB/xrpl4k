package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash

/**
 * A validation event from a WebSocket subscription to the "validations" stream.
 */
public class ValidationEvent(
    public val ledgerHash: String?,
    public val ledgerIndex: LedgerIndex?,
    public val signature: String?,
    public val signingTime: Long?,
    public val validationPublicKey: String?,
    public val flags: Long?,
    public val full: Boolean?,
    public val rawJson: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValidationEvent) return false
        return ledgerHash == other.ledgerHash &&
            ledgerIndex == other.ledgerIndex &&
            signature == other.signature &&
            validationPublicKey == other.validationPublicKey
    }

    override fun hashCode(): Int {
        var result = ledgerHash?.hashCode() ?: 0
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + (signature?.hashCode() ?: 0)
        result = 31 * result + (validationPublicKey?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ValidationEvent(" +
            "ledgerHash=$ledgerHash, " +
            "ledgerIndex=$ledgerIndex, " +
            "validationPublicKey=$validationPublicKey" +
            ")"
}

/**
 * A consensus phase event from a WebSocket subscription to the "consensus" stream.
 */
public class ConsensusEvent(
    public val phase: String?,
    public val rawJson: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConsensusEvent) return false
        return phase == other.phase
    }

    override fun hashCode(): Int = phase?.hashCode() ?: 0

    override fun toString(): String = "ConsensusEvent(phase=$phase)"
}

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

/**
 * A peer status event from a WebSocket subscription to the "peer_status" stream.
 */
public class PeerStatusEvent(
    public val action: String?,
    public val date: String?,
    public val address: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeerStatusEvent) return false
        return action == other.action && date == other.date && address == other.address
    }

    override fun hashCode(): Int {
        var result = action?.hashCode() ?: 0
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "PeerStatusEvent(action=$action, address=$address)"
}

/**
 * A manifest event from a WebSocket subscription to the "manifests" stream.
 */
public class ManifestEvent(
    public val masterKey: String?,
    public val signingKey: String?,
    public val seq: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManifestEvent) return false
        return masterKey == other.masterKey && signingKey == other.signingKey && seq == other.seq
    }

    override fun hashCode(): Int {
        var result = masterKey?.hashCode() ?: 0
        result = 31 * result + (signingKey?.hashCode() ?: 0)
        result = 31 * result + (seq?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "ManifestEvent(masterKey=$masterKey, seq=$seq)"
}

/**
 * A server status event from a WebSocket subscription to the "server" stream.
 */
public class ServerEvent(
    public val serverStatus: String?,
    public val loadFactor: Double?,
    public val baseFee: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerEvent) return false
        return serverStatus == other.serverStatus && baseFee == other.baseFee
    }

    override fun hashCode(): Int {
        var result = serverStatus?.hashCode() ?: 0
        result = 31 * result + (baseFee?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ServerEvent(serverStatus=$serverStatus, loadFactor=$loadFactor, baseFee=$baseFee)"
}

/**
 * A path find update event from a WebSocket `path_find` create subscription.
 */
public class PathFindEvent(
    public val alternatives: JsonElement?,
    public val sourceAccount: Address?,
    public val destinationAccount: Address?,
    public val destinationAmount: JsonElement?,
    public val fullReply: Boolean?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathFindEvent) return false
        return sourceAccount == other.sourceAccount &&
            destinationAccount == other.destinationAccount &&
            fullReply == other.fullReply
    }

    override fun hashCode(): Int {
        var result = sourceAccount?.hashCode() ?: 0
        result = 31 * result + (destinationAccount?.hashCode() ?: 0)
        result = 31 * result + (fullReply?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PathFindEvent(sourceAccount=$sourceAccount, destinationAccount=$destinationAccount, fullReply=$fullReply)"
}
