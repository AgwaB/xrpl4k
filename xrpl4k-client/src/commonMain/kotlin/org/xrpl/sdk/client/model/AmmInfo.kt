package org.xrpl.sdk.client.model

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex

public class AmmInfo(
    public val account: Address?,
    public val amount: JsonElement?,
    public val amount2: JsonElement?,
    public val tradingFee: Long?,
    public val lpToken: JsonElement?,
    public val ledgerIndex: LedgerIndex?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AmmInfo) return false
        return account == other.account &&
            amount == other.amount &&
            amount2 == other.amount2 &&
            tradingFee == other.tradingFee &&
            lpToken == other.lpToken &&
            ledgerIndex == other.ledgerIndex
    }

    override fun hashCode(): Int {
        var result = (account?.hashCode() ?: 0)
        result = 31 * result + (amount?.hashCode() ?: 0)
        result = 31 * result + (amount2?.hashCode() ?: 0)
        result = 31 * result + (tradingFee?.hashCode() ?: 0)
        result = 31 * result + (lpToken?.hashCode() ?: 0)
        result = 31 * result + (ledgerIndex?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "AmmInfo(" +
            "account=$account, " +
            "amount=$amount, " +
            "amount2=$amount2, " +
            "tradingFee=$tradingFee, " +
            "lpToken=$lpToken, " +
            "ledgerIndex=$ledgerIndex" +
            ")"
}
