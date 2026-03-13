package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for an OfferCreate transaction.
 *
 * @property takerGets The amount the taker receives.
 * @property takerPays The amount the taker pays.
 * @property expiration Optional expiration time as seconds since Ripple epoch.
 * @property offerSequence Optional sequence of an offer to cancel before placing this one.
 */
public class OfferCreateFields(
    public val takerGets: CurrencyAmount,
    public val takerPays: CurrencyAmount,
    public val expiration: UInt? = null,
    public val offerSequence: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OfferCreateFields) return false
        return takerGets == other.takerGets &&
            takerPays == other.takerPays &&
            expiration == other.expiration &&
            offerSequence == other.offerSequence
    }

    override fun hashCode(): Int {
        var result = takerGets.hashCode()
        result = 31 * result + takerPays.hashCode()
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (offerSequence?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "OfferCreateFields(" +
            "takerGets=$takerGets, " +
            "takerPays=$takerPays, " +
            "expiration=$expiration, " +
            "offerSequence=$offerSequence" +
            ")"
}

/**
 * DSL builder for an OfferCreate [XrplTransaction.Unsigned].
 */
@XrplDsl
public class OfferCreateBuilder internal constructor() {
    public var account: Address? = null
    public lateinit var takerGets: CurrencyAmount
    public lateinit var takerPays: CurrencyAmount
    public var expiration: UInt? = null
    public var offerSequence: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for OfferCreate" }
        require(::takerGets.isInitialized) { "takerGets is required for OfferCreate" }
        require(::takerPays.isInitialized) { "takerPays is required for OfferCreate" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.OfferCreate,
            account = accountValue,
            fields =
                OfferCreateFields(
                    takerGets = takerGets,
                    takerPays = takerPays,
                    expiration = expiration,
                    offerSequence = offerSequence,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}
