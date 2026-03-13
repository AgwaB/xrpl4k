package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

/**
 * A step in a payment path on the XRPL.
 *
 * @property account Optional intermediate account.
 * @property currency Optional currency code at this step.
 * @property issuer Optional issuer at this step.
 */
public class PathStep(
    public val account: Address? = null,
    public val currency: String? = null,
    public val issuer: Address? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathStep) return false
        return account == other.account &&
            currency == other.currency &&
            issuer == other.issuer
    }

    override fun hashCode(): Int {
        var result = account?.hashCode() ?: 0
        result = 31 * result + (currency?.hashCode() ?: 0)
        result = 31 * result + (issuer?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "PathStep(account=$account, currency=$currency, issuer=$issuer)"
}

/**
 * Transaction-specific fields for a Payment transaction.
 *
 * @property destination The destination account address.
 * @property amount The amount to deliver.
 * @property sendMax The maximum amount to spend.
 * @property deliverMin The minimum amount to deliver.
 * @property destinationTag Optional destination tag.
 * @property invoiceId Optional invoice identifier (hex).
 * @property paths Optional payment paths.
 */
public class PaymentFields(
    public val destination: Address,
    public val amount: CurrencyAmount,
    public val sendMax: CurrencyAmount? = null,
    public val deliverMin: CurrencyAmount? = null,
    public val destinationTag: UInt? = null,
    public val invoiceId: String? = null,
    public val paths: List<List<PathStep>>? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaymentFields) return false
        return destination == other.destination &&
            amount == other.amount &&
            sendMax == other.sendMax &&
            deliverMin == other.deliverMin &&
            destinationTag == other.destinationTag &&
            invoiceId == other.invoiceId &&
            paths == other.paths
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (sendMax?.hashCode() ?: 0)
        result = 31 * result + (deliverMin?.hashCode() ?: 0)
        result = 31 * result + (destinationTag?.hashCode() ?: 0)
        result = 31 * result + (invoiceId?.hashCode() ?: 0)
        result = 31 * result + (paths?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PaymentFields(" +
            "destination=$destination, " +
            "amount=$amount, " +
            "sendMax=$sendMax, " +
            "deliverMin=$deliverMin, " +
            "destinationTag=$destinationTag, " +
            "invoiceId=$invoiceId, " +
            "paths=$paths" +
            ")"
}

/**
 * DSL builder for a Payment [XrplTransaction.Unsigned].
 */
@XrplDsl
public class PaymentBuilder internal constructor() {
    public var account: Address? = null
    public var destination: Address? = null
    public lateinit var amount: CurrencyAmount
    public var sendMax: CurrencyAmount? = null
    public var deliverMin: CurrencyAmount? = null
    public var destinationTag: UInt? = null
    public var invoiceId: String? = null
    public var paths: List<List<PathStep>>? = null
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for Payment" }
        val destinationValue = requireNotNull(destination) { "destination is required for Payment" }
        require(::amount.isInitialized) { "amount is required for Payment" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.Payment,
            account = accountValue,
            fields =
                PaymentFields(
                    destination = destinationValue,
                    amount = amount,
                    sendMax = sendMax,
                    deliverMin = deliverMin,
                    destinationTag = destinationTag,
                    invoiceId = invoiceId,
                    paths = paths,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
        )
    }
}
