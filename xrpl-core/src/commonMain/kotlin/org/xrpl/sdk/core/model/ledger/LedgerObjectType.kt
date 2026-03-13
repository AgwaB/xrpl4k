package org.xrpl.sdk.core.model.ledger

import kotlin.jvm.JvmInline

/**
 * Identifies the type of an XRPL ledger object as used in the `LedgerEntryType` field.
 *
 * This is a value class wrapping a [String] so that any future ledger entry type introduced
 * by amendments is supported without requiring an SDK update. All well-known types are
 * available as constants on the companion object.
 *
 * No validation is applied to [value] — arbitrary strings are accepted for forward compatibility.
 *
 * @param value The raw ledger entry type string, e.g. `"AccountRoot"`.
 */
@JvmInline
public value class LedgerObjectType(public val value: String) {
    public companion object {
        // Account management

        /** Root account object that holds XRP balance and account settings. */
        public val AccountRoot: LedgerObjectType = LedgerObjectType("AccountRoot")

        /** A list of signers authorised to multi-sign transactions for an account. */
        public val SignerList: LedgerObjectType = LedgerObjectType("SignerList")

        /** An internal node in the ownership directory tree. */
        public val DirectoryNode: LedgerObjectType = LedgerObjectType("DirectoryNode")

        /** A sequence-number placeholder that can be consumed later by a transaction. */
        public val Ticket: LedgerObjectType = LedgerObjectType("Ticket")

        // DEX

        /** An open offer to exchange currencies on the decentralised exchange. */
        public val Offer: LedgerObjectType = LedgerObjectType("Offer")

        /** A trust line between two accounts for a non-XRP currency. */
        public val RippleState: LedgerObjectType = LedgerObjectType("RippleState")

        // Payment objects

        /** Funds held in escrow pending a time or condition release. */
        public val Escrow: LedgerObjectType = LedgerObjectType("Escrow")

        /** A unidirectional payment channel for off-ledger micro-payments. */
        public val PayChannel: LedgerObjectType = LedgerObjectType("PayChannel")

        /** A deferred payment that the recipient must explicitly cash. */
        public val Check: LedgerObjectType = LedgerObjectType("Check")

        /** An account's pre-authorisation of an incoming deposit. */
        public val DepositPreauth: LedgerObjectType = LedgerObjectType("DepositPreauth")

        // NFT

        /** A page in the ledger that stores up to 32 NFTokens. */
        public val NFTokenPage: LedgerObjectType = LedgerObjectType("NFTokenPage")

        /** An open offer to buy or sell an NFToken. */
        public val NFTokenOffer: LedgerObjectType = LedgerObjectType("NFTokenOffer")

        // AMM

        /** An Automated Market Maker instance for a currency pair. */
        public val AMM: LedgerObjectType = LedgerObjectType("AMM")

        // MPT

        /** A holder's balance of a Multi-Purpose Token issuance. */
        public val MPToken: LedgerObjectType = LedgerObjectType("MPToken")

        /** The definition and metadata for a Multi-Purpose Token issuance. */
        public val MPTokenIssuance: LedgerObjectType = LedgerObjectType("MPTokenIssuance")

        // Identity

        /** A Decentralised Identifier document anchored on the ledger. */
        public val DID: LedgerObjectType = LedgerObjectType("DID")

        /** A verifiable credential issued to an XRPL account. */
        public val Credential: LedgerObjectType = LedgerObjectType("Credential")

        // Governance

        /** Tracks which amendments are enabled or pending. */
        public val Amendments: LedgerObjectType = LedgerObjectType("Amendments")

        /** Current transaction fee settings used by the network. */
        public val FeeSettings: LedgerObjectType = LedgerObjectType("FeeSettings")

        /** A list of prior ledger hashes used for validation. */
        public val LedgerHashes: LedgerObjectType = LedgerObjectType("LedgerHashes")

        /** Tracks validators temporarily removed from the UNL. */
        public val NegativeUNL: LedgerObjectType = LedgerObjectType("NegativeUNL")

        // Bridge

        /** A cross-chain bridge connecting two XRPL networks. */
        public val Bridge: LedgerObjectType = LedgerObjectType("Bridge")

        /** Tracks a pending cross-chain claim waiting for attestations. */
        public val XChainOwnedClaimID: LedgerObjectType = LedgerObjectType("XChainOwnedClaimID")

        /** Tracks a pending cross-chain account-creation claim. */
        public val XChainOwnedCreateAccountClaimID: LedgerObjectType =
            LedgerObjectType("XChainOwnedCreateAccountClaimID")

        // Oracle

        /** A price oracle providing off-chain data to the ledger. */
        public val Oracle: LedgerObjectType = LedgerObjectType("Oracle")

        // Newer features

        /** A permissioned domain that restricts credential-based operations. */
        public val PermissionedDomain: LedgerObjectType = LedgerObjectType("PermissionedDomain")

        /** A delegation of signing authority from one account to another. */
        public val Delegate: LedgerObjectType = LedgerObjectType("Delegate")

        /** A yield-bearing vault that holds deposited assets. */
        public val Vault: LedgerObjectType = LedgerObjectType("Vault")

        /** An active loan within a lending protocol. */
        public val Loan: LedgerObjectType = LedgerObjectType("Loan")

        /** A broker managing loans within a lending protocol. */
        public val LoanBroker: LedgerObjectType = LedgerObjectType("LoanBroker")
    }
}
