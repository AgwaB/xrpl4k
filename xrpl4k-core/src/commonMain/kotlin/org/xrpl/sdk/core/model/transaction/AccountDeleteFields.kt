package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for an AccountDelete transaction.
 *
 * @property destination The account to receive the remaining XRP balance.
 * @property destinationTag Optional destination tag for the receiving account.
 */
public class AccountDeleteFields(
    public val destination: Address,
    public val destinationTag: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountDeleteFields) return false
        return destination == other.destination &&
            destinationTag == other.destinationTag
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "AccountDeleteFields(destination=$destination, destinationTag=$destinationTag)"
}

/**
 * DSL builder for an AccountDelete [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AccountDeleteBuilder internal constructor() {
    public var account: Address? = null
    public var destination: Address? = null
    public var destinationTag: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null
    public var flags: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AccountDelete" }
        val destinationValue = requireNotNull(destination) { "destination is required for AccountDelete" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AccountDelete,
            account = accountValue,
            fields =
                AccountDeleteFields(
                    destination = destinationValue,
                    destinationTag = destinationTag,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}
