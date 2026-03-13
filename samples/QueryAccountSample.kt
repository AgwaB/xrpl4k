import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.accountLines
import org.xrpl.sdk.client.rpc.accountNfts
import org.xrpl.sdk.client.rpc.accountTx
import org.xrpl.sdk.client.sugar.getXrpBalance
import org.xrpl.sdk.client.sugar.getBalances
import org.xrpl.sdk.client.sugar.getLedgerIndex
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address

/**
 * Demonstrates querying account information and balances.
 *
 * All RPC methods return XrplResult<T>. Use getOrThrow() to unwrap and rethrow
 * on failure, or pattern-match on XrplResult.Success / XrplResult.Failure for
 * explicit error handling.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val address = Address("rSomeAccountAddressHere")

        // --- Current ledger index ---
        val ledgerIndex = client.getLedgerIndex().getOrThrow()
        println("Current ledger: $ledgerIndex")

        // --- XRP balance (convenience helper) ---
        val xrpBalance = client.getXrpBalance(address).getOrThrow()
        println("XRP balance: ${xrpBalance.toXrp()} XRP (${xrpBalance.value} drops)")

        // --- All balances: XRP + issued currencies ---
        val balances = client.getBalances(address).getOrThrow()
        for (balance in balances) {
            val issuerStr = balance.issuer?.let { " (issuer: $it)" } ?: ""
            println("  ${balance.currency}: ${balance.value}$issuerStr")
        }

        // --- Full account info (sequence, owner count, flags, etc.) ---
        val info = client.accountInfo(address).getOrThrow()
        println("Sequence:    ${info.sequence}")
        println("Owner count: ${info.ownerCount}")
        println("Flags:       ${info.flags}")

        // --- Trust lines (IOUs) ---
        val linesResult = client.accountLines(address)
        when (linesResult) {
            is XrplResult.Success -> {
                for (line in linesResult.value.lines) {
                    println("Trust line: ${line.currency} / ${line.account} — balance: ${line.balance}")
                }
            }
            is XrplResult.Failure -> println("accountLines failed: ${linesResult.error}")
        }

        // --- NFTs owned by the account ---
        val nfts = client.accountNfts(address).getOrThrow()
        println("NFT count: ${nfts.accountNfts.size}")
        for (nft in nfts.accountNfts) {
            println("  NFT: ${nft.nftokenId} taxon=${nft.nftokenTaxon}")
        }

        // --- Recent transactions ---
        val txHistory = client.accountTx(address, limit = 10).getOrThrow()
        for (entry in txHistory.transactions) {
            println("  tx validated=${entry.validated}")
        }
    }
}
