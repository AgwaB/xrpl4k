@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.rpc.accountInfo
import org.xrpl.sdk.client.rpc.accountObjects
import org.xrpl.sdk.client.sugar.getBalances
import org.xrpl.sdk.client.sugar.getXrpBalance
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Integration tests for account-level RPC methods.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class AccountMethodsIntegrationTest : IntegrationTestBase({

    val provider = platformCryptoProvider()

    test("accountInfo balance matches funding amount after fund") {
        runTest {
            val client = createClient()
            client.use { c ->
                val wallet = fundNewWallet(c)
                wallet.use { w ->
                    val info = c.accountInfo(w.address).getOrThrow()
                    info.balance shouldBe FUNDING_AMOUNT
                }
            }
        }
    }

    test("accountInfo on unknown address returns Failure") {
        runTest {
            val client = createClient()
            client.use { c ->
                val generated = Wallet.generate(provider = provider)
                generated.wallet.use { w ->
                    val result = c.accountInfo(w.address)
                    result.shouldBeInstanceOf<XrplResult.Failure>()
                }
            }
        }
    }

    test("getXrpBalance returns correct balance for funded wallet") {
        runTest {
            val client = createClient()
            client.use { c ->
                val wallet = fundNewWallet(c)
                wallet.use { w ->
                    val result = c.getXrpBalance(w.address)
                    result.shouldBeInstanceOf<XrplResult.Success<*>>()
                    result.getOrThrow() shouldBe FUNDING_AMOUNT
                }
            }
        }
    }

    test("getBalances returns at least XRP balance for funded wallet") {
        runTest {
            val client = createClient()
            client.use { c ->
                val wallet = fundNewWallet(c)
                wallet.use { w ->
                    val result = c.getBalances(w.address)
                    result.shouldBeInstanceOf<XrplResult.Success<*>>()
                    val balances = result.getOrThrow()
                    balances.shouldNotBeEmpty()
                    val xrpBalance = balances.first { it.currency == "XRP" }
                    (xrpBalance.value.toDouble() > 0.0) shouldBe true
                }
            }
        }
    }

    test("accountObjects on funded wallet returns success") {
        runTest {
            val client = createClient()
            client.use { c ->
                val wallet = fundNewWallet(c)
                wallet.use { w ->
                    val result = c.accountObjects(w.address)
                    result.shouldBeInstanceOf<XrplResult.Success<*>>()
                    val objects = result.getOrThrow()
                    objects.account shouldBe w.address
                }
            }
        }
    }
})
