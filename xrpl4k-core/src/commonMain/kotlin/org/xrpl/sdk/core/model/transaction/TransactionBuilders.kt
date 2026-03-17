package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.type.Address

/**
 * Creates a Payment [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun payment(block: PaymentBuilder.() -> Unit): XrplTransaction.Unsigned = PaymentBuilder().apply(block).build()

/**
 * Creates a Payment [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun payment(
    account: Address,
    block: PaymentBuilder.() -> Unit,
): XrplTransaction.Unsigned = PaymentBuilder().apply(block).apply { this.account = account }.build()

/**
 * Creates an OfferCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun offerCreate(block: OfferCreateBuilder.() -> Unit): XrplTransaction.Unsigned =
    OfferCreateBuilder().apply(block).build()

/**
 * Creates an OfferCreate [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun offerCreate(
    account: Address,
    block: OfferCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned = OfferCreateBuilder().apply(block).apply { this.account = account }.build()

/**
 * Creates an OfferCancel [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun offerCancel(block: OfferCancelBuilder.() -> Unit): XrplTransaction.Unsigned =
    OfferCancelBuilder().apply(block).build()

/**
 * Creates an OfferCancel [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun offerCancel(
    account: Address,
    block: OfferCancelBuilder.() -> Unit,
): XrplTransaction.Unsigned = OfferCancelBuilder().apply(block).apply { this.account = account }.build()

/**
 * Creates a TrustSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun trustSet(block: TrustSetBuilder.() -> Unit): XrplTransaction.Unsigned =
    TrustSetBuilder().apply(block).build()

/**
 * Creates a TrustSet [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun trustSet(
    account: Address,
    block: TrustSetBuilder.() -> Unit,
): XrplTransaction.Unsigned = TrustSetBuilder().apply(block).apply { this.account = account }.build()

/**
 * Creates an AccountSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun accountSet(block: AccountSetBuilder.() -> Unit): XrplTransaction.Unsigned =
    AccountSetBuilder().apply(block).build()

/**
 * Creates an AccountSet [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun accountSet(
    account: Address,
    block: AccountSetBuilder.() -> Unit,
): XrplTransaction.Unsigned = AccountSetBuilder().apply(block).apply { this.account = account }.build()

/**
 * Creates an AccountDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun accountDelete(block: AccountDeleteBuilder.() -> Unit): XrplTransaction.Unsigned =
    AccountDeleteBuilder().apply(block).build()

/**
 * Creates a SetRegularKey [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun setRegularKey(block: SetRegularKeyBuilder.() -> Unit): XrplTransaction.Unsigned =
    SetRegularKeyBuilder().apply(block).build()

/**
 * Creates a SetRegularKey [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun setRegularKey(
    account: Address,
    block: SetRegularKeyBuilder.() -> Unit,
): XrplTransaction.Unsigned = SetRegularKeyBuilder().apply(block).apply { this.account = account }.build()

/**
 * Creates a SignerListSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun signerListSet(block: SignerListSetBuilder.() -> Unit): XrplTransaction.Unsigned =
    SignerListSetBuilder().apply(block).build()

/**
 * Creates a SignerListSet [XrplTransaction.Unsigned] using the DSL builder with [account] as a function parameter.
 */
public fun signerListSet(
    account: Address,
    block: SignerListSetBuilder.() -> Unit,
): XrplTransaction.Unsigned = SignerListSetBuilder().apply(block).apply { this.account = account }.build()
