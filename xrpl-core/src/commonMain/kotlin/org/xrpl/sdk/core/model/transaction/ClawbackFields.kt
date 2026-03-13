package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── Clawback ──────────────────────────────────────────────────────────────────

/**
 * Fields specific to a Clawback transaction.
 *
 * @property amount The amount to claw back, including the token currency and issuer.
 * @property holder The account to claw back tokens from.
 */
public class ClawbackFields(
    public val amount: CurrencyAmount,
    public val holder: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClawbackFields) return false
        return amount == other.amount &&
            holder == other.holder
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + (holder?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ClawbackFields(" +
            "amount=$amount, " +
            "holder=$holder" +
            ")"
}

/**
 * DSL builder for [ClawbackFields].
 */
@XrplDsl
public class ClawbackBuilder internal constructor() {
    /** The amount to claw back. Required. */
    public lateinit var amount: CurrencyAmount

    /** The account to claw back tokens from. */
    public var holder: Address? = null

    internal fun build(): ClawbackFields {
        require(::amount.isInitialized) { "amount is required" }
        return ClawbackFields(
            amount = amount,
            holder = holder,
        )
    }
}
