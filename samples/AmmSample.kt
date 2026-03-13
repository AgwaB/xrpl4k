import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.ammInfo
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow

/**
 * Demonstrates querying an Automated Market Maker (AMM) pool on the XRPL.
 *
 * An AMM pool is identified by its two asset specifiers. Each asset is either:
 *   - XRP:        {"currency": "XRP"}
 *   - Issued IOU: {"currency": "USD", "issuer": "rIssuerAddressHere"}
 *
 * The ammInfo call returns the pool's account address, current reserves for both
 * assets, the LP token supply, and the trading fee (in units of 1/100,000).
 *
 * AMM pools were introduced in the XLS-30 amendment and are available on Mainnet
 * and Testnet from ledger version 1.12+.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        // --- 1. Build asset specifiers as JSON ---
        // XRP is represented by {"currency": "XRP"} with no issuer field.
        val xrp = buildJsonObject { put("currency", "XRP") }

        // An issued currency (IOU) requires both currency code and issuer address.
        val usd = buildJsonObject {
            put("currency", "USD")
            put("issuer", "rIssuerAddressHere")
        }

        // --- 2. Query the AMM pool ---
        // Pass the two assets in either order; the ledger normalises them internally.
        when (val result = client.ammInfo(asset = xrp, asset2 = usd)) {
            is XrplResult.Success -> {
                val amm = result.value
                println("AMM pool found:")
                println("  account:     ${amm.account}")
                println("  ledgerIndex: ${amm.ledgerIndex}")

                // amount and amount2 are JsonElement because XRPL amounts can be
                // either a drops string (XRP) or an {"currency","issuer","value"} object.
                println("  amount:      ${amm.amount}")
                println("  amount2:     ${amm.amount2}")
                println("  lpToken:     ${amm.lpToken}")

                // tradingFee is in units of 1/100,000; divide by 1000 for basis points.
                amm.tradingFee?.let { fee ->
                    val bps = fee / 1000.0
                    println("  tradingFee:  $fee (${bps} bps)")
                }
            }
            is XrplResult.Failure ->
                // The pool may not exist yet, or the amendment may not be active.
                println("ammInfo failed: ${result.error}")
        }

        // --- 3. Query with only one asset ---
        // If your Clio server supports it, you can omit asset2 and specify
        // the pool's AMM account address instead. Here we show the two-asset form only.
        val result2 = client.ammInfo(asset = xrp).getOrThrow()
        println("\nAMM query (single asset): account=${result2.account}")
    }
}
