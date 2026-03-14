package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * A single price data entry within an [Oracle].
 *
 * @property baseAsset The base asset identifier (e.g., "XRP").
 * @property quoteAsset The quote asset identifier (e.g., "USD").
 * @property assetPrice The price as a scaled integer.
 * @property scale The decimal scale of [assetPrice].
 */
public class PriceData(
    public val baseAsset: String,
    public val quoteAsset: String,
    public val assetPrice: Long? = null,
    public val scale: UInt? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PriceData) return false
        return baseAsset == other.baseAsset &&
            quoteAsset == other.quoteAsset &&
            assetPrice == other.assetPrice &&
            scale == other.scale
    }

    override fun hashCode(): Int {
        var result = baseAsset.hashCode()
        result = 31 * result + quoteAsset.hashCode()
        result = 31 * result + (assetPrice?.hashCode() ?: 0)
        result = 31 * result + (scale?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "PriceData(baseAsset=$baseAsset, quoteAsset=$quoteAsset, assetPrice=$assetPrice)"
}

/**
 * A price oracle providing off-chain data to the ledger.
 *
 * @property owner The address of the account that controls this oracle.
 * @property provider An identifier for the oracle provider (hex-encoded).
 * @property assetClass The class of assets this oracle covers (hex-encoded).
 * @property lastUpdateTime The time this oracle was last updated (seconds since Ripple Epoch).
 * @property priceDataSeries The list of price data entries.
 * @property uri A URI associated with this oracle (hex-encoded).
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class Oracle(
    override val index: Hash256,
    public val owner: Address,
    public val provider: String,
    public val assetClass: String,
    public val lastUpdateTime: UInt,
    public val priceDataSeries: List<PriceData> = emptyList(),
    public val uri: String? = null,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Oracle

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Oracle) return false
        return index == other.index &&
            owner == other.owner &&
            provider == other.provider &&
            assetClass == other.assetClass &&
            lastUpdateTime == other.lastUpdateTime &&
            priceDataSeries == other.priceDataSeries &&
            uri == other.uri &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + assetClass.hashCode()
        result = 31 * result + lastUpdateTime.hashCode()
        result = 31 * result + priceDataSeries.hashCode()
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Oracle(owner=$owner, provider=$provider)"
}

/**
 * A permissioned domain that restricts credential-based operations.
 *
 * @property owner The address of the account that owns this domain.
 * @property sequence The sequence number of the transaction that created this domain.
 * @property acceptedCredentials The credential types accepted by this domain.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class PermissionedDomain(
    override val index: Hash256,
    public val owner: Address,
    public val sequence: UInt,
    public val acceptedCredentials: List<String> = emptyList(),
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.PermissionedDomain

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PermissionedDomain) return false
        return index == other.index &&
            owner == other.owner &&
            sequence == other.sequence &&
            acceptedCredentials == other.acceptedCredentials &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + acceptedCredentials.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "PermissionedDomain(owner=$owner)"
}

/**
 * A sequence-number placeholder that can be consumed later by a transaction.
 *
 * @property account The address of the account that owns this ticket.
 * @property ticketSequence The sequence number reserved by this ticket.
 * @property ownerNode Hint for the owner directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class Ticket(
    override val index: Hash256,
    public val account: Address,
    public val ticketSequence: UInt,
    public val ownerNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Ticket

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ticket) return false
        return index == other.index &&
            account == other.account &&
            ticketSequence == other.ticketSequence &&
            ownerNode == other.ownerNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + ticketSequence.hashCode()
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "Ticket(account=$account, ticketSequence=$ticketSequence)"
}
