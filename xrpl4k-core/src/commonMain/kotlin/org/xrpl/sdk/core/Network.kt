package org.xrpl.sdk.core

/**
 * Represents an XRPL network endpoint configuration.
 *
 * Use the predefined singletons [Mainnet], [Testnet], and [Devnet] for standard networks,
 * or [Custom] for private and custom deployments.
 */
public sealed class Network {
    /** The HTTP/HTTPS RPC endpoint URL for this network. */
    public abstract val rpcUrl: String

    /** The WebSocket endpoint URL for this network. */
    public abstract val wsUrl: String

    /**
     * The numeric network ID used to distinguish private networks from public ones.
     * `null` for public networks (Mainnet, Testnet, Devnet).
     */
    public abstract val networkId: UInt?

    /**
     * The XRPL Mainnet — the primary production network.
     *
     * Uses the public cluster provided by the XRPL Foundation.
     */
    public data object Mainnet : Network() {
        override val rpcUrl: String = "https://xrplcluster.com"
        override val wsUrl: String = "wss://xrplcluster.com"
        override val networkId: UInt? = null
    }

    /**
     * The XRPL Testnet — a stable test network that mirrors Mainnet features.
     *
     * Funds can be obtained from the [Testnet Faucet](https://xrpl.org/xrp-testnet-faucet.html).
     */
    public data object Testnet : Network() {
        override val rpcUrl: String = "https://s.altnet.rippletest.net:51234"
        override val wsUrl: String = "wss://s.altnet.rippletest.net:51233"
        override val networkId: UInt? = null
    }

    /**
     * The XRPL Devnet — an unstable development network for cutting-edge feature testing.
     */
    public data object Devnet : Network() {
        override val rpcUrl: String = "https://s.devnet.rippletest.net:51234"
        override val wsUrl: String = "wss://s.devnet.rippletest.net:51233"
        override val networkId: UInt? = null
    }

    /**
     * A custom XRPL-compatible network, such as a private sidechain or local devnet.
     *
     * @param rpcUrl The HTTP or HTTPS RPC endpoint. Must start with `http://` or `https://`.
     * @param wsUrl The WebSocket endpoint. Must start with `ws://` or `wss://`.
     * @param networkId Optional numeric network ID. Use a non-null value to prevent transaction
     *   replay across networks.
     * @throws IllegalArgumentException if [rpcUrl] or [wsUrl] have an invalid scheme.
     */
    public class Custom(
        override val rpcUrl: String,
        override val wsUrl: String,
        override val networkId: UInt? = null,
    ) : Network() {
        init {
            require(rpcUrl.startsWith("http://") || rpcUrl.startsWith("https://")) {
                "RPC URL must start with http:// or https://. Got: $rpcUrl"
            }
            require(wsUrl.startsWith("ws://") || wsUrl.startsWith("wss://")) {
                "WebSocket URL must start with ws:// or wss://. Got: $wsUrl"
            }
        }

        override fun equals(other: Any?): Boolean =
            other is Custom &&
                rpcUrl == other.rpcUrl &&
                wsUrl == other.wsUrl &&
                networkId == other.networkId

        override fun hashCode(): Int {
            var result = rpcUrl.hashCode()
            result = 31 * result + wsUrl.hashCode()
            result = 31 * result + (networkId?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = "Network.Custom(rpcUrl=$rpcUrl, wsUrl=$wsUrl, networkId=$networkId)"
    }
}
