package org.xrpl.sdk.client.model

import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Information about the last ledger close.
 */
public class LastCloseInfo(
    public val convergeTime: Double?,
    public val proposers: Int?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LastCloseInfo) return false
        return convergeTime == other.convergeTime && proposers == other.proposers
    }

    override fun hashCode(): Int {
        var result = convergeTime?.hashCode() ?: 0
        result = 31 * result + (proposers?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "LastCloseInfo(convergeTime=$convergeTime, proposers=$proposers)"
}

/**
 * Information about the most recently validated ledger known to the server.
 */
public class ValidatedLedgerInfo(
    public val age: Long?,
    public val baseFeeXrp: Double?,
    public val hash: String?,
    public val reserveBaseXrp: Double?,
    public val reserveIncXrp: Double?,
    public val seq: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValidatedLedgerInfo) return false
        return age == other.age &&
            baseFeeXrp == other.baseFeeXrp &&
            hash == other.hash &&
            reserveBaseXrp == other.reserveBaseXrp &&
            reserveIncXrp == other.reserveIncXrp &&
            seq == other.seq
    }

    override fun hashCode(): Int {
        var result = age?.hashCode() ?: 0
        result = 31 * result + (baseFeeXrp?.hashCode() ?: 0)
        result = 31 * result + (hash?.hashCode() ?: 0)
        result = 31 * result + (reserveBaseXrp?.hashCode() ?: 0)
        result = 31 * result + (reserveIncXrp?.hashCode() ?: 0)
        result = 31 * result + (seq?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ValidatedLedgerInfo(" +
            "age=$age, " +
            "baseFeeXrp=$baseFeeXrp, " +
            "hash=$hash, " +
            "reserveBaseXrp=$reserveBaseXrp, " +
            "reserveIncXrp=$reserveIncXrp, " +
            "seq=$seq" +
            ")"
}

/**
 * General server information returned by [serverInfo] and [serverState].
 */
public class ServerInfo(
    public val buildVersion: String?,
    public val completeLedgers: String?,
    public val hostId: String?,
    public val ioLatencyMs: Long?,
    public val lastClose: LastCloseInfo?,
    public val loadFactor: Double?,
    public val peers: Int?,
    public val pubkeyNode: String?,
    public val serverState: String?,
    public val uptime: Long?,
    public val validatedLedger: ValidatedLedgerInfo?,
    public val validationQuorum: Int?,
    public val networkId: UInt?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerInfo) return false
        return buildVersion == other.buildVersion &&
            completeLedgers == other.completeLedgers &&
            hostId == other.hostId &&
            ioLatencyMs == other.ioLatencyMs &&
            lastClose == other.lastClose &&
            loadFactor == other.loadFactor &&
            peers == other.peers &&
            pubkeyNode == other.pubkeyNode &&
            serverState == other.serverState &&
            uptime == other.uptime &&
            validatedLedger == other.validatedLedger &&
            validationQuorum == other.validationQuorum &&
            networkId == other.networkId
    }

    override fun hashCode(): Int {
        var result = buildVersion?.hashCode() ?: 0
        result = 31 * result + (completeLedgers?.hashCode() ?: 0)
        result = 31 * result + (hostId?.hashCode() ?: 0)
        result = 31 * result + (ioLatencyMs?.hashCode() ?: 0)
        result = 31 * result + (lastClose?.hashCode() ?: 0)
        result = 31 * result + (loadFactor?.hashCode() ?: 0)
        result = 31 * result + (peers?.hashCode() ?: 0)
        result = 31 * result + (pubkeyNode?.hashCode() ?: 0)
        result = 31 * result + (serverState?.hashCode() ?: 0)
        result = 31 * result + (uptime?.hashCode() ?: 0)
        result = 31 * result + (validatedLedger?.hashCode() ?: 0)
        result = 31 * result + (validationQuorum?.hashCode() ?: 0)
        result = 31 * result + (networkId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "ServerInfo(" +
            "buildVersion=$buildVersion, " +
            "completeLedgers=$completeLedgers, " +
            "hostId=$hostId, " +
            "ioLatencyMs=$ioLatencyMs, " +
            "lastClose=$lastClose, " +
            "loadFactor=$loadFactor, " +
            "peers=$peers, " +
            "pubkeyNode=$pubkeyNode, " +
            "serverState=$serverState, " +
            "uptime=$uptime, " +
            "validatedLedger=$validatedLedger, " +
            "validationQuorum=$validationQuorum, " +
            "networkId=$networkId" +
            ")"
}

/**
 * The fee drops breakdown returned by [fee].
 */
public class FeeDrops(
    public val baseFee: XrpDrops?,
    public val medianFee: XrpDrops?,
    public val minimumFee: XrpDrops?,
    public val openLedgerFee: XrpDrops?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeeDrops) return false
        return baseFee == other.baseFee &&
            medianFee == other.medianFee &&
            minimumFee == other.minimumFee &&
            openLedgerFee == other.openLedgerFee
    }

    override fun hashCode(): Int {
        var result = baseFee?.hashCode() ?: 0
        result = 31 * result + (medianFee?.hashCode() ?: 0)
        result = 31 * result + (minimumFee?.hashCode() ?: 0)
        result = 31 * result + (openLedgerFee?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "FeeDrops(" +
            "baseFee=$baseFee, " +
            "medianFee=$medianFee, " +
            "minimumFee=$minimumFee, " +
            "openLedgerFee=$openLedgerFee" +
            ")"
}

/**
 * Result of a [fee] RPC call.
 *
 * The [openLedgerFee] convenience property mirrors [drops.openLedgerFee] for easy access
 * from autofill and other callers that need the current open-ledger base fee.
 */
public class FeeResult(
    public val currentLedgerSize: String?,
    public val currentQueueSize: String?,
    public val drops: FeeDrops,
    public val expectedLedgerSize: String?,
    public val ledgerCurrentIndex: LedgerIndex?,
    public val maxQueueSize: String?,
) {
    /** Convenience accessor for the base fee. */
    public val baseFee: XrpDrops? get() = drops.baseFee

    /** Convenience accessor for the median fee. */
    public val medianFee: XrpDrops? get() = drops.medianFee

    /** Convenience accessor for the minimum fee. */
    public val minimumFee: XrpDrops? get() = drops.minimumFee

    /** Convenience accessor for the open-ledger fee; defaults to [XrpDrops] of 0 if unavailable. */
    public val openLedgerFee: XrpDrops
        get() = drops.openLedgerFee ?: XrpDrops(0L)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeeResult) return false
        return currentLedgerSize == other.currentLedgerSize &&
            currentQueueSize == other.currentQueueSize &&
            drops == other.drops &&
            expectedLedgerSize == other.expectedLedgerSize &&
            ledgerCurrentIndex == other.ledgerCurrentIndex &&
            maxQueueSize == other.maxQueueSize
    }

    override fun hashCode(): Int {
        var result = currentLedgerSize?.hashCode() ?: 0
        result = 31 * result + (currentQueueSize?.hashCode() ?: 0)
        result = 31 * result + drops.hashCode()
        result = 31 * result + (expectedLedgerSize?.hashCode() ?: 0)
        result = 31 * result + (ledgerCurrentIndex?.hashCode() ?: 0)
        result = 31 * result + (maxQueueSize?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "FeeResult(" +
            "currentLedgerSize=$currentLedgerSize, " +
            "currentQueueSize=$currentQueueSize, " +
            "drops=$drops, " +
            "expectedLedgerSize=$expectedLedgerSize, " +
            "ledgerCurrentIndex=$ledgerCurrentIndex, " +
            "maxQueueSize=$maxQueueSize" +
            ")"
}

/**
 * Result of a [manifest] RPC call.
 */
public class ManifestResult(
    public val manifest: String?,
    public val requested: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManifestResult) return false
        return manifest == other.manifest && requested == other.requested
    }

    override fun hashCode(): Int {
        var result = manifest?.hashCode() ?: 0
        result = 31 * result + (requested?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "ManifestResult(manifest=$manifest, requested=$requested)"
}

/**
 * Information about a single XRPL amendment.
 */
public class FeatureEntry(
    public val enabled: Boolean,
    public val name: String?,
    public val supported: Boolean,
    public val vetoed: Boolean?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeatureEntry) return false
        return enabled == other.enabled &&
            name == other.name &&
            supported == other.supported &&
            vetoed == other.vetoed
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + supported.hashCode()
        result = 31 * result + (vetoed?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "FeatureEntry(enabled=$enabled, name=$name, supported=$supported, vetoed=$vetoed)"
}

/**
 * Result of a [feature] RPC call.
 */
public class FeatureResult(
    public val features: Map<String, FeatureEntry>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeatureResult) return false
        return features == other.features
    }

    override fun hashCode(): Int = features.hashCode()

    override fun toString(): String = "FeatureResult(features=$features)"
}
