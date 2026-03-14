package org.xrpl.sdk.core.model.transaction

/**
 * Creates a Payment [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun payment(block: PaymentBuilder.() -> Unit): XrplTransaction.Unsigned = PaymentBuilder().apply(block).build()

/**
 * Creates an OfferCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun offerCreate(block: OfferCreateBuilder.() -> Unit): XrplTransaction.Unsigned =
    OfferCreateBuilder().apply(block).build()

/**
 * Creates an OfferCancel [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun offerCancel(block: OfferCancelBuilder.() -> Unit): XrplTransaction.Unsigned =
    OfferCancelBuilder().apply(block).build()

/**
 * Creates a TrustSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun trustSet(block: TrustSetBuilder.() -> Unit): XrplTransaction.Unsigned =
    TrustSetBuilder().apply(block).build()

/**
 * Creates an AccountSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun accountSet(block: AccountSetBuilder.() -> Unit): XrplTransaction.Unsigned =
    AccountSetBuilder().apply(block).build()

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
 * Creates a SignerListSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun signerListSet(block: SignerListSetBuilder.() -> Unit): XrplTransaction.Unsigned =
    SignerListSetBuilder().apply(block).build()
