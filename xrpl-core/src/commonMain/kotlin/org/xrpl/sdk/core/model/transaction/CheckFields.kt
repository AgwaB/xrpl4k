package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── CheckCreate ───────────────────────────────────────────────────────────────

/**
 * Fields specific to a CheckCreate transaction.
 *
 * @property destination The address that can cash the check.
 * @property sendMax The maximum amount the check can debit from the sender.
 * @property destinationTag Optional destination tag.
 * @property expiration The time after which the check expires.
 * @property invoiceId Optional 256-bit hash to identify the invoice reason.
 */
public class CheckCreateFields(
    public val destination: Address,
    public val sendMax: CurrencyAmount,
    public val destinationTag: UInt? = null,
    public val expiration: UInt? = null,
    public val invoiceId: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CheckCreateFields) return false
        return destination == other.destination &&
            sendMax == other.sendMax &&
            destinationTag == other.destinationTag &&
            expiration == other.expiration &&
            invoiceId == other.invoiceId
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + sendMax.hashCode()
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (invoiceId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "CheckCreateFields(" +
            "destination=$destination, " +
            "sendMax=$sendMax, " +
            "destinationTag=$destinationTag, " +
            "expiration=$expiration, " +
            "invoiceId=$invoiceId" +
            ")"
}

/**
 * DSL builder for [CheckCreateFields].
 */
@XrplDsl
public class CheckCreateBuilder internal constructor() {
    /** The address that can cash the check. Required. */
    public var destination: Address? = null

    /** The maximum amount the check can debit from the sender. Required. */
    public lateinit var sendMax: CurrencyAmount

    /** Optional destination tag. */
    public var destinationTag: UInt? = null

    /** The time after which the check expires. */
    public var expiration: UInt? = null

    /** Optional 256-bit hash to identify the invoice reason. */
    public var invoiceId: String? = null

    internal fun build(): CheckCreateFields {
        val destinationValue = requireNotNull(destination) { "destination is required" }
        require(::sendMax.isInitialized) { "sendMax is required" }
        return CheckCreateFields(
            destination = destinationValue,
            sendMax = sendMax,
            destinationTag = destinationTag,
            expiration = expiration,
            invoiceId = invoiceId,
        )
    }
}

// ── CheckCash ─────────────────────────────────────────────────────────────────

/**
 * Fields specific to a CheckCash transaction.
 *
 * @property checkId The ID of the check to cash.
 * @property amount Exact amount to deliver (mutually exclusive with deliverMin).
 * @property deliverMin Minimum amount to deliver (mutually exclusive with amount).
 */
public class CheckCashFields(
    public val checkId: String,
    public val amount: CurrencyAmount? = null,
    public val deliverMin: CurrencyAmount? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CheckCashFields) return false
        return checkId == other.checkId &&
            amount == other.amount &&
            deliverMin == other.deliverMin
    }

    override fun hashCode(): Int {
        var result = checkId.hashCode()
        result = 31 * result + (amount?.hashCode() ?: 0)
        result = 31 * result + (deliverMin?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "CheckCashFields(" +
            "checkId=$checkId, " +
            "amount=$amount, " +
            "deliverMin=$deliverMin" +
            ")"
}

/**
 * DSL builder for [CheckCashFields].
 */
@XrplDsl
public class CheckCashBuilder internal constructor() {
    /** The ID of the check to cash. Required. */
    public lateinit var checkId: String

    /** Exact amount to deliver. */
    public var amount: CurrencyAmount? = null

    /** Minimum amount to deliver. */
    public var deliverMin: CurrencyAmount? = null

    internal fun build(): CheckCashFields {
        require(::checkId.isInitialized) { "checkId is required" }
        return CheckCashFields(
            checkId = checkId,
            amount = amount,
            deliverMin = deliverMin,
        )
    }
}

// ── CheckCancel ───────────────────────────────────────────────────────────────

/**
 * Fields specific to a CheckCancel transaction.
 *
 * @property checkId The ID of the check to cancel.
 */
public class CheckCancelFields(
    public val checkId: String,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CheckCancelFields) return false
        return checkId == other.checkId
    }

    override fun hashCode(): Int = checkId.hashCode()

    override fun toString(): String = "CheckCancelFields(checkId=$checkId)"
}

/**
 * DSL builder for [CheckCancelFields].
 */
@XrplDsl
public class CheckCancelBuilder internal constructor() {
    /** The ID of the check to cancel. Required. */
    public lateinit var checkId: String

    internal fun build(): CheckCancelFields {
        require(::checkId.isInitialized) { "checkId is required" }
        return CheckCancelFields(checkId = checkId)
    }
}
