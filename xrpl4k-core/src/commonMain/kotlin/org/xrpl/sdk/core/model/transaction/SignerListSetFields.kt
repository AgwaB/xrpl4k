package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

/**
 * An entry in a signer list.
 *
 * @property account The signer's account address.
 * @property signerWeight The weight of this signer's signature.
 */
public class SignerEntry(
    public val account: Address,
    public val signerWeight: UInt,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignerEntry) return false
        return account == other.account &&
            signerWeight == other.signerWeight
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + signerWeight.hashCode()
        return result
    }

    override fun toString(): String = "SignerEntry(account=$account, signerWeight=$signerWeight)"
}

/**
 * Transaction-specific fields for a SignerListSet transaction.
 *
 * @property signerQuorum The minimum combined weight required to authorize a transaction.
 * @property signerEntries The list of signers. Pass an empty list to delete the signer list.
 */
public class SignerListSetFields(
    public val signerQuorum: UInt,
    public val signerEntries: List<SignerEntry>,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignerListSetFields) return false
        return signerQuorum == other.signerQuorum &&
            signerEntries == other.signerEntries
    }

    override fun hashCode(): Int {
        var result = signerQuorum.hashCode()
        result = 31 * result + signerEntries.hashCode()
        return result
    }

    override fun toString(): String = "SignerListSetFields(signerQuorum=$signerQuorum, signerEntries=$signerEntries)"
}

/**
 * DSL builder for a SignerListSet [XrplTransaction.Unsigned].
 */
@XrplDsl
public class SignerListSetBuilder internal constructor() {
    public var account: Address? = null
    public var signerQuorum: UInt? = null
    public var signerEntries: List<SignerEntry> = emptyList()
    public var memos: List<Memo> = emptyList()
    public var sourceTag: UInt? = null
    public var flags: UInt? = null

    private val memoList = mutableListOf<Memo>()

    public fun memo(block: MemoBuilder.() -> Unit) {
        memoList.add(MemoBuilder().apply(block).build())
    }

    internal fun build(): XrplTransaction.Unsigned {
        val accountValue = requireNotNull(account) { "account is required for SignerListSet" }
        val quorum = requireNotNull(signerQuorum) { "signerQuorum is required for SignerListSet" }
        return XrplTransaction.Unsigned(
            transactionType = TransactionType.SignerListSet,
            account = accountValue,
            fields =
                SignerListSetFields(
                    signerQuorum = quorum,
                    signerEntries = signerEntries,
                ),
            memos = if (memoList.isNotEmpty()) memoList.toList() else memos,
            sourceTag = sourceTag,
            flags = flags,
        )
    }
}
