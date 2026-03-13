import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.bookChanges
import org.xrpl.sdk.client.rpc.bookOffers
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow

/**
 * Demonstrates querying the XRPL decentralised exchange (DEX) order book.
 *
 * bookOffers returns the current open offers for a currency pair, sorted by
 * quality (exchange rate). The caller specifies which side of the book to view
 * by setting takerGets (what they'd receive) and takerPays (what they'd pay).
 *
 * bookChanges returns a summary of all order-book movements that occurred in a
 * single ledger — useful for OHLCV charting and volume analytics.
 *
 * Both methods accept string ledger indices: "validated" (default), "current",
 * "closed", or a specific ledger number as a string.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        // --- 1. Build currency specifiers ---
        // XRP side: just the currency code
        val xrp = buildJsonObject { put("currency", "XRP") }

        // IOU side: currency code + issuer
        val usd = buildJsonObject {
            put("currency", "USD")
            put("issuer", "rIssuerAddressHere")
        }

        // --- 2. bookOffers: XRP/USD asks ---
        // takerGets = USD means the taker would receive USD.
        // takerPays = XRP means the taker would spend XRP.
        // This shows offers from USD sellers (asking XRP in return).
        val offersResult = client.bookOffers(
            takerGets  = usd,
            takerPays  = xrp,
            limit      = 10,
            ledgerIndex = "validated",
        ).getOrThrow()

        println("XRP/USD order book (${offersResult.offers.size} offers, ledger=${offersResult.ledgerIndex}):")
        for (offer in offersResult.offers) {
            println(
                "  account=${offer.account}" +
                "  gets=${offer.takerGets}" +
                "  pays=${offer.takerPays}" +
                "  quality=${offer.quality}" +
                "  ownerFunds=${offer.ownerFunds}",
            )
        }

        // --- 3. bookOffers: reverse side (USD/XRP bids) ---
        val bidsResult = client.bookOffers(
            takerGets = xrp,
            takerPays = usd,
            limit     = 5,
        ).getOrThrow()

        println("\nUSD/XRP bids (${bidsResult.offers.size} offers):")
        for (offer in bidsResult.offers) {
            println("  account=${offer.account}  sequence=${offer.sequence}  flags=${offer.flags}")
        }

        // --- 4. bookChanges: OHLCV data for the latest validated ledger ---
        // Returns one BookChange per currency pair that had activity in that ledger.
        when (val changesResult = client.bookChanges(ledgerIndex = "validated")) {
            is XrplResult.Success -> {
                val changes = changesResult.value
                println("\nOrder-book changes in ledger ${changes.ledgerIndex} (${changes.changes.size} pairs):")
                for (change in changes.changes) {
                    println(
                        "  ${change.currencyA} / ${change.currencyB}" +
                        "  open=${change.open}  close=${change.close}" +
                        "  high=${change.high}  low=${change.low}" +
                        "  volA=${change.volumeA}  volB=${change.volumeB}",
                    )
                }
            }
            is XrplResult.Failure -> println("bookChanges failed: ${changesResult.error}")
        }
    }
}
