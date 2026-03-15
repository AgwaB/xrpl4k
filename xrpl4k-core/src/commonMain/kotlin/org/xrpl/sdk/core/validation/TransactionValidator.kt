package org.xrpl.sdk.core.validation

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.MptAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.AccountSetFields
import org.xrpl.sdk.core.model.transaction.CheckCashFields
import org.xrpl.sdk.core.model.transaction.CheckCreateFields
import org.xrpl.sdk.core.model.transaction.EscrowCreateFields
import org.xrpl.sdk.core.model.transaction.EscrowFinishFields
import org.xrpl.sdk.core.model.transaction.NFTokenCreateOfferFields
import org.xrpl.sdk.core.model.transaction.NFTokenMintFields
import org.xrpl.sdk.core.model.transaction.OfferCreateFields
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.SignerListSetFields
import org.xrpl.sdk.core.model.transaction.TicketCreateFields
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
            is TrustSetFields -> validateTrustSet(fields, accountValue, errors)
            is AccountSetFields -> validateAccountSet(fields, errors)
            is EscrowCreateFields -> validateEscrowCreate(fields, errors)
            is EscrowFinishFields -> validateEscrowFinish(fields, errors)
            is CheckCreateFields -> validateCheckCreate(fields, accountValue, errors)
            is CheckCashFields -> validateCheckCash(fields, errors)
            is NFTokenMintFields -> validateNFTokenMint(fields, accountValue, errors)
            is NFTokenCreateOfferFields -> validateNFTokenCreateOffer(fields, errors)
            is SignerListSetFields -> validateSignerListSet(fields, accountValue, errors)
            is TicketCreateFields -> validateTicketCreate(fields, errors)
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
        // TakerGets and TakerPays must not be the same currency+issuer
        if (fields.takerGets.sameCurrency(fields.takerPays)) {
            errors += "OfferCreate TakerGets and TakerPays must not be the same currency and issuer."
        }
    }

    private fun validateTrustSet(
        fields: TrustSetFields,
        accountValue: String,
        errors: MutableList<String>,
    ) {
        // TrustSetFields.limitAmount is typed as IssuedAmount, which already prevents XRP
        // currency codes at construction time (CurrencyCode rejects "XRP"). This rule is
        // therefore enforced by the type system, but we document it explicitly.
        val currencyValue = fields.limitAmount.currency.value
        if (currencyValue == "XRP") {
            errors += "TrustSet LimitAmount currency must not be XRP."
        }
        // Cannot create a trust line to yourself
        if (fields.limitAmount.issuer.value == accountValue) {
            errors += "TrustSet LimitAmount issuer must differ from the sending account."
        }
    }

    private fun validateAccountSet(
        fields: AccountSetFields,
        errors: MutableList<String>,
    ) {
        // SetFlag and ClearFlag must not be the same value
        val setFlag = fields.setFlag
        val clearFlag = fields.clearFlag
        if (setFlag != null && clearFlag != null && setFlag == clearFlag) {
            errors += "AccountSet SetFlag and ClearFlag must not be the same value."
        }
    }

    private fun validateEscrowCreate(
        fields: EscrowCreateFields,
        errors: MutableList<String>,
    ) {
        if (!fields.amount.isPositive()) {
            errors += "EscrowCreate amount must be positive."
        }
        if (fields.destination.value.isEmpty()) {
            errors += "EscrowCreate destination must not be empty."
        }
        // Must have either finishAfter or condition (or both)
        if (fields.finishAfter == null && fields.condition == null) {
            errors += "EscrowCreate requires either finishAfter or condition (or both)."
        }
        // If both cancelAfter and finishAfter are set, cancelAfter must be after finishAfter
        val cancelAfter = fields.cancelAfter
        val finishAfter = fields.finishAfter
        if (cancelAfter != null && finishAfter != null && cancelAfter <= finishAfter) {
            errors += "EscrowCreate cancelAfter must be after finishAfter."
        }
    }

    private fun validateEscrowFinish(
        fields: EscrowFinishFields,
        errors: MutableList<String>,
    ) {
        if (fields.owner.value.isEmpty()) {
            errors += "EscrowFinish owner must not be empty."
        }
        // condition and fulfillment must both be present or both absent
        if ((fields.condition != null) != (fields.fulfillment != null)) {
            errors += "EscrowFinish condition and fulfillment must both be present or both absent."
        }
    }

    private fun validateCheckCreate(
        fields: CheckCreateFields,
        accountValue: String,
        errors: MutableList<String>,
    ) {
        if (fields.destination.value.isEmpty()) {
            errors += "CheckCreate destination must not be empty."
        } else if (fields.destination.value == accountValue) {
            errors += "CheckCreate destination must differ from the sending account."
        }
        if (!fields.sendMax.isPositive()) {
            errors += "CheckCreate SendMax must be positive."
        }
    }

    private fun validateCheckCash(
        fields: CheckCashFields,
        errors: MutableList<String>,
    ) {
        if (fields.checkId.isBlank()) {
            errors += "CheckCash checkId must not be empty."
        }
        // Must have exactly one of amount or deliverMin
        if (fields.amount == null && fields.deliverMin == null) {
            errors += "CheckCash requires either amount or deliverMin."
        }
        if (fields.amount != null && fields.deliverMin != null) {
            errors += "CheckCash must not set both amount and deliverMin."
        }
        fields.amount?.let { amount ->
            if (!amount.isPositive()) {
                errors += "CheckCash amount must be positive."
            }
        }
        fields.deliverMin?.let { deliverMin ->
            if (!deliverMin.isPositive()) {
                errors += "CheckCash deliverMin must be positive."
            }
        }
    }

    private fun validateNFTokenMint(
        fields: NFTokenMintFields,
        accountValue: String,
        errors: MutableList<String>,
    ) {
        // TransferFee must be 0..50000
        fields.transferFee?.let { fee ->
            if (fee > 50_000u) {
                errors += "NFTokenMint transferFee must be between 0 and 50000 inclusive."
            }
        }
        // Issuer must not equal the sending account
        fields.issuer?.let { issuer ->
            if (issuer.value == accountValue) {
                errors += "NFTokenMint issuer must not equal the sending account."
            }
        }
        // URI must not be empty string
        fields.uri?.let { uri ->
            if (uri.isEmpty()) {
                errors += "NFTokenMint URI must not be empty string."
            }
        }
    }

    private fun validateNFTokenCreateOffer(
        fields: NFTokenCreateOfferFields,
        errors: MutableList<String>,
    ) {
        if (fields.nfTokenId.isBlank()) {
            errors += "NFTokenCreateOffer nfTokenId must not be empty."
        }
    }

    private fun validateSignerListSet(
        fields: SignerListSetFields,
        accountValue: String,
        errors: MutableList<String>,
    ) {
        val quorum = fields.signerQuorum
        val entries = fields.signerEntries

        // If quorum > 0, signerEntries must not be empty
        if (quorum > 0u && entries.isEmpty()) {
            errors += "SignerListSet signerEntries must not be empty when signerQuorum > 0."
        }
        // If quorum is 0, entries must also be empty (delete operation)
        if (quorum == 0u && entries.isNotEmpty()) {
            errors += "SignerListSet signerEntries must be empty when signerQuorum is 0 (delete)."
        }
        // No signer entry may be the sending account
        for (entry in entries) {
            if (entry.account.value == accountValue) {
                errors += "SignerListSet signerEntries must not include the sending account."
                break
            }
        }
        // No duplicate signer accounts
        val accounts = entries.map { it.account.value }
        if (accounts.size != accounts.toSet().size) {
            errors += "SignerListSet signerEntries must not contain duplicate accounts."
        }
    }

    private fun validateTicketCreate(
        fields: TicketCreateFields,
        errors: MutableList<String>,
    ) {
        if (fields.ticketCount < 1u || fields.ticketCount > 250u) {
            errors += "TicketCreate ticketCount must be between 1 and 250 inclusive."
        }
    }

    private fun CurrencyAmount.isPositive(): Boolean =
        when (this) {
            is XrpAmount -> drops.value > 0L
            is IssuedAmount -> issuedAmountPositive(value)
            is MptAmount -> value > 0L
        }

    /**
     * Returns true when two currency amounts represent the same currency and issuer.
     * XRP-to-XRP is same currency. Two IssuedAmounts match if currency+issuer are identical.
     * Two MptAmounts match if mptIssuanceId is identical.
     * Mixed types never match.
     */
    private fun CurrencyAmount.sameCurrency(other: CurrencyAmount): Boolean =
        when {
            this is XrpAmount && other is XrpAmount -> true
            this is IssuedAmount && other is IssuedAmount ->
                currency == other.currency && issuer == other.issuer
            this is MptAmount && other is MptAmount ->
                mptIssuanceId == other.mptIssuanceId
            else -> false
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
