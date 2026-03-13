package org.xrpl.sdk.client.sugar

import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.accountLines
import org.xrpl.sdk.client.rpc.ledgerCurrent
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrNull
import org.xrpl.sdk.core.result.map
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.XrpDrops

/**
 * A balance entry for an account, representing either XRP or an issued currency.
 */
public class Balance(
    public val currency: String,
    public val value: String,
    public val issuer: Address?,
) {
    override fun equals(other: Any?): Boolean =
        other is Balance && currency == other.currency && value == other.value && issuer == other.issuer

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Balance(currency=$currency, value=$value, issuer=$issuer)"
}

/**
 * Returns the XRP balance for the given account.
 *
 * @param account the account address.
 * @return the XRP balance in drops, or [XrplFailure.NotFound] if the account doesn't exist.
 */
public suspend fun XrplClient.getXrpBalance(account: Address): XrplResult<XrpDrops> =
    accountInfo(account).map { it.balance }

/**
 * Returns all balances (XRP + issued currencies) for the given account.
 *
 * @param account the account address.
 * @return a list of [Balance] entries.
 */
public suspend fun XrplClient.getBalances(account: Address): XrplResult<List<Balance>> {
    val infoResult = accountInfo(account)
    val info =
        infoResult.getOrNull()
            ?: return XrplResult.Failure((infoResult as XrplResult.Failure).error)

    val xrpBalance =
        Balance(
            currency = "XRP",
            value = info.balance.toXrp(),
            issuer = null,
        )

    val linesResult = accountLines(account)
    val lines = linesResult.getOrNull()

    val issuedBalances =
        lines?.lines?.map { line ->
            Balance(
                currency = line.currency,
                value = line.balance,
                issuer = line.account,
            )
        } ?: emptyList()

    return XrplResult.Success(listOf(xrpBalance) + issuedBalances)
}

/**
 * Returns the current ledger index.
 *
 * @return the current (open) ledger sequence number.
 */
public suspend fun XrplClient.getLedgerIndex(): XrplResult<LedgerIndex> = ledgerCurrent()
