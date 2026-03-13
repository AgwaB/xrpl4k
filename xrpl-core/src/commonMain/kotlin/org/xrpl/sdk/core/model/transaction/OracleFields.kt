package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl

// ── PriceData ─────────────────────────────────────────────────────────────────

/**
 * A single price data entry in an OracleSet transaction.
 *
 * @property baseAsset The base asset currency code.
 * @property quoteAsset The quote asset currency code.
 * @property assetPrice The asset price (scaled integer).
 * @property scale The scaling factor for the asset price.
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

    override fun toString(): String =
        "PriceData(" +
            "baseAsset=$baseAsset, " +
            "quoteAsset=$quoteAsset, " +
            "assetPrice=$assetPrice, " +
            "scale=$scale" +
            ")"
}

// ── OracleSet ─────────────────────────────────────────────────────────────────

/**
 * Fields specific to an OracleSet transaction.
 *
 * @property oracleDocumentId The unique identifier for this oracle document.
 * @property provider The oracle provider identifier.
 * @property assetClass The asset class for price data.
 * @property lastUpdateTime The last update time (Ripple epoch).
 * @property priceDataSeries The list of price data entries.
 */
public class OracleSetFields(
    public val oracleDocumentId: UInt,
    public val provider: String? = null,
    public val assetClass: String? = null,
    public val lastUpdateTime: UInt? = null,
    public val priceDataSeries: List<PriceData>? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OracleSetFields) return false
        return oracleDocumentId == other.oracleDocumentId &&
            provider == other.provider &&
            assetClass == other.assetClass &&
            lastUpdateTime == other.lastUpdateTime &&
            priceDataSeries == other.priceDataSeries
    }

    override fun hashCode(): Int {
        var result = oracleDocumentId.hashCode()
        result = 31 * result + (provider?.hashCode() ?: 0)
        result = 31 * result + (assetClass?.hashCode() ?: 0)
        result = 31 * result + (lastUpdateTime?.hashCode() ?: 0)
        result = 31 * result + (priceDataSeries?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "OracleSetFields(" +
            "oracleDocumentId=$oracleDocumentId, " +
            "provider=$provider, " +
            "assetClass=$assetClass, " +
            "lastUpdateTime=$lastUpdateTime, " +
            "priceDataSeries=$priceDataSeries" +
            ")"
}

/**
 * DSL builder for [OracleSetFields].
 */
@XrplDsl
public class OracleSetBuilder internal constructor() {
    /** The unique identifier for this oracle document. Required. */
    public var oracleDocumentId: UInt = 0u

    /** The oracle provider identifier. */
    public var provider: String? = null

    /** The asset class for price data. */
    public var assetClass: String? = null

    /** The last update time (Ripple epoch). */
    public var lastUpdateTime: UInt? = null

    /** The list of price data entries. */
    public var priceDataSeries: List<PriceData>? = null

    internal fun build(): OracleSetFields =
        OracleSetFields(
            oracleDocumentId = oracleDocumentId,
            provider = provider,
            assetClass = assetClass,
            lastUpdateTime = lastUpdateTime,
            priceDataSeries = priceDataSeries,
        )
}

// ── OracleDelete ──────────────────────────────────────────────────────────────

/**
 * Fields specific to an OracleDelete transaction.
 *
 * @property oracleDocumentId The unique identifier of the oracle document to delete.
 */
public class OracleDeleteFields(
    public val oracleDocumentId: UInt,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OracleDeleteFields) return false
        return oracleDocumentId == other.oracleDocumentId
    }

    override fun hashCode(): Int = oracleDocumentId.hashCode()

    override fun toString(): String = "OracleDeleteFields(oracleDocumentId=$oracleDocumentId)"
}

/**
 * DSL builder for [OracleDeleteFields].
 */
@XrplDsl
public class OracleDeleteBuilder internal constructor() {
    /** The unique identifier of the oracle document to delete. Required. */
    public var oracleDocumentId: UInt = 0u

    internal fun build(): OracleDeleteFields = OracleDeleteFields(oracleDocumentId = oracleDocumentId)
}
