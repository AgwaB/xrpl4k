package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.type.Hash256

/**
 * A trust line between two accounts for a non-XRP currency.
 *
 * A single `RippleState` object represents the mutual trust line between two accounts.
 * The "low" and "high" sides are determined by comparing the two account addresses
 * numerically — the lower address is always the low side.
 *
 * @property balance The net balance of the trust line from the low account's perspective.
 * @property lowLimit The maximum amount the low account trusts the high account to issue.
 * @property highLimit The maximum amount the high account trusts the low account to issue.
 * @property flags Bit-flags controlling trust line behaviour (e.g., authorise, noRipple).
 * @property lowNode Hint for the low account's owner directory.
 * @property highNode Hint for the high account's owner directory.
 * @property lowQualityIn Inbound quality setting for the low account (0 = default).
 * @property lowQualityOut Outbound quality setting for the low account (0 = default).
 * @property highQualityIn Inbound quality setting for the high account (0 = default).
 * @property highQualityOut Outbound quality setting for the high account (0 = default).
 * @property previousTxnID Hash of the most recent transaction that modified this object.
 * @property previousTxnLgrSeq Ledger index of the most recent transaction that modified this object.
 */
public class RippleState(
    override val index: Hash256,
    public val balance: IssuedAmount,
    public val lowLimit: IssuedAmount,
    public val highLimit: IssuedAmount,
    public val flags: UInt = 0u,
    public val lowNode: String? = null,
    public val highNode: String? = null,
    public val lowQualityIn: UInt? = null,
    public val lowQualityOut: UInt? = null,
    public val highQualityIn: UInt? = null,
    public val highQualityOut: UInt? = null,
    public val previousTxnID: Hash256? = null,
    public val previousTxnLgrSeq: UInt? = null,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.RippleState

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RippleState) return false
        return index == other.index &&
            balance == other.balance &&
            lowLimit == other.lowLimit &&
            highLimit == other.highLimit &&
            flags == other.flags &&
            lowNode == other.lowNode &&
            highNode == other.highNode &&
            lowQualityIn == other.lowQualityIn &&
            lowQualityOut == other.lowQualityOut &&
            highQualityIn == other.highQualityIn &&
            highQualityOut == other.highQualityOut &&
            previousTxnID == other.previousTxnID &&
            previousTxnLgrSeq == other.previousTxnLgrSeq
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + lowLimit.hashCode()
        result = 31 * result + highLimit.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + (lowNode?.hashCode() ?: 0)
        result = 31 * result + (highNode?.hashCode() ?: 0)
        result = 31 * result + (lowQualityIn?.hashCode() ?: 0)
        result = 31 * result + (lowQualityOut?.hashCode() ?: 0)
        result = 31 * result + (highQualityIn?.hashCode() ?: 0)
        result = 31 * result + (highQualityOut?.hashCode() ?: 0)
        result = 31 * result + (previousTxnID?.hashCode() ?: 0)
        result = 31 * result + (previousTxnLgrSeq?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "RippleState(balance=$balance, lowLimit=$lowLimit, highLimit=$highLimit)"
}
