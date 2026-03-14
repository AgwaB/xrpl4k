package org.xrpl.sdk.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class NetworkTest : FunSpec({

    test("Mainnet has correct URLs and null networkId") {
        Network.Mainnet.rpcUrl shouldBe "https://xrplcluster.com"
        Network.Mainnet.wsUrl shouldBe "wss://xrplcluster.com"
        Network.Mainnet.networkId shouldBe null
    }

    test("Testnet has correct URLs and null networkId") {
        Network.Testnet.rpcUrl shouldBe "https://s.altnet.rippletest.net:51234"
        Network.Testnet.wsUrl shouldBe "wss://s.altnet.rippletest.net:51233"
        Network.Testnet.networkId shouldBe null
    }

    test("Devnet has correct URLs and null networkId") {
        Network.Devnet.rpcUrl shouldBe "https://s.devnet.rippletest.net:51234"
        Network.Devnet.wsUrl shouldBe "wss://s.devnet.rippletest.net:51233"
        Network.Devnet.networkId shouldBe null
    }

    test("Custom accepts http rpcUrl and ws wsUrl") {
        val network =
            Network.Custom(
                rpcUrl = "http://localhost:5005",
                wsUrl = "ws://localhost:6006",
            )
        network.rpcUrl shouldBe "http://localhost:5005"
        network.wsUrl shouldBe "ws://localhost:6006"
        network.networkId shouldBe null
    }

    test("Custom accepts https rpcUrl and wss wsUrl with networkId") {
        val network =
            Network.Custom(
                rpcUrl = "https://my-node.example.com",
                wsUrl = "wss://my-node.example.com",
                networkId = 1234u,
            )
        network.networkId shouldBe 1234u
    }

    test("Custom throws IllegalArgumentException for invalid rpcUrl scheme") {
        shouldThrow<IllegalArgumentException> {
            Network.Custom(
                rpcUrl = "ftp://bad-url.example.com",
                wsUrl = "wss://my-node.example.com",
            )
        }
    }

    test("Custom throws IllegalArgumentException for invalid wsUrl scheme") {
        shouldThrow<IllegalArgumentException> {
            Network.Custom(
                rpcUrl = "https://my-node.example.com",
                wsUrl = "http://my-node.example.com",
            )
        }
    }

    test("Custom equals and hashCode are consistent") {
        val a =
            Network.Custom(
                rpcUrl = "https://node.example.com",
                wsUrl = "wss://node.example.com",
                networkId = 42u,
            )
        val b =
            Network.Custom(
                rpcUrl = "https://node.example.com",
                wsUrl = "wss://node.example.com",
                networkId = 42u,
            )
        a shouldBe b
        a.hashCode() shouldBe b.hashCode()
    }

    test("Custom instances with different URLs are not equal") {
        val a =
            Network.Custom(
                rpcUrl = "https://node-a.example.com",
                wsUrl = "wss://node-a.example.com",
            )
        val b =
            Network.Custom(
                rpcUrl = "https://node-b.example.com",
                wsUrl = "wss://node-b.example.com",
            )
        a shouldNotBe b
    }

    test("Custom toString includes all fields") {
        val network =
            Network.Custom(
                rpcUrl = "https://node.example.com",
                wsUrl = "wss://node.example.com",
                networkId = 7u,
            )
        network.toString() shouldBe
            "Network.Custom(rpcUrl=https://node.example.com, wsUrl=wss://node.example.com, networkId=7)"
    }

    test("Exhaustive when over all Network subtypes compiles and returns correct label") {
        fun label(n: Network): String =
            when (n) {
                is Network.Mainnet -> "mainnet"
                is Network.Testnet -> "testnet"
                is Network.Devnet -> "devnet"
                is Network.Custom -> "custom"
            }
        label(Network.Mainnet) shouldBe "mainnet"
        label(Network.Testnet) shouldBe "testnet"
        label(Network.Devnet) shouldBe "devnet"
        label(Network.Custom(rpcUrl = "https://x.com", wsUrl = "wss://x.com")) shouldBe "custom"
    }
})
