package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * A holder's balance of a Multi-Purpose Token issuance.
 *
 * @property account The address of the account holding this MPToken.
 * @property mptIssuanceID The ID of the MPToken issuance (192-bit hex).
 * @property mptAmount The amount of MPT held by this account.
 * @property lockedAmount The amount of MPT locked (e.g., in escrow).
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (e.g., authorised, frozen).
 */
public class MpToken(
    override val index: Hash256,
    public val account: Address,
    public val mptIssuanceID: String,
    public val mptAmount: Long = 0L,
    public val lockedAmount: Long? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.MPToken

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MpToken) return false
        return index == other.index &&
            account == other.account &&
            mptIssuanceID == other.mptIssuanceID &&
            mptAmount == other.mptAmount &&
            lockedAmount == other.lockedAmount &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + mptIssuanceID.hashCode()
        result = 31 * result + mptAmount.hashCode()
        result = 31 * result + (lockedAmount?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "MpToken(account=$account, mptIssuanceID=$mptIssuanceID, mptAmount=$mptAmount)"
}

/**
 * The definition and metadata for a Multi-Purpose Token issuance.
 *
 * @property issuer The address of the account that issued this MPT.
 * @property sequence The sequence number of the transaction that created this issuance.
 * @property maxAmount The maximum number of tokens that can exist (null if unlimited).
 * @property outstandingAmount The current total tokens in circulation.
 * @property transferFee The transfer fee in basis points (0-50000).
 * @property assetScale The decimal exponent for display purposes.
 * @property metadata Arbitrary hex-encoded metadata associated with this issuance.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (e.g., canLock, requireAuth, canTransfer).
 */
public class MpTokenIssuance(
    override val index: Hash256,
    public val issuer: Address,
    public val sequence: UInt,
    public val maxAmount: Long? = null,
    public val outstandingAmount: Long = 0L,
    public val transferFee: UInt? = null,
    public val assetScale: UInt? = null,
    public val metadata: String? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.MPTokenIssuance

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MpTokenIssuance) return false
        return index == other.index &&
            issuer == other.issuer &&
            sequence == other.sequence &&
            maxAmount == other.maxAmount &&
            outstandingAmount == other.outstandingAmount &&
            transferFee == other.transferFee &&
            assetScale == other.assetScale &&
            metadata == other.metadata &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + (maxAmount?.hashCode() ?: 0)
        result = 31 * result + outstandingAmount.hashCode()
        result = 31 * result + (transferFee?.hashCode() ?: 0)
        result = 31 * result + (assetScale?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "MpTokenIssuance(issuer=$issuer, outstandingAmount=$outstandingAmount)"
}
