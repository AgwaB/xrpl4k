package org.xrpl.sdk.core.model.transaction

import kotlin.jvm.JvmInline

/**
 * Represents an XRPL transaction type as a string value.
 *
 * This is an inline value class wrapping a [String] so it carries zero runtime overhead
 * compared to a bare string while still providing type-safety at compile time.
 *
 * No validation is performed on the wrapped value to ensure forward compatibility
 * with new transaction types introduced by future amendments.
 *
 * Named constants for all known transaction types are available in the companion object,
 * grouped by functional category.
 */
@JvmInline
public value class TransactionType(public val value: String) {
    public companion object {
        // Account Management
        public val AccountSet: TransactionType = TransactionType("AccountSet")
        public val AccountDelete: TransactionType = TransactionType("AccountDelete")
        public val SetRegularKey: TransactionType = TransactionType("SetRegularKey")
        public val SignerListSet: TransactionType = TransactionType("SignerListSet")
        public val DepositPreauth: TransactionType = TransactionType("DepositPreauth")

        // Payment
        public val Payment: TransactionType = TransactionType("Payment")

        // Trust & Tokens
        public val TrustSet: TransactionType = TransactionType("TrustSet")
        public val Clawback: TransactionType = TransactionType("Clawback")

        // DEX
        public val OfferCreate: TransactionType = TransactionType("OfferCreate")
        public val OfferCancel: TransactionType = TransactionType("OfferCancel")

        // AMM
        public val AMMCreate: TransactionType = TransactionType("AMMCreate")
        public val AMMDeposit: TransactionType = TransactionType("AMMDeposit")
        public val AMMWithdraw: TransactionType = TransactionType("AMMWithdraw")
        public val AMMVote: TransactionType = TransactionType("AMMVote")
        public val AMMBid: TransactionType = TransactionType("AMMBid")
        public val AMMDelete: TransactionType = TransactionType("AMMDelete")
        public val AMMClawback: TransactionType = TransactionType("AMMClawback")

        // Escrow
        public val EscrowCreate: TransactionType = TransactionType("EscrowCreate")
        public val EscrowFinish: TransactionType = TransactionType("EscrowFinish")
        public val EscrowCancel: TransactionType = TransactionType("EscrowCancel")

        // Payment Channels
        public val PaymentChannelCreate: TransactionType = TransactionType("PaymentChannelCreate")
        public val PaymentChannelFund: TransactionType = TransactionType("PaymentChannelFund")
        public val PaymentChannelClaim: TransactionType = TransactionType("PaymentChannelClaim")

        // Check
        public val CheckCreate: TransactionType = TransactionType("CheckCreate")
        public val CheckCash: TransactionType = TransactionType("CheckCash")
        public val CheckCancel: TransactionType = TransactionType("CheckCancel")

        // NFT
        public val NFTokenMint: TransactionType = TransactionType("NFTokenMint")
        public val NFTokenBurn: TransactionType = TransactionType("NFTokenBurn")
        public val NFTokenCreateOffer: TransactionType = TransactionType("NFTokenCreateOffer")
        public val NFTokenCancelOffer: TransactionType = TransactionType("NFTokenCancelOffer")
        public val NFTokenAcceptOffer: TransactionType = TransactionType("NFTokenAcceptOffer")
        public val NFTokenModify: TransactionType = TransactionType("NFTokenModify")

        // MPT (Multi-Purpose Token)
        public val MPTokenIssuanceCreate: TransactionType = TransactionType("MPTokenIssuanceCreate")
        public val MPTokenIssuanceDestroy: TransactionType = TransactionType("MPTokenIssuanceDestroy")
        public val MPTokenIssuanceSet: TransactionType = TransactionType("MPTokenIssuanceSet")
        public val MPTokenAuthorize: TransactionType = TransactionType("MPTokenAuthorize")

        // DID
        public val DIDSet: TransactionType = TransactionType("DIDSet")
        public val DIDDelete: TransactionType = TransactionType("DIDDelete")

        // Oracle
        public val OracleSet: TransactionType = TransactionType("OracleSet")
        public val OracleDelete: TransactionType = TransactionType("OracleDelete")

        // Credential
        public val CredentialCreate: TransactionType = TransactionType("CredentialCreate")
        public val CredentialAccept: TransactionType = TransactionType("CredentialAccept")
        public val CredentialDelete: TransactionType = TransactionType("CredentialDelete")

        // Ticket
        public val TicketCreate: TransactionType = TransactionType("TicketCreate")

        // XChain (Cross-chain bridges)
        public val XChainCreateBridge: TransactionType = TransactionType("XChainCreateBridge")
        public val XChainModifyBridge: TransactionType = TransactionType("XChainModifyBridge")
        public val XChainCreateClaimID: TransactionType = TransactionType("XChainCreateClaimID")
        public val XChainCommit: TransactionType = TransactionType("XChainCommit")
        public val XChainClaim: TransactionType = TransactionType("XChainClaim")
        public val XChainAccountCreateCommit: TransactionType = TransactionType("XChainAccountCreateCommit")
        public val XChainAddClaimAttestation: TransactionType = TransactionType("XChainAddClaimAttestation")
        public val XChainAddAccountCreateAttestation: TransactionType =
            TransactionType("XChainAddAccountCreateAttestation")

        // PermissionedDomain
        public val PermissionedDomainSet: TransactionType = TransactionType("PermissionedDomainSet")
        public val PermissionedDomainDelete: TransactionType = TransactionType("PermissionedDomainDelete")

        // Batch
        public val Batch: TransactionType = TransactionType("Batch")

        // Delegate
        public val DelegateSet: TransactionType = TransactionType("DelegateSet")

        // Vault
        public val VaultCreate: TransactionType = TransactionType("VaultCreate")
        public val VaultSet: TransactionType = TransactionType("VaultSet")
        public val VaultDelete: TransactionType = TransactionType("VaultDelete")
        public val VaultDeposit: TransactionType = TransactionType("VaultDeposit")
        public val VaultWithdraw: TransactionType = TransactionType("VaultWithdraw")
        public val VaultClawback: TransactionType = TransactionType("VaultClawback")

        // Loan
        public val LoanSet: TransactionType = TransactionType("LoanSet")
        public val LoanDelete: TransactionType = TransactionType("LoanDelete")
        public val LoanManage: TransactionType = TransactionType("LoanManage")
        public val LoanPay: TransactionType = TransactionType("LoanPay")
        public val LoanBrokerSet: TransactionType = TransactionType("LoanBrokerSet")
        public val LoanBrokerDelete: TransactionType = TransactionType("LoanBrokerDelete")
        public val LoanBrokerCoverDeposit: TransactionType = TransactionType("LoanBrokerCoverDeposit")
        public val LoanBrokerCoverWithdraw: TransactionType = TransactionType("LoanBrokerCoverWithdraw")
        public val LoanBrokerCoverClawback: TransactionType = TransactionType("LoanBrokerCoverClawback")

        // Pseudo-transactions
        public val EnableAmendment: TransactionType = TransactionType("EnableAmendment")
        public val SetFee: TransactionType = TransactionType("SetFee")
        public val UNLModify: TransactionType = TransactionType("UNLModify")
        public val LedgerStateFix: TransactionType = TransactionType("LedgerStateFix")

        // Invalid / sentinel
        public val Invalid: TransactionType = TransactionType("Invalid")
    }
}
