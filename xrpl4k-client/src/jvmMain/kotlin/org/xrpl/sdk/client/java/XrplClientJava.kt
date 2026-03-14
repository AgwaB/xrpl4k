package org.xrpl.sdk.client.java

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.future
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.XrplClientConfig
import org.xrpl.sdk.client.model.AccountInfo
import org.xrpl.sdk.client.model.AccountLinesResult
import org.xrpl.sdk.client.model.FeeResult
import org.xrpl.sdk.client.model.ServerInfo
import org.xrpl.sdk.client.model.SubmitResult
import org.xrpl.sdk.client.model.TransactionResult
import org.xrpl.sdk.client.model.ValidatedTransaction
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.accountLines
import org.xrpl.sdk.client.rpc.fee
import org.xrpl.sdk.client.rpc.serverInfo
import org.xrpl.sdk.client.rpc.submit
import org.xrpl.sdk.client.rpc.tx
import org.xrpl.sdk.client.sugar.getXrpBalance
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.Wallet
import java.util.concurrent.CompletableFuture

/**
 * Java-friendly wrapper around [XrplClient].
 *
 * Kotlin suspend functions and extension functions are awkward to call from Java.
 * This class bridges the gap by exposing each operation as a [CompletableFuture],
 * backed by an internal [CoroutineScope] running on [Dispatchers.Default].
 *
 * **Usage from Java:**
 * ```java
 * try (XrplClientJava client = XrplClientJava.create(config -> {
 *     config.setNetwork(Network.Testnet);
 * })) {
 *     AccountInfo info = client.accountInfo(address).get().getOrThrow();
 * }
 * ```
 *
 * Implements [AutoCloseable]: call [close] or use try-with-resources to release
 * the underlying [XrplClient] and its coroutine scope.
 *
 * @param client The underlying [XrplClient] to delegate calls to.
 */
public class XrplClientJava(
    private val client: XrplClient,
) : AutoCloseable {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // -------------------------------------------------------------------------
    // Account methods
    // -------------------------------------------------------------------------

    /**
     * Retrieves account information for the given address.
     *
     * @param address The XRPL account address.
     * @return A [CompletableFuture] resolving to [XrplResult] with [AccountInfo].
     */
    public fun accountInfo(address: Address): CompletableFuture<XrplResult<AccountInfo>> =
        scope.future { client.accountInfo(address) }

    /**
     * Retrieves the trust lines (IOUs) for the given account.
     *
     * @param address The XRPL account address.
     * @return A [CompletableFuture] resolving to [XrplResult] with [AccountLinesResult].
     */
    public fun accountLines(address: Address): CompletableFuture<XrplResult<AccountLinesResult>> =
        scope.future { client.accountLines(address) }

    /**
     * Returns the XRP balance for the given account in drops.
     *
     * @param address The XRPL account address.
     * @return A [CompletableFuture] resolving to [XrplResult] with the balance in [XrpDrops].
     */
    public fun getXrpBalance(address: Address): CompletableFuture<XrplResult<XrpDrops>> =
        scope.future { client.getXrpBalance(address) }

    // -------------------------------------------------------------------------
    // Transaction methods
    // -------------------------------------------------------------------------

    /**
     * Submits a signed transaction to the XRPL network.
     *
     * @param tx The signed transaction to submit.
     * @return A [CompletableFuture] resolving to [XrplResult] with [SubmitResult].
     */
    public fun submit(tx: XrplTransaction.Signed): CompletableFuture<XrplResult<SubmitResult>> =
        scope.future { client.submit(tx) }

    /**
     * Looks up a transaction by its hash.
     *
     * @param hash The 64-character hex transaction hash.
     * @return A [CompletableFuture] resolving to [XrplResult] with [TransactionResult].
     */
    public fun tx(hash: TxHash): CompletableFuture<XrplResult<TransactionResult>> = scope.future { client.tx(hash) }

    // -------------------------------------------------------------------------
    // Server methods
    // -------------------------------------------------------------------------

    /**
     * Returns general information about the current state of the rippled server.
     *
     * @return A [CompletableFuture] resolving to [XrplResult] with [ServerInfo].
     */
    public fun serverInfo(): CompletableFuture<XrplResult<ServerInfo>> = scope.future { client.serverInfo() }

    /**
     * Returns the current transaction fee schedule and queue state.
     *
     * @return A [CompletableFuture] resolving to [XrplResult] with [FeeResult].
     */
    public fun fee(): CompletableFuture<XrplResult<FeeResult>> = scope.future { client.fee() }

    // -------------------------------------------------------------------------
    // Sugar methods
    // -------------------------------------------------------------------------

    /**
     * Full transaction lifecycle: autofill → sign → submit → poll for validation.
     *
     * Polls the network approximately every 4 seconds until the transaction is
     * validated or its [XrplTransaction.Unsigned] `lastLedgerSequence` expires.
     *
     * @param tx The unsigned transaction. Fields such as `fee` and `sequence` will
     *           be filled automatically from the current network state.
     * @param wallet The wallet used to sign the transaction.
     * @return A [CompletableFuture] resolving to [XrplResult] with [ValidatedTransaction].
     */
    public fun submitAndWait(
        tx: XrplTransaction.Unsigned,
        wallet: Wallet,
    ): CompletableFuture<XrplResult<ValidatedTransaction>> = scope.future { client.submitAndWait(tx, wallet) }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Closes the underlying [XrplClient] and cancels the internal coroutine scope.
     *
     * After calling this method the instance must not be used again.
     */
    override fun close() {
        scope.cancel("XrplClientJava closed")
        client.close()
    }

    public companion object {
        /**
         * Creates an [XrplClientJava] using the DSL configuration block.
         *
         * ```java
         * XrplClientJava client = XrplClientJava.create(config -> {
         *     config.setNetwork(Network.Testnet);
         *     config.setTimeout(Duration.parse("PT60S"));
         * });
         * ```
         *
         * @param block A Kotlin lambda configuring [XrplClientConfig]. From Java,
         *              use `config -> { config.setFoo(...); return Unit.INSTANCE; }`.
         * @return A configured [XrplClientJava] instance.
         */
        @JvmStatic
        public fun create(block: XrplClientConfig.() -> Unit): XrplClientJava = XrplClientJava(XrplClient(block))
    }
}
