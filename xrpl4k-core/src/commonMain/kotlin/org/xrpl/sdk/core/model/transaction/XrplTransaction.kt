package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Represents the lifecycle states of an XRPL transaction.
 *
 * A transaction progresses through three states:
 * 1. [Unsigned] – created by the user with required transaction fields.
 * 2. [Filled] – after autofill, containing fee, sequence, and network fields.
 * 3. [Signed] – after signing, containing the serialized blob and hash.
 *
 * Use an exhaustive `when` expression to handle all states safely.
 */
public sealed interface XrplTransaction {
    /** The XRPL transaction type (e.g., Payment, OfferCreate, TrustSet). */
    public val transactionType: TransactionType

    /**
     * An unsigned transaction created by the user.
     *
     * @property transactionType The type of transaction.
     * @property account The sender's classic address.
     * @property fields Transaction-type-specific fields.
     * @property memos Optional memos attached to the transaction.
     * @property sourceTag Optional source tag identifying the originator.
     * @property flags Optional transaction flags (bitmask). Use constants from [TransactionFlags].
     */
    public class Unsigned(
        override val transactionType: TransactionType,
        public val account: Address,
        public val fields: TransactionFields,
        public val memos: List<Memo> = emptyList(),
        public val sourceTag: UInt? = null,
        public val flags: UInt? = null,
    ) : XrplTransaction {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Unsigned) return false
            return transactionType == other.transactionType &&
                account == other.account &&
                fields == other.fields &&
                memos == other.memos &&
                sourceTag == other.sourceTag &&
                flags == other.flags
        }

        override fun hashCode(): Int {
            var result = transactionType.hashCode()
            result = 31 * result + account.hashCode()
            result = 31 * result + fields.hashCode()
            result = 31 * result + memos.hashCode()
            result = 31 * result + (sourceTag?.hashCode() ?: 0)
            result = 31 * result + (flags?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String =
            "XrplTransaction.Unsigned(" +
                "transactionType=$transactionType, " +
                "account=$account, " +
                "fields=$fields, " +
                "memos=$memos, " +
                "sourceTag=$sourceTag, " +
                "flags=$flags" +
                ")"
    }

    /**
     * A transaction after autofill — contains fee, sequence, and network fields.
     *
     * Constructed internally by the autofill mechanism; callers cannot create this directly.
     *
     * @property transactionType The type of transaction.
     * @property account The sender's classic address.
     * @property fields Transaction-type-specific fields.
     * @property memos Optional memos attached to the transaction.
     * @property sourceTag Optional source tag identifying the originator.
     * @property fee The transaction fee in drops.
     * @property sequence The account sequence number.
     * @property lastLedgerSequence The last valid ledger index for this transaction.
     * @property accountTxnId Hash of another transaction that must succeed first.
     * @property signers Multi-signers for this transaction.
     * @property ticketSequence Ticket sequence to use instead of account sequence.
     * @property networkId Network ID for replay protection.
     * @property flags Optional transaction flags (bitmask). Use constants from [TransactionFlags].
     */
    public class Filled internal constructor(
        override val transactionType: TransactionType,
        public val account: Address,
        public val fields: TransactionFields,
        public val memos: List<Memo> = emptyList(),
        public val sourceTag: UInt? = null,
        public val fee: XrpDrops,
        public val sequence: UInt,
        public val lastLedgerSequence: UInt,
        public val accountTxnId: TxHash? = null,
        public val signers: List<Signer> = emptyList(),
        public val ticketSequence: UInt? = null,
        public val networkId: UInt? = null,
        public val flags: UInt? = null,
    ) : XrplTransaction {
        public companion object {
            /**
             * Creates a [Filled] transaction from its components.
             *
             * This factory provides cross-module access to the `internal` constructor,
             * enabling `xrpl-client` and other modules to construct filled transactions.
             */
            public fun create(
                transactionType: TransactionType,
                account: Address,
                fields: TransactionFields,
                memos: List<Memo> = emptyList(),
                sourceTag: UInt? = null,
                fee: XrpDrops,
                sequence: UInt,
                lastLedgerSequence: UInt,
                accountTxnId: TxHash? = null,
                signers: List<Signer> = emptyList(),
                ticketSequence: UInt? = null,
                networkId: UInt? = null,
                flags: UInt? = null,
            ): Filled =
                Filled(
                    transactionType = transactionType,
                    account = account,
                    fields = fields,
                    memos = memos,
                    sourceTag = sourceTag,
                    fee = fee,
                    sequence = sequence,
                    lastLedgerSequence = lastLedgerSequence,
                    accountTxnId = accountTxnId,
                    signers = signers,
                    ticketSequence = ticketSequence,
                    networkId = networkId,
                    flags = flags,
                )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Filled) return false
            return transactionType == other.transactionType &&
                account == other.account &&
                fields == other.fields &&
                memos == other.memos &&
                sourceTag == other.sourceTag &&
                fee == other.fee &&
                sequence == other.sequence &&
                lastLedgerSequence == other.lastLedgerSequence &&
                accountTxnId == other.accountTxnId &&
                signers == other.signers &&
                ticketSequence == other.ticketSequence &&
                networkId == other.networkId &&
                flags == other.flags
        }

        override fun hashCode(): Int {
            var result = transactionType.hashCode()
            result = 31 * result + account.hashCode()
            result = 31 * result + fields.hashCode()
            result = 31 * result + memos.hashCode()
            result = 31 * result + (sourceTag?.hashCode() ?: 0)
            result = 31 * result + fee.hashCode()
            result = 31 * result + sequence.hashCode()
            result = 31 * result + lastLedgerSequence.hashCode()
            result = 31 * result + (accountTxnId?.hashCode() ?: 0)
            result = 31 * result + signers.hashCode()
            result = 31 * result + (ticketSequence?.hashCode() ?: 0)
            result = 31 * result + (networkId?.hashCode() ?: 0)
            result = 31 * result + (flags?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String =
            "XrplTransaction.Filled(" +
                "transactionType=$transactionType, " +
                "account=$account, " +
                "fields=$fields, " +
                "memos=$memos, " +
                "sourceTag=$sourceTag, " +
                "fee=$fee, " +
                "sequence=$sequence, " +
                "lastLedgerSequence=$lastLedgerSequence, " +
                "accountTxnId=$accountTxnId, " +
                "signers=$signers, " +
                "ticketSequence=$ticketSequence, " +
                "networkId=$networkId, " +
                "flags=$flags" +
                ")"
    }

    /**
     * A signed transaction ready for submission to the XRPL network.
     *
     * Constructed internally by the signing mechanism; callers cannot create this directly.
     *
     * @property transactionType The type of transaction.
     * @property txBlob The hex-encoded signed transaction blob.
     * @property hash The transaction hash.
     */
    public class Signed internal constructor(
        override val transactionType: TransactionType,
        public val txBlob: String,
        public val hash: TxHash,
    ) : XrplTransaction {
        public companion object {
            /**
             * Creates a [Signed] transaction from its components.
             *
             * This factory provides cross-module access to the `internal` constructor,
             * enabling `xrpl-client` and other modules to construct signed transactions.
             *
             * @param transactionType The type of transaction.
             * @param txBlob The hex-encoded signed transaction blob.
             * @param hash The transaction hash.
             * @throws IllegalArgumentException if [txBlob] is empty.
             */
            public fun create(
                transactionType: TransactionType,
                txBlob: String,
                hash: TxHash,
            ): Signed {
                require(txBlob.isNotEmpty()) { "txBlob must not be empty." }
                return Signed(transactionType, txBlob, hash)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Signed) return false
            return transactionType == other.transactionType &&
                txBlob == other.txBlob &&
                hash == other.hash
        }

        override fun hashCode(): Int {
            var result = transactionType.hashCode()
            result = 31 * result + txBlob.hashCode()
            result = 31 * result + hash.hashCode()
            return result
        }

        override fun toString(): String =
            "XrplTransaction.Signed(" +
                "transactionType=$transactionType, " +
                "txBlob=$txBlob, " +
                "hash=$hash" +
                ")"
    }
}
