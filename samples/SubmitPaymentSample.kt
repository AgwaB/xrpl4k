import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.sugar.autofill
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.client.signing.signTransaction
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.model.transaction.payment
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.crypto.Wallet

/**
 * Demonstrates the full payment lifecycle on the XRPL.
 *
 * A transaction progresses through three states:
 *   XrplTransaction.Unsigned  ->  XrplTransaction.Filled  ->  XrplTransaction.Signed
 *
 * The `submitAndWait` helper combines autofill + sign + submit + poll in one call.
 * The manual steps below show how to perform each stage individually.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        // Restore a wallet from a Base58-encoded seed.
        // Wallet.generate() creates a brand-new random wallet.
        val wallet = Wallet.fromSeed("sYourTestnetSeedHere")
        println("Sending from: ${wallet.address}")

        val destination = Address("rDestinationAddressHere")

        // ---------------------------------------------------------------
        // Option A: one-shot helper (recommended for most use cases)
        // ---------------------------------------------------------------
        val tx = payment {
            account          = wallet.address
            this.destination = destination
            amount           = 10.xrp          // 10 XRP; use Long.drops for drop precision
            destinationTag   = 12345u           // optional routing tag
        }

        when (val result = client.submitAndWait(tx, wallet)) {
            is XrplResult.Success -> {
                println("Payment validated!")
                println("  Hash:         ${result.value.hash}")
                println("  Ledger index: ${result.value.ledgerIndex}")
                println("  Engine result:${result.value.engineResult}")
            }
            is XrplResult.Failure -> {
                println("Payment failed: ${result.error}")
            }
        }

        // ---------------------------------------------------------------
        // Option B: manual steps (useful when you need the intermediate
        // objects, e.g. to inspect the computed fee before signing)
        // ---------------------------------------------------------------
        val unsignedTx = payment {
            account          = wallet.address
            this.destination = destination
            amount           = 5.xrp
        }

        // Step 1: autofill — fetches fee, sequence, and lastLedgerSequence
        // from the network in parallel; applies feeCushion and maxFeeXrp limits.
        val filled = client.autofill(unsignedTx).getOrThrow()
        println("Fee: ${filled.fee}, Sequence: ${filled.sequence}")

        // Step 2: sign — produces the canonical binary blob and tx hash
        val signed = wallet.signTransaction(filled)
        println("Tx hash: ${signed.hash}")

        // Step 3: submit — broadcasts the signed blob to the network
        val submitInfo = client.submit(signed).getOrThrow()
        println("Preliminary result: ${submitInfo.engineResult}")
        // Poll client.tx(signed.hash) until validated == true to confirm.
    }
}
