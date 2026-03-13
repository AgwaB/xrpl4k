package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for a TrustSet transaction.
 *
 * @property limitAmount The limit for the trust line, as an issued currency amount.
 * @property qualityIn Optional incoming quality multiplier (value in the range 0-2000000000).
 * @property qualityOut Optional outgoing quality multiplier (value in the range 0-2000000000).
 */
public class TrustSetFields(
    public val limitAmount: IssuedAmount,
    public val qualityIn: UInt? = null,
    public val qualityOut: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrustSetFields) return false
        return limitAmount == other.limitAmount &&
            qualityIn == other.qualityIn &&
            qualityOut == other.qualityOut
    }

    override fun hashCode(): Int {
        var result = limitAmount.hashCode()
        result = 31 * result + (qualityIn?.hashCode() ?: 0)
        result = 31 * result + (qualityOut?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "TrustSetFields(" +
            "limitAmount=$limitAmount, " +
            "qualityIn=$qualityIn, " +
            "qualityOut=$qualityOut" +
            ")"
}

/**
 * DSL builder for a TrustSet [XrplTransaction.Unsigned].
 */
@XrplDsl
public class TrustSetBuilder internal constructor() {
    public var account: Address? = null
    public lateinit var limitAmount: IssuedAmount
    public var qualityIn: UInt? = null
    public var qualityOut: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for TrustSet" }
        require(::limitAmount.isInitialized) { "limitAmount is required for TrustSet" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.TrustSet,
            account = accountValue,
            fields =
                TrustSetFields(
                    limitAmount = limitAmount,
                    qualityIn = qualityIn,
                    qualityOut = qualityOut,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}
