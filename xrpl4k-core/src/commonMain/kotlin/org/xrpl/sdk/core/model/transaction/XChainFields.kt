package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── XChainBridgeSpec ──────────────────────────────────────────────────────────

/**
 * Specification of a cross-chain bridge.
 *
 * @property lockingChainDoor The door account on the locking chain.
 * @property lockingChainIssue The currency issue on the locking chain.
 * @property issuingChainDoor The door account on the issuing chain.
 * @property issuingChainIssue The currency issue on the issuing chain.
 */
public class XChainBridgeSpec(
    public val lockingChainDoor: Address,
    public val lockingChainIssue: String,
    public val issuingChainDoor: Address,
    public val issuingChainIssue: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainBridgeSpec) return false
        return lockingChainDoor == other.lockingChainDoor &&
            lockingChainIssue == other.lockingChainIssue &&
            issuingChainDoor == other.issuingChainDoor &&
            issuingChainIssue == other.issuingChainIssue
    }

    override fun hashCode(): Int {
        var result = lockingChainDoor.hashCode()
        result = 31 * result + lockingChainIssue.hashCode()
        result = 31 * result + issuingChainDoor.hashCode()
        result = 31 * result + issuingChainIssue.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainBridgeSpec(" +
            "lockingChainDoor=$lockingChainDoor, " +
            "lockingChainIssue=$lockingChainIssue, " +
            "issuingChainDoor=$issuingChainDoor, " +
            "issuingChainIssue=$issuingChainIssue" +
            ")"
}

// ── XChainCreateBridge ────────────────────────────────────────────────────────

/**
 * Fields specific to an XChainCreateBridge transaction.
 *
 * @property bridge The bridge specification.
 * @property signatureReward The reward for submitting attestations.
 * @property minAccountCreateAmount The minimum amount to create an account via the bridge.
 */
public class XChainCreateBridgeFields(
    public val bridge: XChainBridgeSpec,
    public val signatureReward: CurrencyAmount,
    public val minAccountCreateAmount: CurrencyAmount? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainCreateBridgeFields) return false
        return bridge == other.bridge &&
            signatureReward == other.signatureReward &&
            minAccountCreateAmount == other.minAccountCreateAmount
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + signatureReward.hashCode()
        result = 31 * result + (minAccountCreateAmount?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "XChainCreateBridgeFields(" +
            "bridge=$bridge, " +
            "signatureReward=$signatureReward, " +
            "minAccountCreateAmount=$minAccountCreateAmount" +
            ")"
}

/**
 * DSL builder for [XChainCreateBridgeFields].
 */
@XrplDsl
public class XChainCreateBridgeBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The reward for submitting attestations. Required. */
    public lateinit var signatureReward: CurrencyAmount

    /** The minimum amount to create an account via the bridge. */
    public var minAccountCreateAmount: CurrencyAmount? = null

    internal fun build(): XChainCreateBridgeFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        require(::signatureReward.isInitialized) { "signatureReward is required" }
        return XChainCreateBridgeFields(
            bridge = bridgeValue,
            signatureReward = signatureReward,
            minAccountCreateAmount = minAccountCreateAmount,
        )
    }
}

// ── XChainModifyBridge ────────────────────────────────────────────────────────

/**
 * Fields specific to an XChainModifyBridge transaction.
 *
 * @property bridge The bridge specification.
 * @property signatureReward The new reward for submitting attestations.
 * @property minAccountCreateAmount The new minimum amount to create an account via the bridge.
 */
public class XChainModifyBridgeFields(
    public val bridge: XChainBridgeSpec,
    public val signatureReward: CurrencyAmount? = null,
    public val minAccountCreateAmount: CurrencyAmount? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainModifyBridgeFields) return false
        return bridge == other.bridge &&
            signatureReward == other.signatureReward &&
            minAccountCreateAmount == other.minAccountCreateAmount
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + (signatureReward?.hashCode() ?: 0)
        result = 31 * result + (minAccountCreateAmount?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "XChainModifyBridgeFields(" +
            "bridge=$bridge, " +
            "signatureReward=$signatureReward, " +
            "minAccountCreateAmount=$minAccountCreateAmount" +
            ")"
}

/**
 * DSL builder for [XChainModifyBridgeFields].
 */
@XrplDsl
public class XChainModifyBridgeBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The new reward for submitting attestations. */
    public var signatureReward: CurrencyAmount? = null

    /** The new minimum amount to create an account via the bridge. */
    public var minAccountCreateAmount: CurrencyAmount? = null

    internal fun build(): XChainModifyBridgeFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        return XChainModifyBridgeFields(
            bridge = bridgeValue,
            signatureReward = signatureReward,
            minAccountCreateAmount = minAccountCreateAmount,
        )
    }
}

// ── XChainCreateClaimID ───────────────────────────────────────────────────────

/**
 * Fields specific to an XChainCreateClaimID transaction.
 *
 * @property bridge The bridge specification.
 * @property signatureReward The reward for submitting attestations.
 * @property otherChainSource The account on the other chain that will initiate the transfer.
 */
public class XChainCreateClaimIDFields(
    public val bridge: XChainBridgeSpec,
    public val signatureReward: CurrencyAmount,
    public val otherChainSource: Address,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainCreateClaimIDFields) return false
        return bridge == other.bridge &&
            signatureReward == other.signatureReward &&
            otherChainSource == other.otherChainSource
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + signatureReward.hashCode()
        result = 31 * result + otherChainSource.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainCreateClaimIDFields(" +
            "bridge=$bridge, " +
            "signatureReward=$signatureReward, " +
            "otherChainSource=$otherChainSource" +
            ")"
}

/**
 * DSL builder for [XChainCreateClaimIDFields].
 */
@XrplDsl
public class XChainCreateClaimIDBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The reward for submitting attestations. Required. */
    public lateinit var signatureReward: CurrencyAmount

    /** The account on the other chain that will initiate the transfer. Required. */
    public var otherChainSource: Address? = null

    internal fun build(): XChainCreateClaimIDFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        require(::signatureReward.isInitialized) { "signatureReward is required" }
        val otherChainSourceValue = requireNotNull(otherChainSource) { "otherChainSource is required" }
        return XChainCreateClaimIDFields(
            bridge = bridgeValue,
            signatureReward = signatureReward,
            otherChainSource = otherChainSourceValue,
        )
    }
}

// ── XChainCommit ──────────────────────────────────────────────────────────────

/**
 * Fields specific to an XChainCommit transaction.
 *
 * @property bridge The bridge specification.
 * @property claimId The cross-chain claim ID.
 * @property amount The amount to commit.
 * @property otherChainDestination The destination account on the other chain.
 */
public class XChainCommitFields(
    public val bridge: XChainBridgeSpec,
    public val claimId: UInt,
    public val amount: CurrencyAmount,
    public val otherChainDestination: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainCommitFields) return false
        return bridge == other.bridge &&
            claimId == other.claimId &&
            amount == other.amount &&
            otherChainDestination == other.otherChainDestination
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + claimId.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (otherChainDestination?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "XChainCommitFields(" +
            "bridge=$bridge, " +
            "claimId=$claimId, " +
            "amount=$amount, " +
            "otherChainDestination=$otherChainDestination" +
            ")"
}

/**
 * DSL builder for [XChainCommitFields].
 */
@XrplDsl
public class XChainCommitBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The cross-chain claim ID. Required. */
    public var claimId: UInt = 0u

    /** The amount to commit. Required. */
    public lateinit var amount: CurrencyAmount

    /** The destination account on the other chain. */
    public var otherChainDestination: Address? = null

    internal fun build(): XChainCommitFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        require(::amount.isInitialized) { "amount is required" }
        return XChainCommitFields(
            bridge = bridgeValue,
            claimId = claimId,
            amount = amount,
            otherChainDestination = otherChainDestination,
        )
    }
}

// ── XChainClaim ───────────────────────────────────────────────────────────────

/**
 * Fields specific to an XChainClaim transaction.
 *
 * @property bridge The bridge specification.
 * @property claimId The cross-chain claim ID.
 * @property destination The destination account on this chain.
 * @property amount The amount to claim.
 */
public class XChainClaimFields(
    public val bridge: XChainBridgeSpec,
    public val claimId: UInt,
    public val destination: Address,
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainClaimFields) return false
        return bridge == other.bridge &&
            claimId == other.claimId &&
            destination == other.destination &&
            amount == other.amount
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + claimId.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainClaimFields(" +
            "bridge=$bridge, " +
            "claimId=$claimId, " +
            "destination=$destination, " +
            "amount=$amount" +
            ")"
}

/**
 * DSL builder for [XChainClaimFields].
 */
@XrplDsl
public class XChainClaimBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The cross-chain claim ID. Required. */
    public var claimId: UInt = 0u

    /** The destination account on this chain. Required. */
    public var destination: Address? = null

    /** The amount to claim. Required. */
    public lateinit var amount: CurrencyAmount

    internal fun build(): XChainClaimFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        val destinationValue = requireNotNull(destination) { "destination is required" }
        require(::amount.isInitialized) { "amount is required" }
        return XChainClaimFields(
            bridge = bridgeValue,
            claimId = claimId,
            destination = destinationValue,
            amount = amount,
        )
    }
}

// ── XChainAccountCreateCommit ─────────────────────────────────────────────────

/**
 * Fields specific to an XChainAccountCreateCommit transaction.
 *
 * @property bridge The bridge specification.
 * @property amount The amount to commit.
 * @property signatureReward The reward for submitting attestations.
 * @property destination The destination account to create on the other chain.
 */
public class XChainAccountCreateCommitFields(
    public val bridge: XChainBridgeSpec,
    public val amount: CurrencyAmount,
    public val signatureReward: CurrencyAmount,
    public val destination: Address,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainAccountCreateCommitFields) return false
        return bridge == other.bridge &&
            amount == other.amount &&
            signatureReward == other.signatureReward &&
            destination == other.destination
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + signatureReward.hashCode()
        result = 31 * result + destination.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainAccountCreateCommitFields(" +
            "bridge=$bridge, " +
            "amount=$amount, " +
            "signatureReward=$signatureReward, " +
            "destination=$destination" +
            ")"
}

/**
 * DSL builder for [XChainAccountCreateCommitFields].
 */
@XrplDsl
public class XChainAccountCreateCommitBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The amount to commit. Required. */
    public lateinit var amount: CurrencyAmount

    /** The reward for submitting attestations. Required. */
    public lateinit var signatureReward: CurrencyAmount

    /** The destination account to create on the other chain. Required. */
    public var destination: Address? = null

    internal fun build(): XChainAccountCreateCommitFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        require(::amount.isInitialized) { "amount is required" }
        require(::signatureReward.isInitialized) { "signatureReward is required" }
        val destinationValue = requireNotNull(destination) { "destination is required" }
        return XChainAccountCreateCommitFields(
            bridge = bridgeValue,
            amount = amount,
            signatureReward = signatureReward,
            destination = destinationValue,
        )
    }
}
