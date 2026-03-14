package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Fields common to all XRPL transactions after autofill.
 *
 * These are populated by the SDK's autofill mechanism, not by the user directly.
 *
 * @property account The sender's classic address.
 * @property fee The transaction fee in drops.
 * @property sequence The account sequence number.
 * @property lastLedgerSequence The last valid ledger index for this transaction.
 * @property accountTxnId Hash of another transaction that must succeed first.
 * @property memos Memos attached to this transaction.
 * @property signers Multi-signers.
 * @property sourceTag Source tag identifying the originator.
 * @property ticketSequence Ticket sequence to use instead of account sequence.
 * @property networkId Network ID for replay protection.
 */
public class CommonTransactionFields(
    public val account: Address,
    public val fee: XrpDrops,
    public val sequence: UInt,
    public val lastLedgerSequence: UInt,
    public val accountTxnId: TxHash? = null,
    public val memos: List<Memo> = emptyList(),
    public val signers: List<Signer> = emptyList(),
    public val sourceTag: UInt? = null,
    public val ticketSequence: UInt? = null,
    public val networkId: UInt? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommonTransactionFields) return false
        return account == other.account &&
            fee == other.fee &&
            sequence == other.sequence &&
            lastLedgerSequence == other.lastLedgerSequence &&
            accountTxnId == other.accountTxnId &&
            memos == other.memos &&
            signers == other.signers &&
            sourceTag == other.sourceTag &&
            ticketSequence == other.ticketSequence &&
            networkId == other.networkId
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + fee.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + lastLedgerSequence.hashCode()
        result = 31 * result + (accountTxnId?.hashCode() ?: 0)
        result = 31 * result + memos.hashCode()
        result = 31 * result + signers.hashCode()
        result = 31 * result + (sourceTag?.hashCode() ?: 0)
        result = 31 * result + (ticketSequence?.hashCode() ?: 0)
        result = 31 * result + (networkId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "CommonTransactionFields(" +
            "account=$account, " +
            "fee=$fee, " +
            "sequence=$sequence, " +
            "lastLedgerSequence=$lastLedgerSequence, " +
            "accountTxnId=$accountTxnId, " +
            "memos=$memos, " +
            "signers=$signers, " +
            "sourceTag=$sourceTag, " +
            "ticketSequence=$ticketSequence, " +
            "networkId=$networkId" +
            ")"
}
