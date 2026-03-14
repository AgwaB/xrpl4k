package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * Identifies a cross-chain bridge by its door accounts and currency.
 *
 * @property lockingChainDoor The door account on the locking chain.
 * @property lockingChainIssue The currency on the locking chain (encoded as amount).
 * @property issuingChainDoor The door account on the issuing chain.
 * @property issuingChainIssue The currency on the issuing chain (encoded as amount).
 */
public class XChainBridge(
    public val lockingChainDoor: Address,
    public val lockingChainIssue: CurrencyAmount,
    public val issuingChainDoor: Address,
    public val issuingChainIssue: CurrencyAmount,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainBridge) return false
        return lockingChainDoor == other.lockingChainDoor &&
            lockingChainIssue == other.lockingChainIssue &&
            issuingChainDoor == other.issuingChainDoor &&
            issuingChainIssue == other.issuingChainIssue
    }

    override fun hashCode(): Int {
        var result = lockingChainDoor.hashCode()
        result = 31 * result + lockingChainIssue.hashCode()
        result = 31 * result + issuingChainDoor.hashCode()
        result = 31 * result + issuingChainIssue.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainBridge(lockingChainDoor=$lockingChainDoor, issuingChainDoor=$issuingChainDoor)"
}

/**
 * A cross-chain bridge connecting two XRPL networks.
 *
 * @property account The address of the bridge owner on this chain.
 * @property xchainBridge The bridge specification identifying both sides.
 * @property signatureReward The reward paid to attestation signers.
 * @property minAccountCreateAmount Minimum amount required to create an account via the bridge.
 * @property xchainClaimID The next available claim ID.
 * @property xchainAccountClaimCount The number of account-creation claims processed.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class Bridge(
    override val index: Hash256,
    public val account: Address,
    public val xchainBridge: XChainBridge,
    public val signatureReward: CurrencyAmount,
    public val minAccountCreateAmount: CurrencyAmount? = null,
    public val xchainClaimID: UInt? = null,
    public val xchainAccountClaimCount: UInt? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Bridge

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bridge) return false
        return index == other.index &&
            account == other.account &&
            xchainBridge == other.xchainBridge &&
            signatureReward == other.signatureReward &&
            minAccountCreateAmount == other.minAccountCreateAmount &&
            xchainClaimID == other.xchainClaimID &&
            xchainAccountClaimCount == other.xchainAccountClaimCount &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + xchainBridge.hashCode()
        result = 31 * result + signatureReward.hashCode()
        result = 31 * result + (minAccountCreateAmount?.hashCode() ?: 0)
        result = 31 * result + (xchainClaimID?.hashCode() ?: 0)
        result = 31 * result + (xchainAccountClaimCount?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Bridge(account=$account, xchainBridge=$xchainBridge)"
}

/**
 * Tracks a pending cross-chain claim waiting for attestations.
 *
 * @property account The address of the account that created this claim.
 * @property xchainBridge The bridge specification.
 * @property xchainClaimID The unique claim ID.
 * @property otherChainSource The source account on the other chain.
 * @property signatureReward The reward for attestation signers.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class XChainOwnedClaimId(
    override val index: Hash256,
    public val account: Address,
    public val xchainBridge: XChainBridge,
    public val xchainClaimID: UInt,
    public val otherChainSource: Address,
    public val signatureReward: CurrencyAmount,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.XChainOwnedClaimID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainOwnedClaimId) return false
        return index == other.index &&
            account == other.account &&
            xchainBridge == other.xchainBridge &&
            xchainClaimID == other.xchainClaimID &&
            otherChainSource == other.otherChainSource &&
            signatureReward == other.signatureReward &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + xchainBridge.hashCode()
        result = 31 * result + xchainClaimID.hashCode()
        result = 31 * result + otherChainSource.hashCode()
        result = 31 * result + signatureReward.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "XChainOwnedClaimId(account=$account, xchainClaimID=$xchainClaimID)"
}

/**
 * Tracks a pending cross-chain account-creation claim.
 *
 * @property account The address of the account that created this claim.
 * @property xchainBridge The bridge specification.
 * @property xchainAccountClaimCount The account-creation claim count.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class XChainOwnedCreateAccountClaimId(
    override val index: Hash256,
    public val account: Address,
    public val xchainBridge: XChainBridge,
    public val xchainAccountClaimCount: UInt,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType
        get() = LedgerObjectType.XChainOwnedCreateAccountClaimID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainOwnedCreateAccountClaimId) return false
        return index == other.index &&
            account == other.account &&
            xchainBridge == other.xchainBridge &&
            xchainAccountClaimCount == other.xchainAccountClaimCount &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + xchainBridge.hashCode()
        result = 31 * result + xchainAccountClaimCount.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainOwnedCreateAccountClaimId(account=$account, xchainAccountClaimCount=$xchainAccountClaimCount)"
}
