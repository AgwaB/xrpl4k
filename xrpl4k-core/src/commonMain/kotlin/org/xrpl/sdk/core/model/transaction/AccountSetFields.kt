package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for an AccountSet transaction.
 *
 * @property clearFlag Optional flag to clear on the account.
 * @property setFlag Optional flag to set on the account.
 * @property domain Optional domain associated with the account (hex-encoded).
 * @property emailHash Optional MD5 hash of the account owner's email address (hex).
 * @property transferRate Optional transfer rate for tokens issued by this account.
 * @property tickSize Optional tick size for offers involving currencies issued by this account.
 * @property nftTokenMinter Optional authorized minter for NFTs.
 */
public class AccountSetFields(
    public val clearFlag: UInt? = null,
    public val setFlag: UInt? = null,
    public val domain: String? = null,
    public val emailHash: String? = null,
    public val transferRate: UInt? = null,
    public val tickSize: UInt? = null,
    public val nftTokenMinter: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountSetFields) return false
        return clearFlag == other.clearFlag &&
            setFlag == other.setFlag &&
            domain == other.domain &&
            emailHash == other.emailHash &&
            transferRate == other.transferRate &&
            tickSize == other.tickSize &&
            nftTokenMinter == other.nftTokenMinter
    }

    override fun hashCode(): Int {
        var result = clearFlag?.hashCode() ?: 0
        result = 31 * result + (setFlag?.hashCode() ?: 0)
        result = 31 * result + (domain?.hashCode() ?: 0)
        result = 31 * result + (emailHash?.hashCode() ?: 0)
        result = 31 * result + (transferRate?.hashCode() ?: 0)
        result = 31 * result + (tickSize?.hashCode() ?: 0)
        result = 31 * result + (nftTokenMinter?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AccountSetFields(" +
            "clearFlag=$clearFlag, " +
            "setFlag=$setFlag, " +
            "domain=$domain, " +
            "emailHash=$emailHash, " +
            "transferRate=$transferRate, " +
            "tickSize=$tickSize, " +
            "nftTokenMinter=$nftTokenMinter" +
            ")"
}

/**
 * DSL builder for an AccountSet [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AccountSetBuilder internal constructor() {
    public var account: Address? = null
    public var clearFlag: UInt? = null
    public var setFlag: UInt? = null
    public var domain: String? = null
    public var emailHash: String? = null
    public var transferRate: UInt? = null
    public var tickSize: UInt? = null
    public var nftTokenMinter: Address? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null
    public var flags: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AccountSet" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AccountSet,
            account = accountValue,
            fields =
                AccountSetFields(
                    clearFlag = clearFlag,
                    setFlag = setFlag,
                    domain = domain,
                    emailHash = emailHash,
                    transferRate = transferRate,
                    tickSize = tickSize,
                    nftTokenMinter = nftTokenMinter,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}
