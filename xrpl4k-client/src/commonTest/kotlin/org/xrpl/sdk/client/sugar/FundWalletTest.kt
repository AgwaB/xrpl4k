package org.xrpl.sdk.client.sugar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FundWalletTest : FunSpec({

    context("resolveFaucetHost") {

        test("resolves testnet URL to testnet faucet") {
            resolveFaucetHost("https://s.altnet.rippletest.net:51234") shouldBe
                "faucet.altnet.rippletest.net"
        }

        test("resolves devnet URL to devnet faucet") {
            resolveFaucetHost("https://s.devnet.rippletest.net:51234") shouldBe
                "faucet.devnet.rippletest.net"
        }

        test("resolves sidechain URL to devnet faucet") {
            resolveFaucetHost("https://sidechain.rippletest.net:51234") shouldBe
                "faucet.devnet.rippletest.net"
        }

        test("resolves URL containing testnet keyword to testnet faucet") {
            resolveFaucetHost("https://custom.testnet.example.com:51234") shouldBe
                "faucet.altnet.rippletest.net"
        }

        test("defaults to testnet faucet for unknown URLs") {
            resolveFaucetHost("https://custom.example.com:51234") shouldBe
                "faucet.altnet.rippletest.net"
        }

        test("throws on mainnet xrplcluster.com URL") {
            shouldThrow<IllegalStateException> {
                resolveFaucetHost("https://xrplcluster.com")
            }.message shouldBe "fundWallet is not supported on Mainnet"
        }

        test("throws on mainnet s1.ripple.com URL") {
            shouldThrow<IllegalStateException> {
                resolveFaucetHost("https://s1.ripple.com")
            }.message shouldBe "fundWallet is not supported on Mainnet"
        }

        test("throws on mainnet s2.ripple.com URL") {
            shouldThrow<IllegalStateException> {
                resolveFaucetHost("https://s2.ripple.com")
            }.message shouldBe "fundWallet is not supported on Mainnet"
        }

        test("is case-insensitive") {
            resolveFaucetHost("https://S.DEVNET.rippletest.net:51234") shouldBe
                "faucet.devnet.rippletest.net"
        }
    }
})
