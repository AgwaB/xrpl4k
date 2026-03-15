package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Address

public class NftOffer(
    public val amount: JsonElement,
    public val flags: UInt,
    public val nftOfferIndex: String?,
    public val owner: Address,
    public val destination: Address?,
    public val expiration: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftOffer) return false
        return amount == other.amount &&
            flags == other.flags &&
            nftOfferIndex == other.nftOfferIndex &&
            owner == other.owner &&
            destination == other.destination &&
            expiration == other.expiration
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (nftOfferIndex?.hashCode() ?: 0)
        result = 31 * result + owner.hashCode()
        result = 31 * result + (destination?.hashCode() ?: 0)
        result = 31 * result + (expiration?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NftOffer(" +
            "amount=$amount, " +
            "flags=$flags, " +
            "nftOfferIndex=$nftOfferIndex, " +
            "owner=$owner, " +
            "destination=$destination, " +
            "expiration=$expiration" +
            ")"
}

public class NftOffersResult(
    public val nftId: String,
    public val offers: List<NftOffer>,
    public val marker: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftOffersResult) return false
        return nftId == other.nftId &&
            offers == other.offers &&
            marker == other.marker
    }

    override fun hashCode(): Int {
        var result = nftId.hashCode()
        result = 31 * result + offers.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "NftOffersResult(nftId=$nftId, offers=$offers, marker=$marker)"
}

public class NftInfo(
    public val nftId: String,
    public val owner: Address,
    public val flags: UInt,
    public val uri: String?,
    public val isBurned: Boolean,
    public val nftTaxon: Long?,
    public val nftSerial: Long?,
    public val issuer: Address?,
    public val transferFee: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftInfo) return false
        return nftId == other.nftId &&
            owner == other.owner &&
            flags == other.flags &&
            uri == other.uri &&
            isBurned == other.isBurned &&
            nftTaxon == other.nftTaxon &&
            nftSerial == other.nftSerial &&
            issuer == other.issuer &&
            transferFee == other.transferFee
    }

    override fun hashCode(): Int {
        var result = nftId.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + isBurned.hashCode()
        result = 31 * result + (nftTaxon?.hashCode() ?: 0)
        result = 31 * result + (nftSerial?.hashCode() ?: 0)
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + (transferFee?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NftInfo(" +
            "nftId=$nftId, " +
            "owner=$owner, " +
            "flags=$flags, " +
            "uri=$uri, " +
            "isBurned=$isBurned, " +
            "nftTaxon=$nftTaxon, " +
            "nftSerial=$nftSerial, " +
            "issuer=$issuer, " +
            "transferFee=$transferFee" +
            ")"
}

public class NftHistoryEntry(
    public val tx: JsonElement?,
    public val meta: JsonElement?,
    public val validated: Boolean?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftHistoryEntry) return false
        return tx == other.tx &&
            meta == other.meta &&
            validated == other.validated
    }

    override fun hashCode(): Int {
        var result = (tx?.hashCode() ?: 0)
        result = 31 * result + (meta?.hashCode() ?: 0)
        result = 31 * result + (validated?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "NftHistoryEntry(tx=$tx, meta=$meta, validated=$validated)"
}

public class NftHistoryResult(
    public val nftId: String,
    public val transactions: List<NftHistoryEntry>,
    public val marker: JsonElement?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftHistoryResult) return false
        return nftId == other.nftId &&
            transactions == other.transactions &&
            marker == other.marker
    }

    override fun hashCode(): Int {
        var result = nftId.hashCode()
        result = 31 * result + transactions.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "NftHistoryResult(nftId=$nftId, transactions=$transactions, marker=$marker)"
}

/**
 * An individual NFToken returned by [nftsByIssuer].
 */
public class NftsByIssuerToken(
    public val nftId: String,
    public val ledgerIndex: Long?,
    public val owner: Address?,
    public val isBurned: Boolean,
    public val flags: UInt,
    public val transferFee: Long?,
    public val issuer: Address?,
    public val nftTaxon: Long?,
    public val nftSerial: Long?,
    public val uri: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftsByIssuerToken) return false
        return nftId == other.nftId &&
            ledgerIndex == other.ledgerIndex &&
            owner == other.owner &&
            isBurned == other.isBurned &&
            flags == other.flags &&
            transferFee == other.transferFee &&
            issuer == other.issuer &&
            nftTaxon == other.nftTaxon &&
            nftSerial == other.nftSerial &&
            uri == other.uri
    }

    override fun hashCode(): Int {
        var result = nftId.hashCode()
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + isBurned.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (transferFee?.hashCode() ?: 0)
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + (nftTaxon?.hashCode() ?: 0)
        result = 31 * result + (nftSerial?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NftsByIssuerToken(" +
            "nftId=$nftId, " +
            "ledgerIndex=$ledgerIndex, " +
            "owner=$owner, " +
            "isBurned=$isBurned, " +
            "flags=$flags, " +
            "transferFee=$transferFee, " +
            "issuer=$issuer, " +
            "nftTaxon=$nftTaxon, " +
            "nftSerial=$nftSerial, " +
            "uri=$uri" +
            ")"
}

/**
 * Result of an [nftsByIssuer] RPC call (Clio-only).
 */
public class NftsByIssuerResult(
    public val issuer: Address,
    public val nfts: List<NftsByIssuerToken>,
    public val marker: JsonElement?,
    public val limit: Int?,
    public val nftTaxon: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NftsByIssuerResult) return false
        return issuer == other.issuer &&
            nfts == other.nfts &&
            marker == other.marker &&
            limit == other.limit &&
            nftTaxon == other.nftTaxon
    }

    override fun hashCode(): Int {
        var result = issuer.hashCode()
        result = 31 * result + nfts.hashCode()
        result = 31 * result + (marker?.hashCode() ?: 0)
        result = 31 * result + (limit?.hashCode() ?: 0)
        result = 31 * result + (nftTaxon?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NftsByIssuerResult(issuer=$issuer, nfts=$nfts, marker=$marker, limit=$limit, nftTaxon=$nftTaxon)"
}
