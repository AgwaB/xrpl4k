package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for a SetRegularKey transaction.
 *
 * @property regularKey The new regular key address, or null to remove the regular key.
 */
public class SetRegularKeyFields(
    public val regularKey: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SetRegularKeyFields) return false
        return regularKey == other.regularKey
    }

    override fun hashCode(): Int = regularKey?.hashCode() ?: 0

    override fun toString(): String = "SetRegularKeyFields(regularKey=$regularKey)"
}

/**
 * DSL builder for a SetRegularKey [XrplTransaction.Unsigned].
 */
@XrplDsl
public class SetRegularKeyBuilder internal constructor() {
    public var account: Address? = null
    public var regularKey: Address? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for SetRegularKey" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.SetRegularKey,
            account = accountValue,
            fields = SetRegularKeyFields(regularKey = regularKey),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}
