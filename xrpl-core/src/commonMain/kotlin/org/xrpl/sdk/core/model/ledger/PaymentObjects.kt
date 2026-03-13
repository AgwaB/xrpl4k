package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Funds held in escrow pending a time or condition release.
 *
 * @property account The address of the account that created this escrow.
 * @property destination The address of the account that can claim the escrowed funds.
 * @property amount The amount of XRP held in escrow (in drops).
 * @property condition A PREIMAGE-SHA-256 crypto-condition (hex-encoded).
 * @property cancelAfter Time after which this escrow can be cancelled (seconds since Ripple Epoch).
 * @property finishAfter Time after which this escrow can be finished (seconds since Ripple Epoch).
 * @property sourceTag Source tag from the escrowing transaction.
 * @property destinationTag Destination tag for the payment when the escrow is finished.
 * @property ownerNode Hint for the owner directory page.
 * @property destinationNode Hint for the destination's owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class Escrow(
    override val index: Hash256,
    public val account: Address,
    public val destination: Address,
    public val amount: XrpDrops,
    public val condition: String? = null,
    public val cancelAfter: UInt? = null,
    public val finishAfter: UInt? = null,
    public val sourceTag: UInt? = null,
    public val destinationTag: UInt? = null,
    public val ownerNode: String? = null,
    public val destinationNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Escrow

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Escrow) return false
        return index == other.index &&
            account == other.account &&
            destination == other.destination &&
            amount == other.amount &&
            condition == other.condition &&
            cancelAfter == other.cancelAfter &&
            finishAfter == other.finishAfter &&
            sourceTag == other.sourceTag &&
            destinationTag == other.destinationTag &&
            ownerNode == other.ownerNode &&
            destinationNode == other.destinationNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (condition?.hashCode() ?: 0)
        result = 31 * result + (cancelAfter?.hashCode() ?: 0)
        result = 31 * result + (finishAfter?.hashCode() ?: 0)
        result = 31 * result + (sourceTag?.hashCode() ?: 0)
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (destinationNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Escrow(account=$account, destination=$destination, amount=$amount)"
}

/**
 * A unidirectional payment channel for off-ledger micro-payments.
 *
 * @property account The address of the account that created this channel.
 * @property destination The address of the account that can receive payments from this channel.
 * @property amount Total amount of XRP allocated to this channel (in drops).
 * @property balance Amount of XRP already paid out from this channel (in drops).
 * @property publicKey The public key used to verify payment claims (hex-encoded).
 * @property settleDelay Seconds the source must wait before closing the channel after requesting.
 * @property expiration Optional expiration time (seconds since Ripple Epoch).
 * @property cancelAfter Immutable expiration time set at channel creation (seconds since Ripple Epoch).
 * @property sourceTag Source tag from the creating transaction.
 * @property destinationTag Destination tag for payments from this channel.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class PayChannel(
    override val index: Hash256,
    public val account: Address,
    public val destination: Address,
    public val amount: XrpDrops,
    public val balance: XrpDrops,
    public val publicKey: String,
    public val settleDelay: UInt,
    public val expiration: UInt? = null,
    public val cancelAfter: UInt? = null,
    public val sourceTag: UInt? = null,
    public val destinationTag: UInt? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.PayChannel

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PayChannel) return false
        return index == other.index &&
            account == other.account &&
            destination == other.destination &&
            amount == other.amount &&
            balance == other.balance &&
            publicKey == other.publicKey &&
            settleDelay == other.settleDelay &&
            expiration == other.expiration &&
            cancelAfter == other.cancelAfter &&
            sourceTag == other.sourceTag &&
            destinationTag == other.destinationTag &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + settleDelay.hashCode()
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (cancelAfter?.hashCode() ?: 0)
        result = 31 * result + (sourceTag?.hashCode() ?: 0)
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String =
        "PayChannel(account=$account, destination=$destination, amount=$amount, balance=$balance)"
}

/**
 * A deferred payment that the recipient must explicitly cash.
 *
 * @property account The address of the account that created this check.
 * @property destination The intended recipient of this check.
 * @property sendMax The maximum amount the check can deliver.
 * @property sequence The sequence number of the transaction that created this check.
 * @property expiration Optional expiration time (seconds since Ripple Epoch).
 * @property invoiceID Arbitrary 256-bit hash provided by the creator.
 * @property sourceTag Source tag from the creating transaction.
 * @property destinationTag Destination tag for the payment.
 * @property ownerNode Hint for the owner directory page.
 * @property destinationNode Hint for the destination's owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class Check(
    override val index: Hash256,
    public val account: Address,
    public val destination: Address,
    public val sendMax: CurrencyAmount,
    public val sequence: UInt,
    public val expiration: UInt? = null,
    public val invoiceID: Hash256? = null,
    public val sourceTag: UInt? = null,
    public val destinationTag: UInt? = null,
    public val ownerNode: String? = null,
    public val destinationNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Check

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Check) return false
        return index == other.index &&
            account == other.account &&
            destination == other.destination &&
            sendMax == other.sendMax &&
            sequence == other.sequence &&
            expiration == other.expiration &&
            invoiceID == other.invoiceID &&
            sourceTag == other.sourceTag &&
            destinationTag == other.destinationTag &&
            ownerNode == other.ownerNode &&
            destinationNode == other.destinationNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + sendMax.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (invoiceID?.hashCode() ?: 0)
        result = 31 * result + (sourceTag?.hashCode() ?: 0)
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (destinationNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Check(account=$account, destination=$destination, sendMax=$sendMax)"
}
