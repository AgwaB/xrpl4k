import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.sugar.getXrpBalance
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

/**
 * The simplest possible XRPL transaction: send XRP from one account to another.
 *
 * This sample shows the minimal code needed to:
 *   1. Connect to testnet
 *   2. Check balances
 *   3. Send XRP
 *   4. Confirm the transaction was validated
 *
 * XRP amounts can be expressed two ways:
 *   - `10.xrp`       — integer XRP (auto-converts to 10,000,000 drops)
 *   - `500_000L.drops` — explicit drops (1 XRP = 1,000,000 drops)
 *
 * Prerequisites:
 *   - A funded testnet account (get one at https://xrpl.org/xrp-testnet-faucet.html)
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val sender   = Wallet.fromSeed("sSenderSeedHere")
        val receiver = Address("rReceiverAddressHere")

        // --- Check balances before ---
        val senderBalance  = client.getXrpBalance(sender.address).getOrThrow()
        println("Sender balance:   ${senderBalance.toXrp()} XRP")

        // --- Build and send ---
        val tx = payment {
            account          = sender.address
            this.destination = receiver
            amount           = 10.xrp
        }

        val result = client.submitAndWait(tx, sender).getOrThrow()
        println("\nPayment sent!")
        println("  Hash:          ${result.hash}")
        println("  Ledger:        ${result.ledgerIndex}")
        println("  Engine result: ${result.engineResult}")

        // --- Check balances after ---
        val newBalance = client.getXrpBalance(sender.address).getOrThrow()
        println("\nSender balance:   ${newBalance.toXrp()} XRP")
        println("  (spent 10 XRP + fee)")
    }
}
