package org.xrpl.sdk.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.xrpl.sdk.client.internal.defaultHttpClientEngine
import org.xrpl.sdk.client.transport.HttpTransport
import org.xrpl.sdk.client.transport.WebSocketTransport
import org.xrpl.sdk.client.transport.XrplTransport

/**
 * Creates an [XrplClient] using the DSL configuration block.
 *
 * ```kotlin
 * val client = XrplClient {
 *     network = Network.Testnet
 *     timeout = 60.seconds
 * }
 * client.use {
 *     val info = it.accountInfo(address)
 * }
 * ```
 *
 * @param block configuration DSL block.
 * @return a configured [XrplClient] instance.
 */
public fun XrplClient(block: XrplClientConfig.() -> Unit = {}): XrplClient {
    val config = XrplClientConfig().apply(block).freeze()
    return XrplClient(config)
}

/**
 * The main entry point for interacting with the XRPL network.
 *
 * Supports both HTTP (JSON-RPC) and WebSocket transports. The WebSocket transport
 * is lazily initialized on first subscription call ("HTTP-only mode" means it has
 * not yet been created).
 *
 * Implements [AutoCloseable] — use with `.use { }` for automatic resource cleanup.
 * Closing cancels the internal [CoroutineScope] and shuts down all transports.
 */
public class XrplClient internal constructor(
    internal val config: FrozenClientConfig,
) : AutoCloseable {
    internal val httpTransport: XrplTransport =
        HttpTransport(
            engine = config.engine ?: defaultHttpClientEngine(),
            url = config.network.rpcUrl,
            timeout = config.timeout,
        )

    internal val scope: CoroutineScope =
        CoroutineScope(
            SupervisorJob() + config.ioDispatcher,
        )

    private val lazyWsTransport: Lazy<WebSocketTransport> =
        lazy {
            WebSocketTransport(
                url = config.network.wsUrl,
                engine = config.engine ?: defaultHttpClientEngine(),
                scope = scope,
                heartbeatInterval = config.webSocketConfig.heartbeatInterval,
            )
        }

    internal val webSocketTransport: WebSocketTransport by lazyWsTransport

    /**
     * Closes this client, cancelling the internal coroutine scope and shutting down all transports.
     *
     * The WebSocket transport is only closed if it was previously initialized.
     * After calling this method the client must not be used again.
     */
    override fun close() {
        scope.cancel("XrplClient closed")
        httpTransport.close()
        if (lazyWsTransport.isInitialized()) {
            webSocketTransport.close()
        }
    }
}
