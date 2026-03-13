import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.nftSellOffers
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.client.signing.signTransaction
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.nfTokenMint
import org.xrpl.sdk.core.model.transaction.nfTokenBurn
import org.xrpl.sdk.core.model.transaction.nfTokenCreateOffer
import org.xrpl.sdk.core.model.transaction.nfTokenAcceptOffer
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates the full NFT lifecycle on the XRPL.
 *
 * NFTs follow the XLS-20 standard. A typical lifecycle looks like:
 *   1. Mint    — create the NFT (NFTokenMint)
 *   2. Offer   — list it for sale (NFTokenCreateOffer with sell flag)
 *   3. Accept  — buyer accepts the sell offer (NFTokenAcceptOffer)
 *   4. Burn    — destroy the NFT (NFTokenBurn), only the owner can do this
 *
 * Key NFTokenMint flags (set via the `flags` field):
 *   tfBurnable       = 0x00000001  — issuer can burn the token
 *   tfOnlyXRP        = 0x00000002  — token can only be traded for XRP
 *   tfTransferable   = 0x00000008  — token can be transferred (default: not transferable)
 *
 * NFTokenCreateOffer flags:
 *   tfSellNFToken    = 0x00000001  — this is a sell offer (vs. a buy offer)
 *
 * All NFT builder functions take an account Address as the first argument,
 * unlike the simple payment() builder which reads account from the block.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val minter  = Wallet.fromSeed("sYourMinterSeedHere")
        val buyer   = Wallet.fromSeed("sBuyerSeedHere")

        // --- 1. Mint an NFT ---
        // nfTokenTaxon groups related tokens; use 0 for a standalone collection.
        // transferFee is in units of 1/100,000 (e.g. 5000 = 5%).
        // uri is a hex-encoded pointer to off-chain metadata (IPFS, HTTPS, etc.).
        val mintTx = nfTokenMint(minter.address) {
            nfTokenTaxon = 1u
            transferFee  = 5000u          // 5% royalty on secondary sales
            flags        = 0x00000008u    // tfTransferable — needed for secondary sales
            uri          = "68747470733A2F2F6578616D706C652E636F6D2F6E66742F6D657461646174612E6A736F6E"
        }

        val mintResult = client.submitAndWait(mintTx, minter).getOrThrow()
        println("NFT minted!")
        println("  hash:   ${mintResult.hash}")
        println("  ledger: ${mintResult.ledgerIndex}")
        println("  result: ${mintResult.engineResult}")

        // The actual NFTokenID must be extracted from the transaction metadata.
        // Use TransactionUtilsSample.kt / getNFTokenID() for that.
        val nftId = "000800006B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B6B"

        // --- 2. Create a sell offer ---
        // flags = 0x00000001 (tfSellNFToken) marks this as a sell offer.
        // Omit destination to allow any account to accept.
        val sellOfferTx = nfTokenCreateOffer(minter.address) {
            nfTokenId   = nftId
            amount      = 10.xrp
            flags       = 0x00000001u    // tfSellNFToken
        }

        val sellResult = client.submitAndWait(sellOfferTx, minter).getOrThrow()
        println("\nSell offer created: ${sellResult.hash}")

        // Retrieve the offer index from the on-ledger sell offers list.
        val sellOffers = client.nftSellOffers(nftId).getOrThrow()
        val offerIndex = sellOffers.offers.firstOrNull()?.nftOfferIndex
            ?: error("No sell offer found on ledger")
        println("Offer index: $offerIndex")

        // --- 3. Accept the sell offer (buyer side) ---
        // The buyer accepts by referencing the sell offer's ledger object ID.
        val acceptTx = nfTokenAcceptOffer(buyer.address) {
            nfTokenSellOffer = offerIndex
        }

        val acceptResult = client.submitAndWait(acceptTx, buyer).getOrThrow()
        println("\nOffer accepted (NFT transferred): ${acceptResult.hash}")

        // --- 4. Burn the NFT ---
        // Only the current owner (or the issuer if tfBurnable was set) can burn.
        val burnTx = nfTokenBurn(buyer.address) {
            nfTokenId = nftId
        }

        when (val burnResult = client.submitAndWait(burnTx, buyer)) {
            is XrplResult.Success ->
                println("\nNFT burned: ${burnResult.value.hash}")
            is XrplResult.Failure ->
                println("\nBurn failed: ${burnResult.error}")
        }
    }
}
