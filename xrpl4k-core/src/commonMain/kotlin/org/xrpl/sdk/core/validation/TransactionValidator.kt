package org.xrpl.sdk.core.validation

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.MptAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.OfferCreateFields
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TrustSetFields
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.XrpDrops

/**
 * The result of validating an [XrplTransaction].
 */
public sealed interface ValidationResult {
    /** The transaction passed all validation checks. */
    public object Valid : ValidationResult

    /**
     * The transaction failed one or more validation checks.
     *
     * @property errors A non-empty list of human-readable error messages.
     */
    public class Invalid(public val errors: List<String>) : ValidationResult {
        override fun equals(other: Any?): Boolean = other is Invalid && errors == other.errors

        override fun hashCode(): Int = errors.hashCode()

        override fun toString(): String = "Invalid(errors=$errors)"
    }
}

/**
 * Client-side validator for XRPL transactions.
 *
 * All validation rules are checked and accumulated — the validator does not stop on the first error.
 * This lets callers surface all problems at once rather than fix-and-retry one issue at a time.
 *
 * The [MAX_FEE_DROPS] constant defines the default ceiling for transaction fees (10 XRP).
 * Pass a custom value to [validate] overloads when a different limit is required.
 */
public object TransactionValidator {
    /** Default maximum transaction fee: 10 XRP expressed in drops. */
    public val MAX_FEE_DROPS: XrpDrops = XrpDrops(10_000_000L)

    /**
     * Validates an unsigned transaction.
     *
     * Checks account validity and transaction-type-specific rules.
     * Fee, sequence, and lastLedgerSequence are not validated here because they are not
     * present on [XrplTransaction.Unsigned].
     *
     * @param tx The unsigned transaction to validate.
     * @return [ValidationResult.Valid] when all checks pass, otherwise [ValidationResult.Invalid].
     */
    public fun validate(tx: XrplTransaction.Unsigned): ValidationResult {
        val errors = mutableListOf<String>()
        validateCommonUnsigned(tx.account.value, errors)
        validateFields(tx.fields, tx.account.value, errors)
        return errors.toResult()
    }

    /**
     * Validates a filled transaction.
     *
     * Checks all common fields (account, fee, sequence, lastLedgerSequence) as well as
     * transaction-type-specific rules.
     *
     * @param tx The filled transaction to validate.
     * @param maxFee The maximum permitted fee. Defaults to [MAX_FEE_DROPS] (10 XRP).
     * @return [ValidationResult.Valid] when all checks pass, otherwise [ValidationResult.Invalid].
     */
    public fun validate(
        tx: XrplTransaction.Filled,
        maxFee: XrpDrops = MAX_FEE_DROPS,
    ): ValidationResult {
        val errors = mutableListOf<String>()
        validateCommonUnsigned(tx.account.value, errors)
        validateCommonFilled(tx, maxFee, errors)
        validateFields(tx.fields, tx.account.value, errors)
        return errors.toResult()
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private fun validateCommonUnsigned(
        accountValue: String,
        errors: MutableList<String>,
    ) {
        if (accountValue.isEmpty()) {
            errors += "Account address must not be empty."
        }
    }

    private fun validateCommonFilled(
        tx: XrplTransaction.Filled,
        maxFee: XrpDrops,
        errors: MutableList<String>,
    ) {
        // XrpDrops is always non-negative by construction, so only the ceiling check is needed.
        if (tx.fee > maxFee) {
            errors += "Fee ${tx.fee.value} drops exceeds maximum allowed ${maxFee.value} drops."
        }
        if (tx.sequence == 0u) {
            errors += "Sequence must be greater than 0."
        }
        if (tx.lastLedgerSequence == 0u) {
            errors += "LastLedgerSequence must be greater than 0."
        }
    }

    private fun validateFields(
        fields: org.xrpl.sdk.core.model.transaction.TransactionFields,
        accountValue: String,
        errors: MutableList<String>,
    ) {
        when (fields) {
            is PaymentFields -> validatePayment(fields, accountValue, errors)
            is OfferCreateFields -> validateOfferCreate(fields, errors)
            is TrustSetFields -> validateTrustSet(fields, errors)
            else -> Unit // no additional validation for other transaction types
        }
    }

    private fun validatePayment(
        fields: PaymentFields,
        accountValue: String,
        errors: MutableList<String>,
    ) {
        if (fields.destination.value.isEmpty()) {
            errors += "Payment destination must not be empty."
        } else if (fields.destination.value == accountValue) {
            errors += "Payment destination must differ from the sending account."
        }
        if (!fields.amount.isPositive()) {
            errors += "Payment amount must be positive."
        }
        fields.sendMax?.let { sendMax ->
            if (!sendMax.isPositive()) {
                errors += "Payment SendMax must be positive."
            }
        }
        fields.deliverMin?.let { deliverMin ->
            if (!deliverMin.isPositive()) {
                errors += "Payment DeliverMin must be positive."
            }
        }
    }

    private fun validateOfferCreate(
        fields: OfferCreateFields,
        errors: MutableList<String>,
    ) {
        if (!fields.takerGets.isPositive()) {
            errors += "OfferCreate TakerGets must be positive."
        }
        if (!fields.takerPays.isPositive()) {
            errors += "OfferCreate TakerPays must be positive."
        }
    }

    private fun validateTrustSet(
        fields: TrustSetFields,
        errors: MutableList<String>,
    ) {
        // TrustSetFields.limitAmount is typed as IssuedAmount, which already prevents XRP
        // currency codes at construction time (CurrencyCode rejects "XRP"). This rule is
        // therefore enforced by the type system, but we document it explicitly.
        val currencyValue = fields.limitAmount.currency.value
        if (currencyValue == "XRP") {
            errors += "TrustSet LimitAmount currency must not be XRP."
        }
    }

    private fun CurrencyAmount.isPositive(): Boolean =
        when (this) {
            is XrpAmount -> drops.value > 0L
            is IssuedAmount -> issuedAmountPositive(value)
            is MptAmount -> value > 0L
        }

    /**
     * Returns true when the decimal string [v] represents a positive number.
     *
     * Handles standard decimal notation and scientific notation (e.g. "1.23e5", "1.23E-2").
     * A leading minus sign or a numeric value of zero is treated as non-positive.
     * Unparseable strings return false.
     */
    private fun issuedAmountPositive(v: String): Boolean {
        if (v.isBlank()) return false
        val s = v.trim()
        // Negative values start with '-'
        if (s.startsWith('-')) return false
        // Split off exponent
        val eIndex = s.indexOfFirst { it == 'e' || it == 'E' }
        val mantissa = if (eIndex >= 0) s.substring(0, eIndex) else s
        // Remove optional leading '+'
        val digits = mantissa.removePrefix("+").replace(".", "")
        // All digit characters must be zero for the value to be non-positive
        return digits.isNotEmpty() && digits.any { it != '0' }
    }

    private fun MutableList<String>.toResult(): ValidationResult =
        if (isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(toList())
}

/**
 * Validates this unsigned transaction using [TransactionValidator].
 *
 * @return [ValidationResult.Valid] when all checks pass, otherwise [ValidationResult.Invalid].
 */
public fun XrplTransaction.Unsigned.validate(): ValidationResult = TransactionValidator.validate(this)

/**
 * Validates this filled transaction using [TransactionValidator].
 *
 * @param maxFee The maximum permitted fee. Defaults to [TransactionValidator.MAX_FEE_DROPS] (10 XRP).
 * @return [ValidationResult.Valid] when all checks pass, otherwise [ValidationResult.Invalid].
 */
public fun XrplTransaction.Filled.validate(maxFee: XrpDrops = TransactionValidator.MAX_FEE_DROPS): ValidationResult =
    TransactionValidator.validate(this, maxFee)
