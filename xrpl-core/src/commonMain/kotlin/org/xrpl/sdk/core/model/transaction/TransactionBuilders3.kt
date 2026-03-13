package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.type.Address

// ── T10a Factory Functions ────────────────────────────────────────────────────

/**
 * Creates a DIDSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun didSet(
    account: Address,
    block: DIDSetBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = DIDSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.DIDSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a DIDDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun didDelete(
    account: Address,
    block: DIDDeleteBuilder.() -> Unit = {},
): XrplTransaction.Unsigned {
    val fields = DIDDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.DIDDelete,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an OracleSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun oracleSet(
    account: Address,
    block: OracleSetBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = OracleSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.OracleSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an OracleDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun oracleDelete(
    account: Address,
    block: OracleDeleteBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = OracleDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.OracleDelete,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an MPTokenIssuanceCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun mpTokenIssuanceCreate(
    account: Address,
    block: MPTokenIssuanceCreateBuilder.() -> Unit = {},
): XrplTransaction.Unsigned {
    val fields = MPTokenIssuanceCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.MPTokenIssuanceCreate,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an MPTokenIssuanceDestroy [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun mpTokenIssuanceDestroy(
    account: Address,
    block: MPTokenIssuanceDestroyBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = MPTokenIssuanceDestroyBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.MPTokenIssuanceDestroy,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an MPTokenIssuanceSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun mpTokenIssuanceSet(
    account: Address,
    block: MPTokenIssuanceSetBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = MPTokenIssuanceSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.MPTokenIssuanceSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an MPTokenAuthorize [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun mpTokenAuthorize(
    account: Address,
    block: MPTokenAuthorizeBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = MPTokenAuthorizeBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.MPTokenAuthorize,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a CredentialCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun credentialCreate(
    account: Address,
    block: CredentialCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = CredentialCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.CredentialCreate,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a CredentialAccept [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun credentialAccept(
    account: Address,
    block: CredentialAcceptBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = CredentialAcceptBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.CredentialAccept,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a CredentialDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun credentialDelete(
    account: Address,
    block: CredentialDeleteBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = CredentialDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.CredentialDelete,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a TicketCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun ticketCreate(
    account: Address,
    block: TicketCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = TicketCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.TicketCreate,
        account = account,
        fields = fields,
    )
}

// ── T10b XChain Factory Functions (live on mainnet, no @ExperimentalXrplApi) ─

/**
 * Creates an XChainCreateBridge [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainCreateBridge(
    account: Address,
    block: XChainCreateBridgeBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainCreateBridgeBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainCreateBridge,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainModifyBridge [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainModifyBridge(
    account: Address,
    block: XChainModifyBridgeBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainModifyBridgeBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainModifyBridge,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainCreateClaimID [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainCreateClaimID(
    account: Address,
    block: XChainCreateClaimIDBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainCreateClaimIDBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainCreateClaimID,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainCommit [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainCommit(
    account: Address,
    block: XChainCommitBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainCommitBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainCommit,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainClaim [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainClaim(
    account: Address,
    block: XChainClaimBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainClaimBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainClaim,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainAccountCreateCommit [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainAccountCreateCommit(
    account: Address,
    block: XChainAccountCreateCommitBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainAccountCreateCommitBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainAccountCreateCommit,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainAddClaimAttestation [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainAddClaimAttestation(
    account: Address,
    block: XChainAddClaimAttestationBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainAddClaimAttestationBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainAddClaimAttestation,
        account = account,
        fields = fields,
    )
}

/**
 * Creates an XChainAddAccountCreateAttestation [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun xChainAddAccountCreateAttestation(
    account: Address,
    block: XChainAddAccountCreateAttestationBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = XChainAddAccountCreateAttestationBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.XChainAddAccountCreateAttestation,
        account = account,
        fields = fields,
    )
}

// ── T10b PermissionedDomain Factory Functions (live on mainnet) ───────────────

/**
 * Creates a PermissionedDomainSet [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun permissionedDomainSet(
    account: Address,
    block: PermissionedDomainSetBuilder.() -> Unit = {},
): XrplTransaction.Unsigned {
    val fields = PermissionedDomainSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.PermissionedDomainSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a PermissionedDomainDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
public fun permissionedDomainDelete(
    account: Address,
    block: PermissionedDomainDeleteBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = PermissionedDomainDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.PermissionedDomainDelete,
        account = account,
        fields = fields,
    )
}

// ── T10b Experimental Factory Functions ───────────────────────────────────────

/**
 * Creates a VaultCreate [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun vaultCreate(
    account: Address,
    block: VaultCreateBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = VaultCreateBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.VaultCreate,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a VaultSet [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun vaultSet(
    account: Address,
    block: VaultSetBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = VaultSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.VaultSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a VaultDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun vaultDelete(
    account: Address,
    block: VaultDeleteBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = VaultDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.VaultDelete,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a VaultDeposit [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun vaultDeposit(
    account: Address,
    block: VaultDepositBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = VaultDepositBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.VaultDeposit,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a VaultWithdraw [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun vaultWithdraw(
    account: Address,
    block: VaultWithdrawBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = VaultWithdrawBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.VaultWithdraw,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a VaultClawback [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun vaultClawback(
    account: Address,
    block: VaultClawbackBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = VaultClawbackBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.VaultClawback,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanSet [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanSet(
    account: Address,
    block: LoanSetBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanDelete(
    account: Address,
    block: LoanDeleteBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanDelete,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanManage [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanManage(
    account: Address,
    block: LoanManageBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanManageBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanManage,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanPay [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanPay(
    account: Address,
    block: LoanPayBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanPayBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanPay,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanBrokerSet [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanBrokerSet(
    account: Address,
    block: LoanBrokerSetBuilder.() -> Unit = {},
): XrplTransaction.Unsigned {
    val fields = LoanBrokerSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanBrokerSet,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanBrokerDelete [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanBrokerDelete(
    account: Address,
    block: LoanBrokerDeleteBuilder.() -> Unit = {},
): XrplTransaction.Unsigned {
    val fields = LoanBrokerDeleteBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanBrokerDelete,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanBrokerCoverDeposit [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanBrokerCoverDeposit(
    account: Address,
    block: LoanBrokerCoverDepositBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanBrokerCoverDepositBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanBrokerCoverDeposit,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanBrokerCoverWithdraw [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanBrokerCoverWithdraw(
    account: Address,
    block: LoanBrokerCoverWithdrawBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanBrokerCoverWithdrawBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanBrokerCoverWithdraw,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a LoanBrokerCoverClawback [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun loanBrokerCoverClawback(
    account: Address,
    block: LoanBrokerCoverClawbackBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = LoanBrokerCoverClawbackBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.LoanBrokerCoverClawback,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a Batch [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun batch(
    account: Address,
    block: BatchBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = BatchBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.Batch,
        account = account,
        fields = fields,
    )
}

/**
 * Creates a DelegateSet [XrplTransaction.Unsigned] using the DSL builder.
 */
@ExperimentalXrplApi
public fun delegateSet(
    account: Address,
    block: DelegateSetBuilder.() -> Unit,
): XrplTransaction.Unsigned {
    val fields = DelegateSetBuilder().apply(block).build()
    return XrplTransaction.Unsigned(
        transactionType = TransactionType.DelegateSet,
        account = account,
        fields = fields,
    )
}
