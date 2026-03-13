package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.type.Address

// ── T9 Factory Functions ──────────────────────────────────────────────────────
// These builders return Fields objects, so factory functions wrap them and
// construct XrplTransaction.Unsigned with the given account.

/**
 * Creates an EscrowCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun escrowCreate(
    account: Address,
    block: EscrowCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = EscrowCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.EscrowCreate,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an EscrowFinish [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun escrowFinish(
    account: Address,
    block: EscrowFinishBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = EscrowFinishBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.EscrowFinish,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an EscrowCancel [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun escrowCancel(
    account: Address,
    block: EscrowCancelBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = EscrowCancelBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.EscrowCancel,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a PaymentChannelCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun paymentChannelCreate(
    account: Address,
    block: PaymentChannelCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = PaymentChannelCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.PaymentChannelCreate,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a PaymentChannelFund [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun paymentChannelFund(
    account: Address,
    block: PaymentChannelFundBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = PaymentChannelFundBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.PaymentChannelFund,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a PaymentChannelClaim [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun paymentChannelClaim(
    account: Address,
    block: PaymentChannelClaimBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = PaymentChannelClaimBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.PaymentChannelClaim,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a CheckCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun checkCreate(
    account: Address,
    block: CheckCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = CheckCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.CheckCreate,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a CheckCash [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun checkCash(
    account: Address,
    block: CheckCashBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = CheckCashBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.CheckCash,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a CheckCancel [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun checkCancel(
    account: Address,
    block: CheckCancelBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = CheckCancelBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.CheckCancel,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an NFTokenMint [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun nfTokenMint(
    account: Address,
    block: NFTokenMintBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = NFTokenMintBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.NFTokenMint,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an NFTokenBurn [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun nfTokenBurn(
    account: Address,
    block: NFTokenBurnBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = NFTokenBurnBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.NFTokenBurn,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an NFTokenCreateOffer [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun nfTokenCreateOffer(
    account: Address,
    block: NFTokenCreateOfferBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = NFTokenCreateOfferBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.NFTokenCreateOffer,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an NFTokenCancelOffer [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun nfTokenCancelOffer(
    account: Address,
    block: NFTokenCancelOfferBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = NFTokenCancelOfferBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.NFTokenCancelOffer,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an NFTokenAcceptOffer [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun nfTokenAcceptOffer(
    account: Address,
    block: NFTokenAcceptOfferBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = NFTokenAcceptOfferBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.NFTokenAcceptOffer,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an NFTokenModify [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun nfTokenModify(
    account: Address,
    block: NFTokenModifyBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = NFTokenModifyBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.NFTokenModify,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a DepositPreauth [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun depositPreauth(
    account: Address,
    block: DepositPreauthBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = DepositPreauthBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.DepositPreauth,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a Clawback [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun clawback(
    account: Address,
    block: ClawbackBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = ClawbackBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.Clawback,
        account = account,
        fields = fields,
    )
}
