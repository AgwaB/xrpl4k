import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.core.Network
import kotlin.time.Duration.Companion.seconds

/**
 * Demonstrates the various ways to configure and construct an XrplClient.
 *
 * XrplClient supports both HTTP (JSON-RPC) and WebSocket transports.
 * The WebSocket transport is initialized lazily on the first subscription call.
 * Always close the client (or use `.use {}`) to release resources.
 */
suspend fun main() {

    // --- 1. Minimal setup: connect to Mainnet with all defaults ---
    val mainnetClient = XrplClient()
    mainnetClient.close()

    // --- 2. Connect to Testnet ---
    val testnetClient = XrplClient {
        network = Network.Testnet
    }
    testnetClient.close()

    // --- 3. Connect to Devnet with a custom timeout ---
    val devnetClient = XrplClient {
        network  = Network.Devnet
        timeout  = 60.seconds
    }
    devnetClient.close()

    // --- 4. Custom network (private node or sidechain) ---
    val customClient = XrplClient {
        network = Network.Custom(
            rpcUrl    = "https://my-private-node.example.com:51234",
            wsUrl     = "wss://my-private-node.example.com:51233",
            networkId = 1234u,   // prevents transaction replay across networks
        )
        timeout = 45.seconds
    }
    customClient.close()

    // --- 5. Tune fee handling ---
    val feeClient = XrplClient {
        network      = Network.Testnet
        feeCushion   = 1.5    // multiply the base fee by 1.5
        maxFeeXrp    = 1.0    // reject if computed fee exceeds 1 XRP
    }
    feeClient.close()

    // --- 6. Configure retry behavior for transient failures ---
    val retryClient = XrplClient {
        network = Network.Testnet
        retry {
            maxAttempts  = 5
            initialDelay = 2.seconds
            maxDelay     = 30.seconds
        }
    }
    retryClient.close()

    // --- 7. Configure WebSocket heartbeat ---
    val wsClient = XrplClient {
        network = Network.Testnet
        webSocket {
            heartbeatInterval = 20.seconds
        }
    }
    wsClient.close()

    // --- 8. Use .use {} for automatic cleanup ---
    XrplClient { network = Network.Testnet }.use { client ->
        // client is closed automatically when the block exits
        println("Connected to ${client.config.network}")
    }
}
