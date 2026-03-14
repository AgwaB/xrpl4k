package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── NFTokenMint ───────────────────────────────────────────────────────────────

/**
 * Fields specific to an NFTokenMint transaction.
 *
 * @property nfTokenTaxon The taxon associated with the NFT.
 * @property issuer The issuer of the token if different from the account.
 * @property transferFee The fee charged when transferring this NFT (0–50000).
 * @property uri Optional URI pointing to the NFT data.
 * @property flags Flags controlling NFT behavior.
 */
public class NFTokenMintFields(
    public val nfTokenTaxon: UInt,
    public val issuer: Address? = null,
    public val transferFee: UInt? = null,
    public val uri: String? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenMintFields) return false
        return nfTokenTaxon == other.nfTokenTaxon &&
            issuer == other.issuer &&
            transferFee == other.transferFee &&
            uri == other.uri &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = nfTokenTaxon.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + (transferFee?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NFTokenMintFields(" +
            "nfTokenTaxon=$nfTokenTaxon, " +
            "issuer=$issuer, " +
            "transferFee=$transferFee, " +
            "uri=$uri, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [NFTokenMintFields].
 */
@XrplDsl
public class NFTokenMintBuilder internal constructor() {
    /** The taxon associated with the NFT. Required. */
    public var nfTokenTaxon: UInt = 0u

    /** The issuer of the token if different from the account. */
    public var issuer: Address? = null

    /** The fee charged when transferring this NFT (0–50000). */
    public var transferFee: UInt? = null

    /** Optional URI pointing to the NFT data. */
    public var uri: String? = null

    /** Flags controlling NFT behavior. */
    public var flags: UInt? = null

    internal fun build(): NFTokenMintFields =
        NFTokenMintFields(
            nfTokenTaxon = nfTokenTaxon,
            issuer = issuer,
            transferFee = transferFee,
            uri = uri,
            flags = flags,
        )
}

// ── NFTokenBurn ───────────────────────────────────────────────────────────────

/**
 * Fields specific to an NFTokenBurn transaction.
 *
 * @property nfTokenId The ID of the NFT to destroy.
 */
public class NFTokenBurnFields(
    public val nfTokenId: String,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenBurnFields) return false
        return nfTokenId == other.nfTokenId
    }

    override fun hashCode(): Int = nfTokenId.hashCode()

    override fun toString(): String = "NFTokenBurnFields(nfTokenId=$nfTokenId)"
}

/**
 * DSL builder for [NFTokenBurnFields].
 */
@XrplDsl
public class NFTokenBurnBuilder internal constructor() {
    /** The ID of the NFT to destroy. Required. */
    public lateinit var nfTokenId: String

    internal fun build(): NFTokenBurnFields {
        require(::nfTokenId.isInitialized) { "nfTokenId is required" }
        return NFTokenBurnFields(nfTokenId = nfTokenId)
    }
}

// ── NFTokenCreateOffer ────────────────────────────────────────────────────────

/**
 * Fields specific to an NFTokenCreateOffer transaction.
 *
 * @property nfTokenId The ID of the NFT.
 * @property amount The amount offered or requested.
 * @property destination Optional address that can accept the offer.
 * @property owner Owner address (for buy offers).
 * @property expiration The time after which the offer expires.
 * @property flags Flags controlling offer behavior.
 */
public class NFTokenCreateOfferFields(
    public val nfTokenId: String,
    public val amount: CurrencyAmount,
    public val destination: Address? = null,
    public val owner: Address? = null,
    public val expiration: UInt? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenCreateOfferFields) return false
        return nfTokenId == other.nfTokenId &&
            amount == other.amount &&
            destination == other.destination &&
            owner == other.owner &&
            expiration == other.expiration &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = nfTokenId.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (destination?.hashCode() ?: 0)
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NFTokenCreateOfferFields(" +
            "nfTokenId=$nfTokenId, " +
            "amount=$amount, " +
            "destination=$destination, " +
            "owner=$owner, " +
            "expiration=$expiration, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [NFTokenCreateOfferFields].
 */
@XrplDsl
public class NFTokenCreateOfferBuilder internal constructor() {
    /** The ID of the NFT. Required. */
    public lateinit var nfTokenId: String

    /** The amount offered or requested. Required. */
    public lateinit var amount: CurrencyAmount

    /** Optional address that can accept the offer. */
    public var destination: Address? = null

    /** Owner address (for buy offers). */
    public var owner: Address? = null

    /** The time after which the offer expires. */
    public var expiration: UInt? = null

    /** Flags controlling offer behavior. */
    public var flags: UInt? = null

    internal fun build(): NFTokenCreateOfferFields {
        require(::nfTokenId.isInitialized) { "nfTokenId is required" }
        require(::amount.isInitialized) { "amount is required" }
        return NFTokenCreateOfferFields(
            nfTokenId = nfTokenId,
            amount = amount,
            destination = destination,
            owner = owner,
            expiration = expiration,
            flags = flags,
        )
    }
}

// ── NFTokenCancelOffer ────────────────────────────────────────────────────────

/**
 * Fields specific to an NFTokenCancelOffer transaction.
 *
 * @property nfTokenOffers The list of NFTokenOffer IDs to cancel.
 */
public class NFTokenCancelOfferFields(
    public val nfTokenOffers: List<String>,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenCancelOfferFields) return false
        return nfTokenOffers == other.nfTokenOffers
    }

    override fun hashCode(): Int = nfTokenOffers.hashCode()

    override fun toString(): String = "NFTokenCancelOfferFields(nfTokenOffers=$nfTokenOffers)"
}

/**
 * DSL builder for [NFTokenCancelOfferFields].
 */
@XrplDsl
public class NFTokenCancelOfferBuilder internal constructor() {
    /** The list of NFTokenOffer IDs to cancel. Required. */
    public var nfTokenOffers: List<String> = emptyList()

    internal fun build(): NFTokenCancelOfferFields {
        require(nfTokenOffers.isNotEmpty()) { "nfTokenOffers must not be empty" }
        return NFTokenCancelOfferFields(nfTokenOffers = nfTokenOffers)
    }
}

// ── NFTokenAcceptOffer ────────────────────────────────────────────────────────

/**
 * Fields specific to an NFTokenAcceptOffer transaction.
 *
 * @property nfTokenSellOffer ID of the sell offer to accept.
 * @property nfTokenBuyOffer ID of the buy offer to accept.
 * @property nfTokenBrokerFee Broker fee to split between buy and sell offers.
 */
public class NFTokenAcceptOfferFields(
    public val nfTokenSellOffer: String? = null,
    public val nfTokenBuyOffer: String? = null,
    public val nfTokenBrokerFee: CurrencyAmount? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenAcceptOfferFields) return false
        return nfTokenSellOffer == other.nfTokenSellOffer &&
            nfTokenBuyOffer == other.nfTokenBuyOffer &&
            nfTokenBrokerFee == other.nfTokenBrokerFee
    }

    override fun hashCode(): Int {
        var result = (nfTokenSellOffer?.hashCode() ?: 0)
        result = 31 * result + (nfTokenBuyOffer?.hashCode() ?: 0)
        result = 31 * result + (nfTokenBrokerFee?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NFTokenAcceptOfferFields(" +
            "nfTokenSellOffer=$nfTokenSellOffer, " +
            "nfTokenBuyOffer=$nfTokenBuyOffer, " +
            "nfTokenBrokerFee=$nfTokenBrokerFee" +
            ")"
}

/**
 * DSL builder for [NFTokenAcceptOfferFields].
 */
@XrplDsl
public class NFTokenAcceptOfferBuilder internal constructor() {
    /** ID of the sell offer to accept. */
    public var nfTokenSellOffer: String? = null

    /** ID of the buy offer to accept. */
    public var nfTokenBuyOffer: String? = null

    /** Broker fee to split between buy and sell offers. */
    public var nfTokenBrokerFee: CurrencyAmount? = null

    internal fun build(): NFTokenAcceptOfferFields =
        NFTokenAcceptOfferFields(
            nfTokenSellOffer = nfTokenSellOffer,
            nfTokenBuyOffer = nfTokenBuyOffer,
            nfTokenBrokerFee = nfTokenBrokerFee,
        )
}

// ── NFTokenModify ─────────────────────────────────────────────────────────────

/**
 * Fields specific to an NFTokenModify transaction.
 *
 * @property nfTokenId The ID of the NFT to modify.
 * @property owner Owner of the NFT if different from the transaction account.
 * @property uri The new URI for the NFT.
 */
public class NFTokenModifyFields(
    public val nfTokenId: String,
    public val owner: Address? = null,
    public val uri: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NFTokenModifyFields) return false
        return nfTokenId == other.nfTokenId &&
            owner == other.owner &&
            uri == other.uri
    }

    override fun hashCode(): Int {
        var result = nfTokenId.hashCode()
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "NFTokenModifyFields(" +
            "nfTokenId=$nfTokenId, " +
            "owner=$owner, " +
            "uri=$uri" +
            ")"
}

/**
 * DSL builder for [NFTokenModifyFields].
 */
@XrplDsl
public class NFTokenModifyBuilder internal constructor() {
    /** The ID of the NFT to modify. Required. */
    public lateinit var nfTokenId: String

    /** Owner of the NFT if different from the transaction account. */
    public var owner: Address? = null

    /** The new URI for the NFT. */
    public var uri: String? = null

    internal fun build(): NFTokenModifyFields {
        require(::nfTokenId.isInitialized) { "nfTokenId is required" }
        return NFTokenModifyFields(
            nfTokenId = nfTokenId,
            owner = owner,
            uri = uri,
        )
    }
}
