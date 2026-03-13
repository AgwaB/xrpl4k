import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.take
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.rpc.accountTransactions
import org.xrpl.sdk.client.rpc.allAccountLines
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.type.Address

/**
 * Demonstrates automatic marker-based pagination using Flow.
 *
 * The XRPL uses a cursor/marker pattern: each paginated RPC response optionally
 * includes a `marker` field. Pass that marker in the next request to fetch the
 * following page. The Flow-based helpers in PaginatedMethods.kt hide this loop
 * and emit individual items across all pages.
 *
 * Two paginated helpers are available out of the box:
 *   - allAccountLines():      iterates all trust lines for an account
 *   - accountTransactions():  iterates all transactions for an account
 *
 * Both support cooperative cancellation: use take(N) to stop after N items
 * without fetching further pages. Errors on any page propagate through the Flow
 * and can be handled with catch {}.
 *
 * Under the hood, cursorFlow() drives the pagination loop:
 *   1. Call fetch(marker = null) to get the first page.
 *   2. Emit all items from that page.
 *   3. If the response includes a non-null marker, call fetch(marker) again.
 *   4. Repeat until marker is null (last page reached) or the collector cancels.
 */
suspend fun main() {
    XrplClient { network = Network.Testnet }.use { client ->

        val account = Address("rSomeAccountAddressHere")

        // --- 1. Iterate all trust lines (auto-paginating) ---
        // allAccountLines() fetches pages of size `pageSize` until exhausted.
        println("Trust lines:")
        client.allAccountLines(account, pageSize = 50)
            .take(100)                     // cap at 100 lines to avoid runaway fetching
            .catch { e -> println("  Error: $e") }
            .collect { line ->
                println("  ${line.currency} / ${line.account}  balance=${line.balance}  limit=${line.limit}")
            }

        // --- 2. Iterate all transactions (chronological order) ---
        // forward = true returns oldest-first; omit or set false for newest-first.
        println("\nTransaction history (forward, first 20):")
        client.accountTransactions(account, forward = true, pageSize = 10)
            .take(20)
            .catch { e -> println("  Error: $e") }
            .collect { entry ->
                println("  validated=${entry.validated}  tx=${entry.tx}")
            }

        // --- 3. Iterate transactions within a ledger range ---
        // Use ledgerIndexMin / ledgerIndexMax to scope the search.
        // -1 means "no bound" on that side.
        println("\nTransactions in ledger range 1000000–2000000:")
        client.accountTransactions(
            account       = account,
            ledgerIndexMin = 1_000_000L,
            ledgerIndexMax = 2_000_000L,
            pageSize       = 20,
        )
            .take(50)
            .catch { e -> println("  Error fetching page: $e") }
            .collect { entry ->
                println("  validated=${entry.validated}")
            }

        println("Done.")
    }
}
