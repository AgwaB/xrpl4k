package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256

/**
 * A single NFToken within an [NFTokenPage].
 *
 * @property nfTokenID The unique identifier of this NFToken.
 * @property uri The URI associated with this NFToken (hex-encoded).
 */
public class NFToken(
    public val nfTokenID: Hash256,
    public val uri: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFToken) return false
        return nfTokenID == other.nfTokenID &&
            uri == other.uri
    }

    override fun hashCode(): Int {
        var result = nfTokenID.hashCode()
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "NFToken(nfTokenID=$nfTokenID)"
}

/**
 * A page in the ledger that stores up to 32 NFTokens.
 *
 * @property nfTokens The list of NFTokens stored in this page.
 * @property nextPageMin The index of the next NFToken page, if any.
 * @property previousPageMin The index of the previous NFToken page, if any.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (reserved).
 */
public class NFTokenPage(
    override val index: Hash256,
    public val nfTokens: List<NFToken> = emptyList(),
    public val nextPageMin: Hash256? = null,
    public val previousPageMin: Hash256? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.NFTokenPage

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenPage) return false
        return index == other.index &&
            nfTokens == other.nfTokens &&
            nextPageMin == other.nextPageMin &&
            previousPageMin == other.previousPageMin &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + nfTokens.hashCode()
        result = 31 * result + (nextPageMin?.hashCode() ?: 0)
        result = 31 * result + (previousPageMin?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "NFTokenPage(nfTokens=${nfTokens.size} tokens)"
}

/**
 * An open offer to buy or sell an NFToken.
 *
 * @property owner The address of the account that created this offer.
 * @property amount The amount offered (for buy) or requested (for sell).
 * @property nfTokenID The ID of the NFToken this offer applies to.
 * @property destination If set, only this account can accept the offer.
 * @property expiration Optional expiration time (seconds since Ripple Epoch).
 * @property ownerNode Hint for the owner directory page.
 * @property nfTokenOfferNode Hint for the NFToken's offer directory page.
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 * @property flags Bit-flags (e.g., sell offer).
 */
public class NFTokenOffer(
    override val index: Hash256,
    public val owner: Address,
    public val amount: CurrencyAmount,
    public val nfTokenID: Hash256,
    public val destination: Address? = null,
    public val expiration: UInt? = null,
    public val ownerNode: String? = null,
    public val nfTokenOfferNode: String? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.NFTokenOffer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenOffer) return false
        return index == other.index &&
            owner == other.owner &&
            amount == other.amount &&
            nfTokenID == other.nfTokenID &&
            destination == other.destination &&
            expiration == other.expiration &&
            ownerNode == other.ownerNode &&
            nfTokenOfferNode == other.nfTokenOfferNode &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + nfTokenID.hashCode()
        result = 31 * result + (destination?.hashCode() ?: 0)
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (ownerNode?.hashCode() ?: 0)
        result = 31 * result + (nfTokenOfferNode?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "NFTokenOffer(owner=$owner, amount=$amount, nfTokenID=$nfTokenID)"
}
