package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.XrpDrops

/**
 * The root account object that holds XRP balance and account settings.
 *
 * Every funded account on the XRPL has exactly one `AccountRoot` object in the ledger.
 *
 * @property account The classic address of this account.
 * @property balance The account's current XRP balance in drops.
 * @property sequence The next valid sequence number for this account.
 * @property ownerCount The number of objects this account owns in the ledger.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags for account settings (e.g., requireDest, disallowXRP).
 * @property accountTxnID Hash of the last transaction sent by this account (if enabled).
 * @property domain The domain associated with this account (hex-encoded).
 * @property emailHash MD5 hash of the account owner's email (deprecated, hex-encoded).
 * @property messageKey A public key for encrypted messaging (hex-encoded).
 * @property regularKey Address of a regular key pair authorised to sign transactions.
 * @property ticketCount Number of open tickets owned by this account.
 * @property tickSize Tick size for offers placed by this account on the DEX.
 * @property transferRate Transfer rate for issued currencies (0 means no fee, >1000000000 is fee).
 * @property walletLocator Arbitrary 256-bit value set by the account owner.
 * @property walletSize Unused legacy field.
 * @property nfTokenMinter Address authorised to mint NFTs on behalf of this account.
 * @property mintedNFTokens Total number of NFTs minted by this account.
 * @property burnedNFTokens Total number of NFTs burned by this account.
 * @property firstNFTokenSequence Sequence number of the first NFT this account minted.
 * @property ammID The AMM ledger object ID if this is an AMM account.
 */
public class AccountRoot(
    override val index: Hash256,
    public val account: Address,
    public val balance: XrpDrops,
    public val sequence: UInt,
    public val ownerCount: UInt,
    public val previousTxnID: Hash256,
    public val previousTxnLgrSeq: UInt,
    public val flags: UInt = 0u,
    public val accountTxnID: Hash256? = null,
    public val domain: String? = null,
    public val emailHash: String? = null,
    public val messageKey: String? = null,
    public val regularKey: Address? = null,
    public val ticketCount: UInt? = null,
    public val tickSize: UInt? = null,
    public val transferRate: UInt? = null,
    public val walletLocator: Hash256? = null,
    public val walletSize: UInt? = null,
    public val nfTokenMinter: Address? = null,
    public val mintedNFTokens: UInt? = null,
    public val burnedNFTokens: UInt? = null,
    public val firstNFTokenSequence: UInt? = null,
    public val ammID: Hash256? = null,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.AccountRoot

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountRoot) return false
        return index == other.index &&
            account == other.account &&
            balance == other.balance &&
            sequence == other.sequence &&
            ownerCount == other.ownerCount &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags &&
            accountTxnID == other.accountTxnID &&
            domain == other.domain &&
            emailHash == other.emailHash &&
            messageKey == other.messageKey &&
            regularKey == other.regularKey &&
            ticketCount == other.ticketCount &&
            tickSize == other.tickSize &&
            transferRate == other.transferRate &&
            walletLocator == other.walletLocator &&
            walletSize == other.walletSize &&
            nfTokenMinter == other.nfTokenMinter &&
            mintedNFTokens == other.mintedNFTokens &&
            burnedNFTokens == other.burnedNFTokens &&
            firstNFTokenSequence == other.firstNFTokenSequence &&
            ammID == other.ammID
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + ownerCount.hashCode()
        result = 31 * result + previousTxnID.hashCode()
        result = 31 * result + previousTxnLgrSeq.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (accountTxnID?.hashCode() ?: 0)
        result = 31 * result + (domain?.hashCode() ?: 0)
        result = 31 * result + (emailHash?.hashCode() ?: 0)
        result = 31 * result + (messageKey?.hashCode() ?: 0)
        result = 31 * result + (regularKey?.hashCode() ?: 0)
        result = 31 * result + (ticketCount?.hashCode() ?: 0)
        result = 31 * result + (tickSize?.hashCode() ?: 0)
        result = 31 * result + (transferRate?.hashCode() ?: 0)
        result = 31 * result + (walletLocator?.hashCode() ?: 0)
        result = 31 * result + (walletSize?.hashCode() ?: 0)
        result = 31 * result + (nfTokenMinter?.hashCode() ?: 0)
        result = 31 * result + (mintedNFTokens?.hashCode() ?: 0)
        result = 31 * result + (burnedNFTokens?.hashCode() ?: 0)
        result = 31 * result + (firstNFTokenSequence?.hashCode() ?: 0)
        result = 31 * result + (ammID?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountRoot(account=$account, balance=$balance, sequence=$sequence, ownerCount=$ownerCount)"
}

/**
 * A single entry in a [SignerList].
 *
 * @property account The address of this signer.
 * @property signerWeight The weight of this signer's signature.
 * @property walletLocator An optional 256-bit identifier for this signer.
 */
public class SignerEntry(
    public val account: Address,
    public val signerWeight: UInt,
    public val walletLocator: Hash256? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignerEntry) return false
        return account == other.account &&
            signerWeight == other.signerWeight &&
            walletLocator == other.walletLocator
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + signerWeight.hashCode()
        result = 31 * result + (walletLocator?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "SignerEntry(account=$account, signerWeight=$signerWeight)"
}

/**
 * A list of signers authorised to multi-sign transactions for an account.
 *
 * @property signerQuorum The minimum total weight required to authorise a transaction.
 * @property signerEntries The list of authorised signers.
 * @property signerListID The ID of this signer list (currently always 0).
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved for future use).
 */
public class SignerList(
    override val index: Hash256,
    public val signerQuorum: UInt,
    public val signerEntries: List<SignerEntry>,
    public val signerListID: UInt = 0u,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.SignerList

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignerList) return false
        return index == other.index &&
            signerQuorum == other.signerQuorum &&
            signerEntries == other.signerEntries &&
            signerListID == other.signerListID &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + signerQuorum.hashCode()
        result = 31 * result + signerEntries.hashCode()
        result = 31 * result + signerListID.hashCode()
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "SignerList(signerQuorum=$signerQuorum, signerEntries=$signerEntries)"
}
