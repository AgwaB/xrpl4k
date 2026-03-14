package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * A Decentralised Identifier document anchored on the ledger.
 *
 * @property account The address of the account that controls this DID.
 * @property didDocument The DID document content (hex-encoded).
 * @property uri A URI associated with this DID (hex-encoded).
 * @property data Arbitrary data associated with this DID (hex-encoded).
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class Did(
    override val index: Hash256,
    public val account: Address,
    public val didDocument: String? = null,
    public val uri: String? = null,
    public val data: String? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.DID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Did) return false
        return index == other.index &&
            account == other.account &&
            didDocument == other.didDocument &&
            uri == other.uri &&
            data == other.data &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + (didDocument?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Did(account=$account)"
}

/**
 * A verifiable credential issued to an XRPL account.
 *
 * @property subject The address of the account that holds this credential.
 * @property issuer The address of the account that issued this credential.
 * @property credentialType The type of credential (hex-encoded).
 * @property expiration Optional expiration time (seconds since Ripple Epoch).
 * @property uri A URI associated with this credential (hex-encoded).
 * @property ownerNode Hint for the owner directory page.
 * @property issuerNode Hint for the issuer's owner directory page.
 * @property subjectNode Hint for the subject's owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (e.g., accepted).
 */
public class Credential(
    override val index: Hash256,
    public val subject: Address,
    public val issuer: Address,
    public val credentialType: String,
    public val expiration: UInt? = null,
    public val uri: String? = null,
    public val ownerNode: String? = null,
    public val issuerNode: String? = null,
    public val subjectNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Credential

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Credential) return false
        return index == other.index &&
            subject == other.subject &&
            issuer == other.issuer &&
            credentialType == other.credentialType &&
            expiration == other.expiration &&
            uri == other.uri &&
            ownerNode == other.ownerNode &&
            issuerNode == other.issuerNode &&
            subjectNode == other.subjectNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + credentialType.hashCode()
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (issuerNode?.hashCode() ?: 0)
        result = 31 * result + (subjectNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Credential(subject=$subject, issuer=$issuer, credentialType=$credentialType)"
}

/**
 * An account's pre-authorisation of an incoming deposit.
 *
 * @property account The address of the account that granted the pre-authorisation.
 * @property authorize The address of the pre-authorised sender.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class DepositPreauth(
    override val index: Hash256,
    public val account: Address,
    public val authorize: Address,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.DepositPreauth

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DepositPreauth) return false
        return index == other.index &&
            account == other.account &&
            authorize == other.authorize &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + authorize.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "DepositPreauth(account=$account, authorize=$authorize)"
}
