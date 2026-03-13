import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountOffers
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.offerCancel
import org.xrpl.sdk.core.model.transaction.offerCreate
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates placing and cancelling orders on the XRPL decentralised exchange (DEX).
 *
 * The XRPL has a built-in order book. Anyone can place offers to trade XRP for
 * issued currencies (or IOU-to-IOU). Offers are matched automatically by the
 * ledger's matching engine when they cross existing offers.
 *
 * Key concepts:
 *   - OfferCreate: places a new order. takerGets = what the taker receives,
 *     takerPays = what the taker pays. From the maker's perspective, you are
 *     selling takerGets and buying takerPays.
 *   - OfferCancel: removes an open order by its sequence number.
 *   - Offers can be partially filled. If your offer crosses existing orders,
 *     it executes immediately (like a market order). Any remainder stays on
 *     the book as a limit order.
 *
 * Flags (optional):
 *   - tfPassive (0x00010000): don't match existing offers; only sit on the book
 *   - tfImmediateOrCancel (0x00020000): fill what you can immediately, cancel the rest
 *   - tfFillOrKill (0x00040000): either fill the entire amount or cancel everything
 *   - tfSell (0x00080000): treat takerPays as a minimum; sell all of takerGets
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val trader = Wallet.fromSeed("sTraderSeedHere")

        val usd = CurrencyCode("USD")
        val issuer = Address("rIssuerAddressHere")

        // --- 1. Place a limit order: sell 100 XRP for 50 USD ---
        // This means: I'm offering 100 XRP (takerGets) and want 50 USD (takerPays).
        // Exchange rate: 1 XRP = 0.5 USD
        val sellXrp = offerCreate {
            account  = trader.address
            takerGets = 100.xrp
            takerPays = IssuedAmount(currency = usd, issuer = issuer, value = "50")
        }

        val sellResult = client.submitAndWait(sellXrp, trader).getOrThrow()
        println("Sell order placed: ${sellResult.engineResult}")
        println("  Hash: ${sellResult.hash}")

        // --- 2. Place a buy order: buy 50 XRP for 30 USD ---
        // I'm offering 30 USD (takerGets) and want 50 XRP (takerPays).
        // Exchange rate: 1 XRP = 0.6 USD
        val buyXrp = offerCreate {
            account   = trader.address
            takerGets = IssuedAmount(currency = usd, issuer = issuer, value = "30")
            takerPays = 50.xrp
        }

        val buyResult = client.submitAndWait(buyXrp, trader).getOrThrow()
        println("\nBuy order placed: ${buyResult.engineResult}")

        // --- 3. View open offers ---
        when (val offersResult = client.accountOffers(trader.address)) {
            is XrplResult.Success -> {
                val offers = offersResult.value
                println("\nOpen offers (${offers.offers.size}):")
                for (offer in offers.offers) {
                    println("  seq=${offer.sequence}  gets=${offer.takerGets}  pays=${offer.takerPays}")
                }
            }
            is XrplResult.Failure -> println("accountOffers failed: ${offersResult.error}")
        }

        // --- 4. Cancel an order ---
        // Use the sequence number from the original OfferCreate transaction.
        // You can find it in accountOffers results or from the autofilled tx.
        val cancelTx = offerCancel {
            account       = trader.address
            offerSequence = 12345u   // replace with actual sequence
        }

        val cancelResult = client.submitAndWait(cancelTx, trader).getOrThrow()
        println("\nOrder cancelled: ${cancelResult.engineResult}")
    }
}
