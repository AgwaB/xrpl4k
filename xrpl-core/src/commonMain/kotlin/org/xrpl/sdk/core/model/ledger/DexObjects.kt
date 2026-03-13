package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * An open offer to exchange currencies on the decentralised exchange.
 *
 * @property account The address of the account that placed this offer.
 * @property takerGets The amount the offer creator receives if the offer is consumed.
 * @property takerPays The amount the offer creator pays if the offer is consumed.
 * @property sequence The sequence number of the transaction that created this offer.
 * @property flags Bit-flags (e.g., passive, sell).
 * @property bookDirectory The ID of the order book directory containing this offer.
 * @property bookNode A hint indicating which page of the directory links to this offer.
 * @property ownerNode A hint indicating which page of the owner directory links to this offer.
 * @property expiration Optional expiration time (seconds since Ripple Epoch).
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 */
public class Offer(
    override val index: Hash256,
    public val account: Address,
    public val takerGets: CurrencyAmount,
    public val takerPays: CurrencyAmount,
    public val sequence: UInt,
    public val flags: UInt = 0u,
    public val bookDirectory: Hash256? = null,
    public val bookNode: String? = null,
    public val ownerNode: String? = null,
    public val expiration: UInt? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Offer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Offer) return false
        return index == other.index &&
            account == other.account &&
            takerGets == other.takerGets &&
            takerPays == other.takerPays &&
            sequence == other.sequence &&
            flags == other.flags &&
            bookDirectory == other.bookDirectory &&
            bookNode == other.bookNode &&
            ownerNode == other.ownerNode &&
            expiration == other.expiration &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + takerGets.hashCode()
        result = 31 * result + takerPays.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (bookDirectory?.hashCode() ?: 0)
        result = 31 * result + (bookNode?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Offer(account=$account, takerGets=$takerGets, takerPays=$takerPays, sequence=$sequence)"
}

/**
 * An internal node in a directory tree.
 *
 * Directories organise other ledger objects — either by owner (owner directories) or by
 * order-book price level (offer directories).
 *
 * @property rootIndex The ID of the root page of this directory.
 * @property indexes The list of ledger object IDs contained in this page.
 * @property indexNext Index of the next page in this directory, if any.
 * @property indexPrevious Index of the previous page in this directory, if any.
 * @property owner The owner of this directory (present only for owner directories).
 * @property takerGetsCurrency Currency code of the TakerGets side (offer directories only).
 * @property takerGetsIssuer Issuer of the TakerGets side (offer directories only).
 * @property takerPaysCurrency Currency code of the TakerPays side (offer directories only).
 * @property takerPaysIssuer Issuer of the TakerPays side (offer directories only).
 * @property nfTokenID NFToken ID if this is an NFToken offer directory.
 * @property flags Bit-flags (reserved).
 */
public class DirectoryNode(
    override val index: Hash256,
    public val rootIndex: Hash256,
    public val indexes: List<Hash256> = emptyList(),
    public val indexNext: String? = null,
    public val indexPrevious: String? = null,
    public val owner: Address? = null,
    public val takerGetsCurrency: String? = null,
    public val takerGetsIssuer: String? = null,
    public val takerPaysCurrency: String? = null,
    public val takerPaysIssuer: String? = null,
    public val nfTokenID: Hash256? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.DirectoryNode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DirectoryNode) return false
        return index == other.index &&
            rootIndex == other.rootIndex &&
            indexes == other.indexes &&
            indexNext == other.indexNext &&
            indexPrevious == other.indexPrevious &&
            owner == other.owner &&
            takerGetsCurrency == other.takerGetsCurrency &&
            takerGetsIssuer == other.takerGetsIssuer &&
            takerPaysCurrency == other.takerPaysCurrency &&
            takerPaysIssuer == other.takerPaysIssuer &&
            nfTokenID == other.nfTokenID &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + rootIndex.hashCode()
        result = 31 * result + indexes.hashCode()
        result = 31 * result + (indexNext?.hashCode() ?: 0)
        result = 31 * result + (indexPrevious?.hashCode() ?: 0)
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (takerGetsCurrency?.hashCode() ?: 0)
        result = 31 * result + (takerGetsIssuer?.hashCode() ?: 0)
        result = 31 * result + (takerPaysCurrency?.hashCode() ?: 0)
        result = 31 * result + (takerPaysIssuer?.hashCode() ?: 0)
        result = 31 * result + (nfTokenID?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "DirectoryNode(rootIndex=$rootIndex, indexes=${indexes.size} entries)"
}
