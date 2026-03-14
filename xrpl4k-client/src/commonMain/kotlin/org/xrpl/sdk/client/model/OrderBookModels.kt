package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex

public class BookOffer(
    public val account: Address,
    public val takerGets: JsonElement,
    public val takerPays: JsonElement,
    public val quality: String?,
    public val flags: UInt,
    public val sequence: Long?,
    public val ownerFunds: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BookOffer) return false
        return account == other.account &&
            takerGets == other.takerGets &&
            takerPays == other.takerPays &&
            quality == other.quality &&
            flags == other.flags &&
            sequence == other.sequence &&
            ownerFunds == other.ownerFunds
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + takerGets.hashCode()
        result = 31 * result + takerPays.hashCode()
        result = 31 * result + (quality?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        result = 31 * result + (sequence?.hashCode() ?: 0)
        result = 31 * result + (ownerFunds?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "BookOffer(" +
            "account=$account, " +
            "takerGets=$takerGets, " +
            "takerPays=$takerPays, " +
            "quality=$quality, " +
            "flags=$flags, " +
            "sequence=$sequence, " +
            "ownerFunds=$ownerFunds" +
            ")"
}

public class BookOffersResult(
    public val offers: List<BookOffer>,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BookOffersResult) return false
        return offers == other.offers && ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = offers.hashCode()
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "BookOffersResult(offers=$offers, ledgerIndex=$ledgerIndex)"
}

public class BookChange(
    public val currencyA: String,
    public val currencyB: String,
    public val volumeA: String?,
    public val volumeB: String?,
    public val high: String?,
    public val low: String?,
    public val open: String?,
    public val close: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BookChange) return false
        return currencyA == other.currencyA &&
            currencyB == other.currencyB &&
            volumeA == other.volumeA &&
            volumeB == other.volumeB &&
            high == other.high &&
            low == other.low &&
            open == other.open &&
            close == other.close
    }

    override fun hashCode(): Int {
        var result = currencyA.hashCode()
        result = 31 * result + currencyB.hashCode()
        result = 31 * result + (volumeA?.hashCode() ?: 0)
        result = 31 * result + (volumeB?.hashCode() ?: 0)
        result = 31 * result + (high?.hashCode() ?: 0)
        result = 31 * result + (low?.hashCode() ?: 0)
        result = 31 * result + (open?.hashCode() ?: 0)
        result = 31 * result + (close?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "BookChange(" +
            "currencyA=$currencyA, " +
            "currencyB=$currencyB, " +
            "volumeA=$volumeA, " +
            "volumeB=$volumeB, " +
            "high=$high, " +
            "low=$low, " +
            "open=$open, " +
            "close=$close" +
            ")"
}

public class BookChangesResult(
    public val changes: List<BookChange>,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BookChangesResult) return false
        return changes == other.changes && ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = changes.hashCode()
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "BookChangesResult(changes=$changes, ledgerIndex=$ledgerIndex)"
}
