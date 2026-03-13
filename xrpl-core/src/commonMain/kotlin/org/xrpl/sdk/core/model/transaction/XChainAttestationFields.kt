package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── XChainAddClaimAttestation ─────────────────────────────────────────────────

/**
 * Fields specific to an XChainAddClaimAttestation transaction.
 *
 * @property bridge The bridge specification.
 * @property publicKey The public key of the witness server.
 * @property signature The signature from the witness server.
 * @property otherChainSource The account on the other chain that initiated the transfer.
 * @property amount The amount being attested.
 * @property attestationRewardAccount The account to receive the attestation reward.
 * @property attestationSignerAccount The account that signed the attestation.
 * @property wasLockingChainSend Whether the transfer was from the locking chain.
 * @property xChainClaimId The cross-chain claim ID.
 * @property destination Optional destination account on this chain.
 */
public class XChainAddClaimAttestationFields(
    public val bridge: XChainBridgeSpec,
    public val publicKey: String,
    public val signature: String,
    public val otherChainSource: Address,
    public val amount: CurrencyAmount,
    public val attestationRewardAccount: Address,
    public val attestationSignerAccount: Address,
    public val wasLockingChainSend: Boolean,
    public val xChainClaimId: UInt,
    public val destination: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainAddClaimAttestationFields) return false
        return bridge == other.bridge &&
            publicKey == other.publicKey &&
            signature == other.signature &&
            otherChainSource == other.otherChainSource &&
            amount == other.amount &&
            attestationRewardAccount == other.attestationRewardAccount &&
            attestationSignerAccount == other.attestationSignerAccount &&
            wasLockingChainSend == other.wasLockingChainSend &&
            xChainClaimId == other.xChainClaimId &&
            destination == other.destination
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + otherChainSource.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + attestationRewardAccount.hashCode()
        result = 31 * result + attestationSignerAccount.hashCode()
        result = 31 * result + wasLockingChainSend.hashCode()
        result = 31 * result + xChainClaimId.hashCode()
        result = 31 * result + (destination?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "XChainAddClaimAttestationFields(" +
            "bridge=$bridge, " +
            "publicKey=$publicKey, " +
            "signature=$signature, " +
            "otherChainSource=$otherChainSource, " +
            "amount=$amount, " +
            "attestationRewardAccount=$attestationRewardAccount, " +
            "attestationSignerAccount=$attestationSignerAccount, " +
            "wasLockingChainSend=$wasLockingChainSend, " +
            "xChainClaimId=$xChainClaimId, " +
            "destination=$destination" +
            ")"
}

/**
 * DSL builder for [XChainAddClaimAttestationFields].
 */
@XrplDsl
public class XChainAddClaimAttestationBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The public key of the witness server. Required. */
    public lateinit var publicKey: String

    /** The signature from the witness server. Required. */
    public lateinit var signature: String

    /** The account on the other chain that initiated the transfer. Required. */
    public var otherChainSource: Address? = null

    /** The amount being attested. Required. */
    public lateinit var amount: CurrencyAmount

    /** The account to receive the attestation reward. Required. */
    public var attestationRewardAccount: Address? = null

    /** The account that signed the attestation. Required. */
    public var attestationSignerAccount: Address? = null

    /** Whether the transfer was from the locking chain. Required. */
    public var wasLockingChainSend: Boolean = false

    /** The cross-chain claim ID. Required. */
    public var xChainClaimId: UInt = 0u

    /** Optional destination account on this chain. */
    public var destination: Address? = null

    internal fun build(): XChainAddClaimAttestationFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        require(::publicKey.isInitialized) { "publicKey is required" }
        require(::signature.isInitialized) { "signature is required" }
        val otherChainSourceValue = requireNotNull(otherChainSource) { "otherChainSource is required" }
        require(::amount.isInitialized) { "amount is required" }
        val attestationRewardAccountValue =
            requireNotNull(attestationRewardAccount) { "attestationRewardAccount is required" }
        val attestationSignerAccountValue =
            requireNotNull(attestationSignerAccount) { "attestationSignerAccount is required" }
        return XChainAddClaimAttestationFields(
            bridge = bridgeValue,
            publicKey = publicKey,
            signature = signature,
            otherChainSource = otherChainSourceValue,
            amount = amount,
            attestationRewardAccount = attestationRewardAccountValue,
            attestationSignerAccount = attestationSignerAccountValue,
            wasLockingChainSend = wasLockingChainSend,
            xChainClaimId = xChainClaimId,
            destination = destination,
        )
    }
}

// ── XChainAddAccountCreateAttestation ────────────────────────────────────────

/**
 * Fields specific to an XChainAddAccountCreateAttestation transaction.
 *
 * @property bridge The bridge specification.
 * @property publicKey The public key of the witness server.
 * @property signature The signature from the witness server.
 * @property otherChainSource The account on the other chain that initiated the transfer.
 * @property amount The amount being attested.
 * @property attestationRewardAccount The account to receive the attestation reward.
 * @property attestationSignerAccount The account that signed the attestation.
 * @property wasLockingChainSend Whether the transfer was from the locking chain.
 * @property xChainAccountCreateCount The create count for the cross-chain account creation.
 * @property destination The destination account to create on this chain.
 * @property signatureReward The reward for submitting the attestation.
 */
public class XChainAddAccountCreateAttestationFields(
    public val bridge: XChainBridgeSpec,
    public val publicKey: String,
    public val signature: String,
    public val otherChainSource: Address,
    public val amount: CurrencyAmount,
    public val attestationRewardAccount: Address,
    public val attestationSignerAccount: Address,
    public val wasLockingChainSend: Boolean,
    public val xChainAccountCreateCount: UInt,
    public val destination: Address,
    public val signatureReward: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XChainAddAccountCreateAttestationFields) return false
        return bridge == other.bridge &&
            publicKey == other.publicKey &&
            signature == other.signature &&
            otherChainSource == other.otherChainSource &&
            amount == other.amount &&
            attestationRewardAccount == other.attestationRewardAccount &&
            attestationSignerAccount == other.attestationSignerAccount &&
            wasLockingChainSend == other.wasLockingChainSend &&
            xChainAccountCreateCount == other.xChainAccountCreateCount &&
            destination == other.destination &&
            signatureReward == other.signatureReward
    }

    override fun hashCode(): Int {
        var result = bridge.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + otherChainSource.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + attestationRewardAccount.hashCode()
        result = 31 * result + attestationSignerAccount.hashCode()
        result = 31 * result + wasLockingChainSend.hashCode()
        result = 31 * result + xChainAccountCreateCount.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + signatureReward.hashCode()
        return result
    }

    override fun toString(): String =
        "XChainAddAccountCreateAttestationFields(" +
            "bridge=$bridge, " +
            "publicKey=$publicKey, " +
            "signature=$signature, " +
            "otherChainSource=$otherChainSource, " +
            "amount=$amount, " +
            "attestationRewardAccount=$attestationRewardAccount, " +
            "attestationSignerAccount=$attestationSignerAccount, " +
            "wasLockingChainSend=$wasLockingChainSend, " +
            "xChainAccountCreateCount=$xChainAccountCreateCount, " +
            "destination=$destination, " +
            "signatureReward=$signatureReward" +
            ")"
}

/**
 * DSL builder for [XChainAddAccountCreateAttestationFields].
 */
@XrplDsl
public class XChainAddAccountCreateAttestationBuilder internal constructor() {
    /** The bridge specification. Required. */
    public var bridge: XChainBridgeSpec? = null

    /** The public key of the witness server. Required. */
    public lateinit var publicKey: String

    /** The signature from the witness server. Required. */
    public lateinit var signature: String

    /** The account on the other chain that initiated the transfer. Required. */
    public var otherChainSource: Address? = null

    /** The amount being attested. Required. */
    public lateinit var amount: CurrencyAmount

    /** The account to receive the attestation reward. Required. */
    public var attestationRewardAccount: Address? = null

    /** The account that signed the attestation. Required. */
    public var attestationSignerAccount: Address? = null

    /** Whether the transfer was from the locking chain. Required. */
    public var wasLockingChainSend: Boolean = false

    /** The create count for the cross-chain account creation. Required. */
    public var xChainAccountCreateCount: UInt = 0u

    /** The destination account to create on this chain. Required. */
    public var destination: Address? = null

    /** The reward for submitting the attestation. Required. */
    public lateinit var signatureReward: CurrencyAmount

    internal fun build(): XChainAddAccountCreateAttestationFields {
        val bridgeValue = requireNotNull(bridge) { "bridge is required" }
        require(::publicKey.isInitialized) { "publicKey is required" }
        require(::signature.isInitialized) { "signature is required" }
        val otherChainSourceValue = requireNotNull(otherChainSource) { "otherChainSource is required" }
        require(::amount.isInitialized) { "amount is required" }
        val attestationRewardAccountValue =
            requireNotNull(attestationRewardAccount) { "attestationRewardAccount is required" }
        val attestationSignerAccountValue =
            requireNotNull(attestationSignerAccount) { "attestationSignerAccount is required" }
        val destinationValue = requireNotNull(destination) { "destination is required" }
        require(::signatureReward.isInitialized) { "signatureReward is required" }
        return XChainAddAccountCreateAttestationFields(
            bridge = bridgeValue,
            publicKey = publicKey,
            signature = signature,
            otherChainSource = otherChainSourceValue,
            amount = amount,
            attestationRewardAccount = attestationRewardAccountValue,
            attestationSignerAccount = attestationSignerAccountValue,
            wasLockingChainSend = wasLockingChainSend,
            xChainAccountCreateCount = xChainAccountCreateCount,
            destination = destinationValue,
            signatureReward = signatureReward,
        )
    }
}
