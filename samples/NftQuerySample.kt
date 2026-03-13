import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.nftBuyOffers
import org.xrpl.sdk.client.rpc.nftSellOffers
import org.xrpl.sdk.client.rpc.nftInfo
import org.xrpl.sdk.client.rpc.nftHistory
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow

/**
 * Demonstrates querying NFT data from the XRPL.
 *
 * nftBuyOffers and nftSellOffers return the open marketplace offers for an NFT.
 * nftInfo and nftHistory require a Clio server (not available on all nodes).
 *
 * All four methods accept an optional `limit` and `marker` for pagination.
 * Use `marker` from one response as the input for the next call to walk through
 * large result sets one page at a time.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        // Replace with a real NFTokenID (64-character hex string).
        val nftId = "000800006B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B"

        // --- 1. Buy offers ---
        // Returns all open NFTokenOffer ledger objects where someone is bidding to buy this NFT.
        val buyResult = client.nftBuyOffers(nftId, limit = 20).getOrThrow()
        println("Buy offers for NFT ${buyResult.nftId}:")
        for (offer in buyResult.offers) {
            println("  offer index=${offer.nftOfferIndex}  owner=${offer.owner}  amount=${offer.amount}  flags=${offer.flags}")
            offer.destination?.let { println("    restricted to: $it") }
            offer.expiration?.let  { println("    expires at ripple epoch: $it") }
        }
        println("  (${buyResult.offers.size} buy offers; nextMarker=${buyResult.marker})")

        // --- 2. Sell offers ---
        // Returns all open NFTokenOffer ledger objects where the owner is selling this NFT.
        val sellResult = client.nftSellOffers(nftId, limit = 20).getOrThrow()
        println("\nSell offers:")
        for (offer in sellResult.offers) {
            println("  offer index=${offer.nftOfferIndex}  owner=${offer.owner}  amount=${offer.amount}")
        }

        // --- 3. NFT info (Clio server required) ---
        // Returns the current on-ledger state of the NFT: owner, URI, taxon, serial, flags.
        when (val infoResult = client.nftInfo(nftId)) {
            is XrplResult.Success -> {
                val info = infoResult.value
                println("\nNFT info:")
                println("  owner:       ${info.owner}")
                println("  issuer:      ${info.issuer}")
                println("  taxon:       ${info.nftTaxon}")
                println("  serial:      ${info.nftSerial}")
                println("  transferFee: ${info.transferFee}")
                println("  uri:         ${info.uri}")
                println("  isBurned:    ${info.isBurned}")
                println("  flags:       ${info.flags}")
            }
            is XrplResult.Failure ->
                println("\nnftInfo not available (requires Clio): ${infoResult.error}")
        }

        // --- 4. NFT transaction history (Clio server required) ---
        // Walks the full lifecycle of an NFT: mint, offers, transfers, burn.
        when (val histResult = client.nftHistory(nftId, limit = 10)) {
            is XrplResult.Success -> {
                val history = histResult.value
                println("\nNFT history (${history.transactions.size} entries):")
                for (entry in history.transactions) {
                    println("  validated=${entry.validated}  tx=${entry.tx}")
                }
                history.marker?.let { println("  more pages available; marker=$it") }
            }
            is XrplResult.Failure ->
                println("\nnftHistory not available (requires Clio): ${histResult.error}")
        }
    }
}
