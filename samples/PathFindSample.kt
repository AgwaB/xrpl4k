import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.ripplePathFind
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address

/**
 * Demonstrates finding payment paths using ripplePathFind.
 *
 * ripplePathFind is the one-shot HTTP equivalent of the WebSocket path_find command.
 * It finds the cheapest set of paths for sending a specific currency from a source
 * account to a destination, without the source needing to hold that currency directly.
 *
 * Use cases:
 *   - Cross-currency payments (send XRP, deliver USD)
 *   - Deliver issued currencies the sender doesn't hold via auto-bridging
 *
 * Each PathAlternative contains:
 *   - pathsComputed: one or more hop sequences through intermediate accounts/offers
 *   - sourceAmount:  how much the sender would spend on this path
 *   - destinationAmount: confirmed delivery amount
 *
 * Note: path_find (the WebSocket subscription variant) is deprecated in this SDK;
 * always use ripplePathFind for one-shot path queries.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val sourceAccount      = Address("rSenderAddressHere")
        val destinationAccount = Address("rReceiverAddressHere")

        // --- 1. Simple XRP destination amount ---
        // Ask the network: "what paths exist to deliver exactly 10 XRP?"
        // XRP amounts are plain drop strings; 10 XRP = 10_000_000 drops.
        val xrpAmount = buildJsonObject { put("value", "10000000") }  // expressed as drops string

        when (val result = client.ripplePathFind(
            sourceAccount      = sourceAccount,
            destinationAccount = destinationAccount,
            destinationAmount  = xrpAmount,
        )) {
            is XrplResult.Success -> {
                val pathResult = result.value
                println("Paths from ${pathResult.sourceAccount} to ${pathResult.destinationAccount}:")
                println("  destination amount: ${pathResult.destinationAmount}")
                for ((i, alt) in pathResult.alternatives.withIndex()) {
                    println("  Alternative $i:")
                    println("    sourceAmount: ${alt.sourceAmount}")
                    println("    hops:         ${alt.pathsComputed.size} path(s)")
                }
            }
            is XrplResult.Failure -> println("ripplePathFind failed: ${result.error}")
        }

        // --- 2. IOU destination amount ---
        // Find paths to deliver 100 USD to the destination.
        val usdAmount = buildJsonObject {
            put("currency", "USD")
            put("issuer",   "rIssuerAddressHere")
            put("value",    "100")
        }

        val iouResult = client.ripplePathFind(
            sourceAccount      = sourceAccount,
            destinationAccount = destinationAccount,
            destinationAmount  = usdAmount,
        ).getOrThrow()

        println("\nUSD delivery paths (${iouResult.alternatives.size} alternatives):")
        for (alt in iouResult.alternatives) {
            println("  source spends: ${alt.sourceAmount}")
        }

        // --- 3. Restrict source currencies ---
        // Optionally hint which currencies the source is willing to spend.
        // The network will only consider paths that consume those currencies.
        val xrpCurrency = buildJsonObject { put("currency", "XRP") }

        val restrictedResult = client.ripplePathFind(
            sourceAccount      = sourceAccount,
            destinationAccount = destinationAccount,
            destinationAmount  = usdAmount,
            sourceCurrencies   = listOf(xrpCurrency),
        ).getOrThrow()

        println("\nXRP-only source paths: ${restrictedResult.alternatives.size} alternatives")
    }
}
