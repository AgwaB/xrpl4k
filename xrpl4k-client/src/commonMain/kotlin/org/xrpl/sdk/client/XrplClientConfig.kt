package org.xrpl.sdk.client

import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.xrpl.sdk.core.Network
import org.xrpl.sdk.core.XrplDsl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration DSL for [XrplClient].
 *
 * ```kotlin
 * XrplClient {
 *     network = Network.Testnet
 *     timeout = 60.seconds
 *     retry {
 *         maxAttempts = 5
 *         initialDelay = 2.seconds
 *     }
 * }
 * ```
 */
@XrplDsl
public class XrplClientConfig {
    /** The XRPL network to connect to. Defaults to [Network.Mainnet]. */
    public var network: Network = Network.Mainnet

    /** Request timeout for RPC calls. Defaults to 30 seconds. */
    public var timeout: Duration = 30.seconds

    /** Fee cushion multiplier applied to the base fee. Defaults to 1.2. */
    public var feeCushion: Double = 1.2

    /** Maximum fee in XRP. Transactions with fees exceeding this are rejected. Defaults to 2.0. */
    public var maxFeeXrp: Double = 2.0

    /** Optional explicit HTTP client engine. `null` uses the platform default. */
    public var engine: HttpClientEngine? = null

    /**
     * Coroutine dispatcher for I/O operations.
     * Defaults to [Dispatchers.Default] (KMP-safe; `Dispatchers.IO` is JVM-only).
     * In tests, inject `UnconfinedTestDispatcher(testScheduler)`.
     */
    public var ioDispatcher: CoroutineDispatcher = Dispatchers.Default

    internal var retryConfig: RetryConfig = RetryConfig()
    internal var webSocketConfig: WebSocketConfig = WebSocketConfig()

    /** Configures retry behavior for transient failures. */
    public fun retry(block: RetryConfig.() -> Unit) {
        retryConfig.apply(block)
    }

    /** Configures WebSocket behavior (heartbeat, future reconnect). */
    public fun webSocket(block: WebSocketConfig.() -> Unit) {
        webSocketConfig.apply(block)
    }

    internal fun freeze(): FrozenClientConfig {
        require(timeout.isPositive()) { "timeout must be positive, got: $timeout" }
        require(feeCushion > 0.0) { "feeCushion must be positive, got: $feeCushion" }
        require(maxFeeXrp > 0.0) { "maxFeeXrp must be positive, got: $maxFeeXrp" }

        return FrozenClientConfig(
            network = network,
            timeout = timeout,
            feeCushion = feeCushion,
            maxFeeXrp = maxFeeXrp,
            engine = engine,
            ioDispatcher = ioDispatcher,
            retryConfig = retryConfig,
            webSocketConfig = webSocketConfig,
        )
    }
}

/** Retry configuration for transient failures. */
@XrplDsl
public class RetryConfig {
    /** Maximum number of retry attempts. Defaults to 3. */
    public var maxAttempts: Int = 3

    /** Initial delay before the first retry. Defaults to 1 second. */
    public var initialDelay: Duration = 1.seconds

    /** Maximum delay between retries. Defaults to 30 seconds. */
    public var maxDelay: Duration = 30.seconds
}

/** WebSocket-specific configuration. */
@XrplDsl
public class WebSocketConfig {
    /** Interval between WebSocket heartbeat pings. Defaults to 30 seconds. */
    public var heartbeatInterval: Duration = 30.seconds

    /** Whether to automatically reconnect when the connection drops unexpectedly. Defaults to true. */
    public var autoReconnect: Boolean = true

    /** Maximum number of reconnect attempts. Defaults to [Int.MAX_VALUE] (unlimited). */
    public var maxReconnectAttempts: Int = Int.MAX_VALUE

    /** Initial delay before the first reconnect attempt. Defaults to 100ms. */
    public var initialReconnectDelay: Duration = 100.milliseconds

    /** Maximum delay between reconnect attempts. Defaults to 60 seconds. */
    public var maxReconnectDelay: Duration = 60.seconds
}

/**
 * Immutable snapshot of client configuration.
 * Created at construction time; mutations to the original config have no effect.
 */
internal class FrozenClientConfig(
    val network: Network,
    val timeout: Duration,
    val feeCushion: Double,
    val maxFeeXrp: Double,
    val engine: HttpClientEngine?,
    val ioDispatcher: CoroutineDispatcher,
    val retryConfig: RetryConfig,
    val webSocketConfig: WebSocketConfig,
)
