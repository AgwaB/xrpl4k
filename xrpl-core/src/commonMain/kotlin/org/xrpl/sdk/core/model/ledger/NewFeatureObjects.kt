package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * A delegation of signing authority from one account to another.
 *
 * This is an experimental ledger object type.
 *
 * @property account The address of the delegating account.
 * @property authorize The address of the delegated account.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
@ExperimentalXrplApi
public class Delegate(
    override val index: Hash256,
    public val account: Address,
    public val authorize: Address,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Delegate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Delegate) return false
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

    override fun toString(): String = "Delegate(account=$account, authorize=$authorize)"
}

/**
 * A yield-bearing vault that holds deposited assets.
 *
 * This is an experimental ledger object type.
 *
 * @property owner The address of the vault owner.
 * @property asset The asset held in this vault.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags.
 */
@ExperimentalXrplApi
public class Vault(
    override val index: Hash256,
    public val owner: Address,
    public val asset: CurrencyAmount? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Vault

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vault) return false
        return index == other.index &&
            owner == other.owner &&
            asset == other.asset &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + (asset?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Vault(owner=$owner)"
}

/**
 * An active loan within a lending protocol.
 *
 * This is an experimental ledger object type.
 *
 * @property account The address of the borrower.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags.
 */
@ExperimentalXrplApi
public class Loan(
    override val index: Hash256,
    public val account: Address,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Loan

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Loan) return false
        return index == other.index &&
            account == other.account &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Loan(account=$account)"
}

/**
 * A broker managing loans within a lending protocol.
 *
 * This is an experimental ledger object type.
 *
 * @property account The address of the loan broker.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags.
 */
@ExperimentalXrplApi
public class LoanBroker(
    override val index: Hash256,
    public val account: Address,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.LoanBroker

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoanBroker) return false
        return index == other.index &&
            account == other.account &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "LoanBroker(account=$account)"
}
