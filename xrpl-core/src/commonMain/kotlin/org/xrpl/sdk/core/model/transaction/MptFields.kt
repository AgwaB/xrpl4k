package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

// ── MPTokenIssuanceCreate ─────────────────────────────────────────────────────

/**
 * Fields specific to an MPTokenIssuanceCreate transaction.
 *
 * @property assetScale The scale factor for asset amounts.
 * @property transferFee The fee charged on transfers (in units of 1/10 basis points).
 * @property maxAmount The maximum issuance amount.
 * @property metadata Arbitrary metadata about the token.
 * @property flags Flags controlling token behavior.
 */
public class MPTokenIssuanceCreateFields(
    public val assetScale: UInt? = null,
    public val transferFee: UInt? = null,
    public val maxAmount: String? = null,
    public val metadata: String? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MPTokenIssuanceCreateFields) return false
        return assetScale == other.assetScale &&
            transferFee == other.transferFee &&
            maxAmount == other.maxAmount &&
            metadata == other.metadata &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = (assetScale?.hashCode() ?: 0)
        result = 31 * result + (transferFee?.hashCode() ?: 0)
        result = 31 * result + (maxAmount?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "MPTokenIssuanceCreateFields(" +
            "assetScale=$assetScale, " +
            "transferFee=$transferFee, " +
            "maxAmount=$maxAmount, " +
            "metadata=$metadata, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [MPTokenIssuanceCreateFields].
 */
@XrplDsl
public class MPTokenIssuanceCreateBuilder internal constructor() {
    /** The scale factor for asset amounts. */
    public var assetScale: UInt? = null

    /** The fee charged on transfers. */
    public var transferFee: UInt? = null

    /** The maximum issuance amount. */
    public var maxAmount: String? = null

    /** Arbitrary metadata about the token. */
    public var metadata: String? = null

    /** Flags controlling token behavior. */
    public var flags: UInt? = null

    internal fun build(): MPTokenIssuanceCreateFields =
        MPTokenIssuanceCreateFields(
            assetScale = assetScale,
            transferFee = transferFee,
            maxAmount = maxAmount,
            metadata = metadata,
            flags = flags,
        )
}

// ── MPTokenIssuanceDestroy ────────────────────────────────────────────────────

/**
 * Fields specific to an MPTokenIssuanceDestroy transaction.
 *
 * @property mptIssuanceId The ID of the MPTokenIssuance to destroy.
 */
public class MPTokenIssuanceDestroyFields(
    public val mptIssuanceId: String,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MPTokenIssuanceDestroyFields) return false
        return mptIssuanceId == other.mptIssuanceId
    }

    override fun hashCode(): Int = mptIssuanceId.hashCode()

    override fun toString(): String = "MPTokenIssuanceDestroyFields(mptIssuanceId=$mptIssuanceId)"
}

/**
 * DSL builder for [MPTokenIssuanceDestroyFields].
 */
@XrplDsl
public class MPTokenIssuanceDestroyBuilder internal constructor() {
    /** The ID of the MPTokenIssuance to destroy. Required. */
    public lateinit var mptIssuanceId: String

    internal fun build(): MPTokenIssuanceDestroyFields {
        require(::mptIssuanceId.isInitialized) { "mptIssuanceId is required" }
        return MPTokenIssuanceDestroyFields(mptIssuanceId = mptIssuanceId)
    }
}

// ── MPTokenIssuanceSet ────────────────────────────────────────────────────────

/**
 * Fields specific to an MPTokenIssuanceSet transaction.
 *
 * @property mptIssuanceId The ID of the MPTokenIssuance to modify.
 * @property holder Optional holder address to target.
 * @property flags Flags controlling the operation.
 */
public class MPTokenIssuanceSetFields(
    public val mptIssuanceId: String,
    public val holder: Address? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MPTokenIssuanceSetFields) return false
        return mptIssuanceId == other.mptIssuanceId &&
            holder == other.holder &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = mptIssuanceId.hashCode()
        result = 31 * result + (holder?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "MPTokenIssuanceSetFields(" +
            "mptIssuanceId=$mptIssuanceId, " +
            "holder=$holder, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [MPTokenIssuanceSetFields].
 */
@XrplDsl
public class MPTokenIssuanceSetBuilder internal constructor() {
    /** The ID of the MPTokenIssuance to modify. Required. */
    public lateinit var mptIssuanceId: String

    /** Optional holder address to target. */
    public var holder: Address? = null

    /** Flags controlling the operation. */
    public var flags: UInt? = null

    internal fun build(): MPTokenIssuanceSetFields {
        require(::mptIssuanceId.isInitialized) { "mptIssuanceId is required" }
        return MPTokenIssuanceSetFields(
            mptIssuanceId = mptIssuanceId,
            holder = holder,
            flags = flags,
        )
    }
}

// ── MPTokenAuthorize ──────────────────────────────────────────────────────────

/**
 * Fields specific to an MPTokenAuthorize transaction.
 *
 * @property mptIssuanceId The ID of the MPTokenIssuance to authorize.
 * @property holder Optional holder address to authorize.
 * @property flags Flags controlling authorization behavior.
 */
public class MPTokenAuthorizeFields(
    public val mptIssuanceId: String,
    public val holder: Address? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MPTokenAuthorizeFields) return false
        return mptIssuanceId == other.mptIssuanceId &&
            holder == other.holder &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = mptIssuanceId.hashCode()
        result = 31 * result + (holder?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "MPTokenAuthorizeFields(" +
            "mptIssuanceId=$mptIssuanceId, " +
            "holder=$holder, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [MPTokenAuthorizeFields].
 */
@XrplDsl
public class MPTokenAuthorizeBuilder internal constructor() {
    /** The ID of the MPTokenIssuance to authorize. Required. */
    public lateinit var mptIssuanceId: String

    /** Optional holder address to authorize. */
    public var holder: Address? = null

    /** Flags controlling authorization behavior. */
    public var flags: UInt? = null

    internal fun build(): MPTokenAuthorizeFields {
        require(::mptIssuanceId.isInitialized) { "mptIssuanceId is required" }
        return MPTokenAuthorizeFields(
            mptIssuanceId = mptIssuanceId,
            holder = holder,
            flags = flags,
        )
    }
}
