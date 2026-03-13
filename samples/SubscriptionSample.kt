import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.subscription.subscribeToLedger
import org.xrpl.sdk.client.subscription.subscribeToTransactions
import org.xrpl.sdk.client.subscription.subscribeToAccount
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.type.Address

/**
 * Demonstrates WebSocket subscriptions using Kotlin Flow.
 *
 * The WebSocket transport is initialized lazily on the first subscription call.
 * All subscription Flows are cold — they start subscribing on `collect`.
 *
 * Important: Flows do not auto-reconnect after a dropped connection. Wrap
 * with Flow.retry {} to re-subscribe on failure.
 *
 * Cancel a subscription by cancelling the collecting coroutine, or by using
 * operators like take(N) which cancel the Flow after N elements.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        // --- 1. Ledger close events ---
        // Emits one LedgerEvent each time a ledger closes (~every 4 seconds).
        println("Waiting for 3 ledger closes...")
        client.subscribeToLedger()
            .take(3)
            .catch { e -> println("Ledger stream error: $e") }
            .collect { event ->
                println("Ledger closed: index=${event.ledgerIndex} hash=${event.ledgerHash} txns=${event.txnCount}")
            }

        // --- 2. All network transactions ---
        // Emits one TransactionEvent for every validated transaction on the network.
        println("\nListening for 5 network transactions...")
        client.subscribeToTransactions()
            .take(5)
            .catch { e -> println("Transaction stream error: $e") }
            .collect { event ->
                println("Transaction: hash=${event.hash} result=${event.engineResult} validated=${event.validated}")
            }

        // --- 3. Account-specific events ---
        // Emits AccountEvent whenever a transaction affects the watched address.
        val watchedAddress = Address("rSomeAccountAddressHere")
        println("\nWatching account $watchedAddress for 2 events...")
        client.subscribeToAccount(watchedAddress)
            .take(2)
            .retry(3)   // re-subscribe up to 3 times on connection failure
            .catch { e -> println("Account stream error: $e") }
            .collect { event ->
                println("Account event: hash=${event.hash} result=${event.engineResult}")
            }

        println("Done.")
    }
}
