package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── EscrowCreate ─────────────────────────────────────────────────────────────

/**
 * Fields specific to an EscrowCreate transaction.
 *
 * @property destination The address to receive the escrowed XRP.
 * @property amount The amount of XRP to escrow.
 * @property finishAfter The time after which the escrow can be finished.
 * @property cancelAfter The time after which the escrow can be cancelled.
 * @property condition Hex-encoded PREIMAGE-SHA-256 crypto-condition.
 * @property destinationTag Optional destination tag.
 */
public class EscrowCreateFields(
    public val destination: Address,
    public val amount: CurrencyAmount,
    public val finishAfter: UInt? = null,
    public val cancelAfter: UInt? = null,
    public val condition: String? = null,
    public val destinationTag: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EscrowCreateFields) return false
        return destination == other.destination &&
            amount == other.amount &&
            finishAfter == other.finishAfter &&
            cancelAfter == other.cancelAfter &&
            condition == other.condition &&
            destinationTag == other.destinationTag
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (finishAfter?.hashCode() ?: 0)
        result = 31 * result + (cancelAfter?.hashCode() ?: 0)
        result = 31 * result + (condition?.hashCode() ?: 0)
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "EscrowCreateFields(" +
            "destination=$destination, " +
            "amount=$amount, " +
            "finishAfter=$finishAfter, " +
            "cancelAfter=$cancelAfter, " +
            "condition=$condition, " +
            "destinationTag=$destinationTag" +
            ")"
}

/**
 * DSL builder for [EscrowCreateFields].
 */
@XrplDsl
public class EscrowCreateBuilder internal constructor() {
    /** The address to receive the escrowed XRP. Required. */
    public var destination: Address? = null

    /** The amount of XRP to escrow. Required. */
    public lateinit var amount: CurrencyAmount

    /** The time after which the escrow can be finished. */
    public var finishAfter: UInt? = null

    /** The time after which the escrow can be cancelled. */
    public var cancelAfter: UInt? = null

    /** Hex-encoded PREIMAGE-SHA-256 crypto-condition. */
    public var condition: String? = null

    /** Optional destination tag. */
    public var destinationTag: UInt? = null

    internal fun build(): EscrowCreateFields {
        val destinationValue = requireNotNull(destination) { "destination is required" }
        require(::amount.isInitialized) { "amount is required" }
        return EscrowCreateFields(
            destination = destinationValue,
            amount = amount,
            finishAfter = finishAfter,
            cancelAfter = cancelAfter,
            condition = condition,
            destinationTag = destinationTag,
        )
    }
}

// ── EscrowFinish ─────────────────────────────────────────────────────────────

/**
 * Fields specific to an EscrowFinish transaction.
 *
 * @property owner The address of the account that created the escrow.
 * @property offerSequence The sequence number of the EscrowCreate transaction.
 * @property condition Hex-encoded PREIMAGE-SHA-256 crypto-condition.
 * @property fulfillment Hex-encoded PREIMAGE-SHA-256 crypto-fulfillment.
 */
public class EscrowFinishFields(
    public val owner: Address,
    public val offerSequence: UInt,
    public val condition: String? = null,
    public val fulfillment: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EscrowFinishFields) return false
        return owner == other.owner &&
            offerSequence == other.offerSequence &&
            condition == other.condition &&
            fulfillment == other.fulfillment
    }

    override fun hashCode(): Int {
        var result = owner.hashCode()
        result = 31 * result + offerSequence.hashCode()
        result = 31 * result + (condition?.hashCode() ?: 0)
        result = 31 * result + (fulfillment?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "EscrowFinishFields(" +
            "owner=$owner, " +
            "offerSequence=$offerSequence, " +
            "condition=$condition, " +
            "fulfillment=$fulfillment" +
            ")"
}

/**
 * DSL builder for [EscrowFinishFields].
 */
@XrplDsl
public class EscrowFinishBuilder internal constructor() {
    /** The address of the account that created the escrow. Required. */
    public var owner: Address? = null

    /** The sequence number of the EscrowCreate transaction. Required. */
    public var offerSequence: UInt = 0u
    private var offerSequenceSet: Boolean = false

    /** Hex-encoded PREIMAGE-SHA-256 crypto-condition. */
    public var condition: String? = null

    /** Hex-encoded PREIMAGE-SHA-256 crypto-fulfillment. */
    public var fulfillment: String? = null

    internal fun build(): EscrowFinishFields {
        val ownerValue = requireNotNull(owner) { "owner is required" }
        return EscrowFinishFields(
            owner = ownerValue,
            offerSequence = offerSequence,
            condition = condition,
            fulfillment = fulfillment,
        )
    }
}

// ── EscrowCancel ─────────────────────────────────────────────────────────────

/**
 * Fields specific to an EscrowCancel transaction.
 *
 * @property owner The address of the account that created the escrow.
 * @property offerSequence The sequence number of the EscrowCreate transaction.
 */
public class EscrowCancelFields(
    public val owner: Address,
    public val offerSequence: UInt,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EscrowCancelFields) return false
        return owner == other.owner &&
            offerSequence == other.offerSequence
    }

    override fun hashCode(): Int {
        var result = owner.hashCode()
        result = 31 * result + offerSequence.hashCode()
        return result
    }

    override fun toString(): String =
        "EscrowCancelFields(" +
            "owner=$owner, " +
            "offerSequence=$offerSequence" +
            ")"
}

/**
 * DSL builder for [EscrowCancelFields].
 */
@XrplDsl
public class EscrowCancelBuilder internal constructor() {
    /** The address of the account that created the escrow. Required. */
    public var owner: Address? = null

    /** The sequence number of the EscrowCreate transaction. Required. */
    public var offerSequence: UInt = 0u

    internal fun build(): EscrowCancelFields {
        val ownerValue = requireNotNull(owner) { "owner is required" }
        return EscrowCancelFields(
            owner = ownerValue,
            offerSequence = offerSequence,
        )
    }
}
