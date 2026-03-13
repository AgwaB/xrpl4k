package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── LoanSet ───────────────────────────────────────────────────────────────────

/**
 * Fields specific to a LoanSet transaction.
 *
 * @property collateralAsset The collateral asset identifier.
 * @property loanAsset The loan asset identifier.
 * @property flags Flags controlling loan behavior.
 */
@ExperimentalXrplApi
public class LoanSetFields(
    public val collateralAsset: String,
    public val loanAsset: String,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanSetFields) return false
        return collateralAsset == other.collateralAsset &&
            loanAsset == other.loanAsset &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = collateralAsset.hashCode()
        result = 31 * result + loanAsset.hashCode()
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "LoanSetFields(collateralAsset=$collateralAsset, loanAsset=$loanAsset, flags=$flags)"
}

/**
 * DSL builder for [LoanSetFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanSetBuilder internal constructor() {
    /** The collateral asset identifier. Required. */
    public lateinit var collateralAsset: String

    /** The loan asset identifier. Required. */
    public lateinit var loanAsset: String

    /** Flags controlling loan behavior. */
    public var flags: UInt? = null

    internal fun build(): LoanSetFields {
        require(::collateralAsset.isInitialized) { "collateralAsset is required" }
        require(::loanAsset.isInitialized) { "loanAsset is required" }
        return LoanSetFields(collateralAsset = collateralAsset, loanAsset = loanAsset, flags = flags)
    }
}

// ── LoanDelete ────────────────────────────────────────────────────────────────

/**
 * Fields specific to a LoanDelete transaction.
 *
 * @property loanId The ID of the loan to delete.
 */
@ExperimentalXrplApi
public class LoanDeleteFields(
    public val loanId: String,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanDeleteFields) return false
        return loanId == other.loanId
    }

    override fun hashCode(): Int = loanId.hashCode()

    override fun toString(): String = "LoanDeleteFields(loanId=$loanId)"
}

/**
 * DSL builder for [LoanDeleteFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanDeleteBuilder internal constructor() {
    /** The ID of the loan to delete. Required. */
    public lateinit var loanId: String

    internal fun build(): LoanDeleteFields {
        require(::loanId.isInitialized) { "loanId is required" }
        return LoanDeleteFields(loanId = loanId)
    }
}

// ── LoanManage ────────────────────────────────────────────────────────────────

/**
 * Fields specific to a LoanManage transaction.
 *
 * @property loanId The ID of the loan to manage.
 * @property flags Flags controlling the operation.
 */
@ExperimentalXrplApi
public class LoanManageFields(
    public val loanId: String,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanManageFields) return false
        return loanId == other.loanId && flags == other.flags
    }

    override fun hashCode(): Int {
        var result = loanId.hashCode()
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "LoanManageFields(loanId=$loanId, flags=$flags)"
}

/**
 * DSL builder for [LoanManageFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanManageBuilder internal constructor() {
    /** The ID of the loan to manage. Required. */
    public lateinit var loanId: String

    /** Flags controlling the operation. */
    public var flags: UInt? = null

    internal fun build(): LoanManageFields {
        require(::loanId.isInitialized) { "loanId is required" }
        return LoanManageFields(loanId = loanId, flags = flags)
    }
}

// ── LoanPay ───────────────────────────────────────────────────────────────────

/**
 * Fields specific to a LoanPay transaction.
 *
 * @property loanId The ID of the loan to pay.
 * @property amount The amount to pay.
 */
@ExperimentalXrplApi
public class LoanPayFields(
    public val loanId: String,
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanPayFields) return false
        return loanId == other.loanId && amount == other.amount
    }

    override fun hashCode(): Int {
        var result = loanId.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun toString(): String = "LoanPayFields(loanId=$loanId, amount=$amount)"
}

/**
 * DSL builder for [LoanPayFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanPayBuilder internal constructor() {
    /** The ID of the loan to pay. Required. */
    public lateinit var loanId: String

    /** The amount to pay. Required. */
    public lateinit var amount: CurrencyAmount

    internal fun build(): LoanPayFields {
        require(::loanId.isInitialized) { "loanId is required" }
        require(::amount.isInitialized) { "amount is required" }
        return LoanPayFields(loanId = loanId, amount = amount)
    }
}

// ── LoanBrokerSet ─────────────────────────────────────────────────────────────

/**
 * Fields specific to a LoanBrokerSet transaction.
 *
 * @property flags Flags controlling broker behavior.
 */
@ExperimentalXrplApi
public class LoanBrokerSetFields(
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanBrokerSetFields) return false
        return flags == other.flags
    }

    override fun hashCode(): Int = (flags?.hashCode() ?: 0)

    override fun toString(): String = "LoanBrokerSetFields(flags=$flags)"
}

/**
 * DSL builder for [LoanBrokerSetFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanBrokerSetBuilder internal constructor() {
    /** Flags controlling broker behavior. */
    public var flags: UInt? = null

    internal fun build(): LoanBrokerSetFields = LoanBrokerSetFields(flags = flags)
}

// ── LoanBrokerDelete ──────────────────────────────────────────────────────────

/**
 * Fields specific to a LoanBrokerDelete transaction.
 *
 * LoanBrokerDelete has no transaction-type-specific fields.
 */
@ExperimentalXrplApi
public class LoanBrokerDeleteFields : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is LoanBrokerDeleteFields
    }

    override fun hashCode(): Int = "LoanBrokerDeleteFields".hashCode()

    override fun toString(): String = "LoanBrokerDeleteFields()"
}

/**
 * DSL builder for [LoanBrokerDeleteFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanBrokerDeleteBuilder internal constructor() {
    internal fun build(): LoanBrokerDeleteFields = LoanBrokerDeleteFields()
}

// ── LoanBrokerCoverDeposit ────────────────────────────────────────────────────

/**
 * Fields specific to a LoanBrokerCoverDeposit transaction.
 *
 * @property amount The amount to deposit as cover.
 */
@ExperimentalXrplApi
public class LoanBrokerCoverDepositFields(
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanBrokerCoverDepositFields) return false
        return amount == other.amount
    }

    override fun hashCode(): Int = amount.hashCode()

    override fun toString(): String = "LoanBrokerCoverDepositFields(amount=$amount)"
}

/**
 * DSL builder for [LoanBrokerCoverDepositFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanBrokerCoverDepositBuilder internal constructor() {
    /** The amount to deposit as cover. Required. */
    public lateinit var amount: CurrencyAmount

    internal fun build(): LoanBrokerCoverDepositFields {
        require(::amount.isInitialized) { "amount is required" }
        return LoanBrokerCoverDepositFields(amount = amount)
    }
}

// ── LoanBrokerCoverWithdraw ───────────────────────────────────────────────────

/**
 * Fields specific to a LoanBrokerCoverWithdraw transaction.
 *
 * @property amount The amount to withdraw from cover.
 */
@ExperimentalXrplApi
public class LoanBrokerCoverWithdrawFields(
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanBrokerCoverWithdrawFields) return false
        return amount == other.amount
    }

    override fun hashCode(): Int = amount.hashCode()

    override fun toString(): String = "LoanBrokerCoverWithdrawFields(amount=$amount)"
}

/**
 * DSL builder for [LoanBrokerCoverWithdrawFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanBrokerCoverWithdrawBuilder internal constructor() {
    /** The amount to withdraw from cover. Required. */
    public lateinit var amount: CurrencyAmount

    internal fun build(): LoanBrokerCoverWithdrawFields {
        require(::amount.isInitialized) { "amount is required" }
        return LoanBrokerCoverWithdrawFields(amount = amount)
    }
}

// ── LoanBrokerCoverClawback ───────────────────────────────────────────────────

/**
 * Fields specific to a LoanBrokerCoverClawback transaction.
 *
 * @property amount The amount to claw back from cover.
 * @property holder The holder account to claw back from.
 */
@ExperimentalXrplApi
public class LoanBrokerCoverClawbackFields(
    public val amount: CurrencyAmount,
    public val holder: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanBrokerCoverClawbackFields) return false
        return amount == other.amount && holder == other.holder
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + (holder?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "LoanBrokerCoverClawbackFields(amount=$amount, holder=$holder)"
}

/**
 * DSL builder for [LoanBrokerCoverClawbackFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class LoanBrokerCoverClawbackBuilder internal constructor() {
    /** The amount to claw back from cover. Required. */
    public lateinit var amount: CurrencyAmount

    /** The holder account to claw back from. */
    public var holder: Address? = null

    internal fun build(): LoanBrokerCoverClawbackFields {
        require(::amount.isInitialized) { "amount is required" }
        return LoanBrokerCoverClawbackFields(amount = amount, holder = holder)
    }
}
