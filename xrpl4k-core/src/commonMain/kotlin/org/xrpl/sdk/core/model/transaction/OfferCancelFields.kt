package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for an OfferCancel transaction.
 *
 * @property offerSequence The sequence number of the offer to cancel.
 */
public class OfferCancelFields(
    public val offerSequence: UInt,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OfferCancelFields) return false
        return offerSequence == other.offerSequence
    }

    override fun hashCode(): Int = offerSequence.hashCode()

    override fun toString(): String = "OfferCancelFields(offerSequence=$offerSequence)"
}

/**
 * DSL builder for an OfferCancel [XrplTransaction.Unsigned].
 */
@XrplDsl
public class OfferCancelBuilder internal constructor() {
    public var account: Address? = null
    public var offerSequence: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null
    public var flags: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for OfferCancel" }
        val seq = requireNotNull(offerSequence) { "offerSequence is required for OfferCancel" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.OfferCancel,
            account = accountValue,
            fields = OfferCancelFields(offerSequence = seq),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}
