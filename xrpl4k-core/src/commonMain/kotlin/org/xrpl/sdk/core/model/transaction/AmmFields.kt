package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

/**
 * Transaction-specific fields for an AMMCreate transaction.
 *
 * @property amount The first asset for the AMM pool.
 * @property amount2 The second asset for the AMM pool.
 * @property tradingFee The fee applied to trades, in units of 1/100,000.
 */
public class AMMCreateFields(
    public val amount: CurrencyAmount,
    public val amount2: CurrencyAmount,
    public val tradingFee: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMCreateFields) return false
        return amount == other.amount &&
            amount2 == other.amount2 &&
            tradingFee == other.tradingFee
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + amount2.hashCode()
        result = 31 * result + (tradingFee?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AMMCreateFields(" +
            "amount=$amount, " +
            "amount2=$amount2, " +
            "tradingFee=$tradingFee" +
            ")"
}

/**
 * DSL builder for an AMMCreate [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMCreateBuilder internal constructor() {
    public var account: Address? = null
    public lateinit var amount: CurrencyAmount
    public lateinit var amount2: CurrencyAmount
    public var tradingFee: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null
    public var flags: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMCreate" }
        require(::amount.isInitialized) { "amount is required for AMMCreate" }
        require(::amount2.isInitialized) { "amount2 is required for AMMCreate" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMCreate,
            account = accountValue,
            fields =
                AMMCreateFields(
                    amount = amount,
                    amount2 = amount2,
                    tradingFee = tradingFee,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}

/**
 * Transaction-specific fields for an AMMDeposit transaction.
 *
 * @property asset The first asset of the AMM pool.
 * @property asset2 The second asset of the AMM pool.
 * @property amount The first asset amount to deposit.
 * @property amount2 The second asset amount to deposit.
 * @property ePrice The price at which to deposit.
 * @property lpTokenOut The LP token amount to receive.
 * @property flags Transaction flags.
 */
public class AMMDepositFields(
    public val asset: String? = null,
    public val asset2: String? = null,
    public val amount: CurrencyAmount? = null,
    public val amount2: CurrencyAmount? = null,
    public val ePrice: CurrencyAmount? = null,
    public val lpTokenOut: CurrencyAmount? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMDepositFields) return false
        return asset == other.asset &&
            asset2 == other.asset2 &&
            amount == other.amount &&
            amount2 == other.amount2 &&
            ePrice == other.ePrice &&
            lpTokenOut == other.lpTokenOut &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = asset?.hashCode() ?: 0
        result = 31 * result + (asset2?.hashCode() ?: 0)
        result = 31 * result + (amount?.hashCode() ?: 0)
        result = 31 * result + (amount2?.hashCode() ?: 0)
        result = 31 * result + (ePrice?.hashCode() ?: 0)
        result = 31 * result + (lpTokenOut?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AMMDepositFields(" +
            "asset=$asset, " +
            "asset2=$asset2, " +
            "amount=$amount, " +
            "amount2=$amount2, " +
            "ePrice=$ePrice, " +
            "lpTokenOut=$lpTokenOut, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for an AMMDeposit [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMDepositBuilder internal constructor() {
    public var account: Address? = null
    public var asset: String? = null
    public var asset2: String? = null
    public var amount: CurrencyAmount? = null
    public var amount2: CurrencyAmount? = null
    public var ePrice: CurrencyAmount? = null
    public var lpTokenOut: CurrencyAmount? = null
    public var flags: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMDeposit" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMDeposit,
            account = accountValue,
            fields =
                AMMDepositFields(
                    asset = asset,
                    asset2 = asset2,
                    amount = amount,
                    amount2 = amount2,
                    ePrice = ePrice,
                    lpTokenOut = lpTokenOut,
                    flags = flags,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}

/**
 * Transaction-specific fields for an AMMWithdraw transaction.
 *
 * @property asset The first asset of the AMM pool.
 * @property asset2 The second asset of the AMM pool.
 * @property amount The first asset amount to withdraw.
 * @property amount2 The second asset amount to withdraw.
 * @property ePrice The price at which to withdraw.
 * @property lpTokenIn The LP token amount to spend.
 * @property flags Transaction flags.
 */
public class AMMWithdrawFields(
    public val asset: String? = null,
    public val asset2: String? = null,
    public val amount: CurrencyAmount? = null,
    public val amount2: CurrencyAmount? = null,
    public val ePrice: CurrencyAmount? = null,
    public val lpTokenIn: CurrencyAmount? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMWithdrawFields) return false
        return asset == other.asset &&
            asset2 == other.asset2 &&
            amount == other.amount &&
            amount2 == other.amount2 &&
            ePrice == other.ePrice &&
            lpTokenIn == other.lpTokenIn &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = asset?.hashCode() ?: 0
        result = 31 * result + (asset2?.hashCode() ?: 0)
        result = 31 * result + (amount?.hashCode() ?: 0)
        result = 31 * result + (amount2?.hashCode() ?: 0)
        result = 31 * result + (ePrice?.hashCode() ?: 0)
        result = 31 * result + (lpTokenIn?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AMMWithdrawFields(" +
            "asset=$asset, " +
            "asset2=$asset2, " +
            "amount=$amount, " +
            "amount2=$amount2, " +
            "ePrice=$ePrice, " +
            "lpTokenIn=$lpTokenIn, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for an AMMWithdraw [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMWithdrawBuilder internal constructor() {
    public var account: Address? = null
    public var asset: String? = null
    public var asset2: String? = null
    public var amount: CurrencyAmount? = null
    public var amount2: CurrencyAmount? = null
    public var ePrice: CurrencyAmount? = null
    public var lpTokenIn: CurrencyAmount? = null
    public var flags: UInt? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMWithdraw" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMWithdraw,
            account = accountValue,
            fields =
                AMMWithdrawFields(
                    asset = asset,
                    asset2 = asset2,
                    amount = amount,
                    amount2 = amount2,
                    ePrice = ePrice,
                    lpTokenIn = lpTokenIn,
                    flags = flags,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}

/**
 * Transaction-specific fields for an AMMVote transaction.
 *
 * @property asset The first asset of the AMM pool.
 * @property asset2 The second asset of the AMM pool.
 * @property tradingFee The proposed trading fee, in units of 1/100,000.
 */
public class AMMVoteFields(
    public val asset: String? = null,
    public val asset2: String? = null,
    public val tradingFee: UInt,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMVoteFields) return false
        return asset == other.asset &&
            asset2 == other.asset2 &&
            tradingFee == other.tradingFee
    }

    override fun hashCode(): Int {
        var result = asset?.hashCode() ?: 0
        result = 31 * result + (asset2?.hashCode() ?: 0)
        result = 31 * result + tradingFee.hashCode()
        return result
    }

    override fun toString(): String =
        "AMMVoteFields(" +
            "asset=$asset, " +
            "asset2=$asset2, " +
            "tradingFee=$tradingFee" +
            ")"
}

/**
 * DSL builder for an AMMVote [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMVoteBuilder internal constructor() {
    public var account: Address? = null
    public var asset: String? = null
    public var asset2: String? = null
    public var tradingFee: UInt = 0u
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMVote" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMVote,
            account = accountValue,
            fields =
                AMMVoteFields(
                    asset = asset,
                    asset2 = asset2,
                    tradingFee = tradingFee,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}

/**
 * Transaction-specific fields for an AMMBid transaction.
 *
 * @property asset The first asset of the AMM pool.
 * @property asset2 The second asset of the AMM pool.
 * @property bidMin Minimum LP token bid amount.
 * @property bidMax Maximum LP token bid amount.
 * @property authAccounts List of accounts authorized to trade at the discounted fee.
 */
public class AMMBidFields(
    public val asset: String? = null,
    public val asset2: String? = null,
    public val bidMin: CurrencyAmount? = null,
    public val bidMax: CurrencyAmount? = null,
    public val authAccounts: List<Address>? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMBidFields) return false
        return asset == other.asset &&
            asset2 == other.asset2 &&
            bidMin == other.bidMin &&
            bidMax == other.bidMax &&
            authAccounts == other.authAccounts
    }

    override fun hashCode(): Int {
        var result = asset?.hashCode() ?: 0
        result = 31 * result + (asset2?.hashCode() ?: 0)
        result = 31 * result + (bidMin?.hashCode() ?: 0)
        result = 31 * result + (bidMax?.hashCode() ?: 0)
        result = 31 * result + (authAccounts?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AMMBidFields(" +
            "asset=$asset, " +
            "asset2=$asset2, " +
            "bidMin=$bidMin, " +
            "bidMax=$bidMax, " +
            "authAccounts=$authAccounts" +
            ")"
}

/**
 * DSL builder for an AMMBid [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMBidBuilder internal constructor() {
    public var account: Address? = null
    public var asset: String? = null
    public var asset2: String? = null
    public var bidMin: CurrencyAmount? = null
    public var bidMax: CurrencyAmount? = null
    public var authAccounts: List<Address>? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMBid" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMBid,
            account = accountValue,
            fields =
                AMMBidFields(
                    asset = asset,
                    asset2 = asset2,
                    bidMin = bidMin,
                    bidMax = bidMax,
                    authAccounts = authAccounts,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}

/**
 * Transaction-specific fields for an AMMDelete transaction.
 *
 * @property asset The first asset of the AMM pool.
 * @property asset2 The second asset of the AMM pool.
 */
public class AMMDeleteFields(
    public val asset: String? = null,
    public val asset2: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMDeleteFields) return false
        return asset == other.asset &&
            asset2 == other.asset2
    }

    override fun hashCode(): Int {
        var result = asset?.hashCode() ?: 0
        result = 31 * result + (asset2?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AMMDeleteFields(" +
            "asset=$asset, " +
            "asset2=$asset2" +
            ")"
}

/**
 * DSL builder for an AMMDelete [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMDeleteBuilder internal constructor() {
    public var account: Address? = null
    public var asset: String? = null
    public var asset2: String? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMDelete" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMDelete,
            account = accountValue,
            fields =
                AMMDeleteFields(
                    asset = asset,
                    asset2 = asset2,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}

/**
 * Transaction-specific fields for an AMMClawback transaction.
 *
 * @property holder The account whose tokens are being clawed back.
 * @property asset The asset being clawed back.
 * @property amount The amount to claw back.
 */
public class AMMClawbackFields(
    public val holder: Address,
    public val asset: String? = null,
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AMMClawbackFields) return false
        return holder == other.holder &&
            asset == other.asset &&
            amount == other.amount
    }

    override fun hashCode(): Int {
        var result = holder.hashCode()
        result = 31 * result + (asset?.hashCode() ?: 0)
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun toString(): String =
        "AMMClawbackFields(" +
            "holder=$holder, " +
            "asset=$asset, " +
            "amount=$amount" +
            ")"
}

/**
 * DSL builder for an AMMClawback [XrplTransaction.Unsigned].
 */
@XrplDsl
public class AMMClawbackBuilder internal constructor() {
    public var account: Address? = null
    public var holder: Address? = null
    public lateinit var amount: CurrencyAmount
    public var asset: String? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for AMMClawback" }
        val holderValue = requireNotNull(holder) { "holder is required for AMMClawback" }
        require(::amount.isInitialized) { "amount is required for AMMClawback" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.AMMClawback,
            account = accountValue,
            fields =
                AMMClawbackFields(
                    holder = holderValue,
                    asset = asset,
                    amount = amount,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}
