package org.xrpl.sdk.core.model.amount

/**
 * An amount of a Multi-Purpose Token (MPT) on the XRPL.
 */
public class MptAmount(
    /** The MPT issuance ID (192-bit hex). */
    public val mptIssuanceId: String,
    /** The integer amount value. */
    public val value: Long,
) : CurrencyAmount {
    init {
        require(mptIssuanceId.length == 48) {
            "MPT issuance ID must be 48 hex characters (192 bits). Got ${mptIssuanceId.length} characters."
        }
    }

    override fun equals(other: Any?): Boolean =
        other is MptAmount &&
            mptIssuanceId == other.mptIssuanceId &&
            value == other.value

    override fun hashCode(): Int = 31 * mptIssuanceId.hashCode() + value.hashCode()

    override fun toString(): String = "$value MPT($mptIssuanceId)"
}
