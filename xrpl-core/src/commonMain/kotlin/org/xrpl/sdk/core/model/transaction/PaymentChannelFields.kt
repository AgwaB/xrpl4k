package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── PaymentChannelCreate ──────────────────────────────────────────────────────

/**
 * Fields specific to a PaymentChannelCreate transaction.
 *
 * @property destination The address to receive XRP claims against the channel.
 * @property amount The amount of XRP to set aside in the channel.
 * @property settleDelay Seconds the sender must wait before closing the channel.
 * @property publicKey The 33-byte public key of the key pair used to sign claims.
 * @property cancelAfter The time after which the channel expires.
 * @property destinationTag Optional destination tag.
 */
public class PaymentChannelCreateFields(
    public val destination: Address,
    public val amount: CurrencyAmount,
    public val settleDelay: UInt,
    public val publicKey: String,
    public val cancelAfter: UInt? = null,
    public val destinationTag: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentChannelCreateFields) return false
        return destination == other.destination &&
            amount == other.amount &&
            settleDelay == other.settleDelay &&
            publicKey == other.publicKey &&
            cancelAfter == other.cancelAfter &&
            destinationTag == other.destinationTag
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + settleDelay.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + (cancelAfter?.hashCode() ?: 0)
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PaymentChannelCreateFields(" +
            "destination=$destination, " +
            "amount=$amount, " +
            "settleDelay=$settleDelay, " +
            "publicKey=$publicKey, " +
            "cancelAfter=$cancelAfter, " +
            "destinationTag=$destinationTag" +
            ")"
}

/**
 * DSL builder for [PaymentChannelCreateFields].
 */
@XrplDsl
public class PaymentChannelCreateBuilder internal constructor() {
    /** The address to receive XRP claims against the channel. Required. */
    public var destination: Address? = null

    /** The amount of XRP to set aside in the channel. Required. */
    public lateinit var amount: CurrencyAmount

    /** Seconds the sender must wait before closing the channel. Required. */
    public var settleDelay: UInt = 0u

    /** The 33-byte public key of the key pair used to sign claims. Required. */
    public lateinit var publicKey: String

    /** The time after which the channel expires. */
    public var cancelAfter: UInt? = null

    /** Optional destination tag. */
    public var destinationTag: UInt? = null

    internal fun build(): PaymentChannelCreateFields {
        val destinationValue = requireNotNull(destination) { "destination is required" }
        require(::amount.isInitialized) { "amount is required" }
        require(::publicKey.isInitialized) { "publicKey is required" }
        return PaymentChannelCreateFields(
            destination = destinationValue,
            amount = amount,
            settleDelay = settleDelay,
            publicKey = publicKey,
            cancelAfter = cancelAfter,
            destinationTag = destinationTag,
        )
    }
}

// ── PaymentChannelFund ────────────────────────────────────────────────────────

/**
 * Fields specific to a PaymentChannelFund transaction.
 *
 * @property channel The channel ID (256-bit hex).
 * @property amount The additional XRP to add to the channel.
 * @property expiration The new expiration for the channel.
 */
public class PaymentChannelFundFields(
    public val channel: String,
    public val amount: CurrencyAmount,
    public val expiration: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentChannelFundFields) return false
        return channel == other.channel &&
            amount == other.amount &&
            expiration == other.expiration
    }

    override fun hashCode(): Int {
        var result = channel.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (expiration?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PaymentChannelFundFields(" +
            "channel=$channel, " +
            "amount=$amount, " +
            "expiration=$expiration" +
            ")"
}

/**
 * DSL builder for [PaymentChannelFundFields].
 */
@XrplDsl
public class PaymentChannelFundBuilder internal constructor() {
    /** The channel ID. Required. */
    public lateinit var channel: String

    /** The additional XRP to add to the channel. Required. */
    public lateinit var amount: CurrencyAmount

    /** The new expiration for the channel. */
    public var expiration: UInt? = null

    internal fun build(): PaymentChannelFundFields {
        require(::channel.isInitialized) { "channel is required" }
        require(::amount.isInitialized) { "amount is required" }
        return PaymentChannelFundFields(
            channel = channel,
            amount = amount,
            expiration = expiration,
        )
    }
}

// ── PaymentChannelClaim ───────────────────────────────────────────────────────

/**
 * Fields specific to a PaymentChannelClaim transaction.
 *
 * @property channel The channel ID (256-bit hex).
 * @property balance The total XRP delivered by the channel after this claim.
 * @property amount The amount of XRP authorized by the claim signature.
 * @property signature The signature authorizing the claim amount.
 * @property publicKey The public key used to verify the claim signature.
 */
public class PaymentChannelClaimFields(
    public val channel: String,
    public val balance: CurrencyAmount? = null,
    public val amount: CurrencyAmount? = null,
    public val signature: String? = null,
    public val publicKey: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentChannelClaimFields) return false
        return channel == other.channel &&
            balance == other.balance &&
            amount == other.amount &&
            signature == other.signature &&
            publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = channel.hashCode()
        result = 31 * result + (balance?.hashCode() ?: 0)
        result = 31 * result + (amount?.hashCode() ?: 0)
        result = 31 * result + (signature?.hashCode() ?: 0)
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PaymentChannelClaimFields(" +
            "channel=$channel, " +
            "balance=$balance, " +
            "amount=$amount, " +
            "signature=$signature, " +
            "publicKey=$publicKey" +
            ")"
}

/**
 * DSL builder for [PaymentChannelClaimFields].
 */
@XrplDsl
public class PaymentChannelClaimBuilder internal constructor() {
    /** The channel ID. Required. */
    public lateinit var channel: String

    /** The total XRP delivered by the channel after this claim. */
    public var balance: CurrencyAmount? = null

    /** The amount of XRP authorized by the claim signature. */
    public var amount: CurrencyAmount? = null

    /** The signature authorizing the claim amount. */
    public var signature: String? = null

    /** The public key used to verify the claim signature. */
    public var publicKey: String? = null

    internal fun build(): PaymentChannelClaimFields {
        require(::channel.isInitialized) { "channel is required" }
        return PaymentChannelClaimFields(
            channel = channel,
            balance = balance,
            amount = amount,
            signature = signature,
            publicKey = publicKey,
        )
    }
}
