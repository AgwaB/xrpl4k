@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.rpc.accountChannels
import org.xrpl.sdk.client.sugar.submitAndWait
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.paymentChannelCreate
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.XrpDrops
import org.xrpl.sdk.crypto.platformCryptoProvider
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for PaymentChannel operations.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class PaymentChannelIntegrationTest : IntegrationTestBase({

    val provider = platformCryptoProvider()

    test("paymentChannelCreate creates a channel visible in accountChannels") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                val source = fundNewWallet(c)
                val dest = fundNewWallet(c)

                source.use { s ->
                    dest.use { d ->
                        val channelAmount = XrpDrops(100_000_000L) // 100 XRP

                        val unsigned =
                            paymentChannelCreate(s.address) {
                                destination = d.address
                                amount = XrpAmount(channelAmount)
                                settleDelay = 86400u // 1 day
                                publicKey = s.publicKey.value
                            }

                        val advancer =
                            launch {
                                repeat(30) {
                                    delay(2000)
                                    ledgerAccept(c)
                                }
                            }

                        val result = c.submitAndWait(unsigned, s)
                        advancer.cancelAndJoin()

                        result.shouldBeInstanceOf<XrplResult.Success<*>>()
                        val validated = result.getOrThrow()
                        (validated.engineResult?.startsWith("tes")) shouldBe true

                        // Verify channel appears in accountChannels
                        val channelsResult = c.accountChannels(s.address).getOrThrow()
                        channelsResult.channels shouldHaveSize 1
                        val channel = channelsResult.channels.first()
                        channel.account shouldBe s.address
                        channel.destinationAccount shouldBe d.address
                        channel.amount shouldBe channelAmount
                        channel.settleDelay shouldBe 86400u
                    }
                }
            }
        }
    }

    test("accountChannels with destination filter returns only matching channels") {
        runTest(timeout = 2.minutes) {
            val client = createClient()
            client.use { c ->
                val source = fundNewWallet(c)
                val dest1 = fundNewWallet(c)
                val dest2 = fundNewWallet(c)

                source.use { s ->
                    dest1.use { d1 ->
                        dest2.use { d2 ->
                            // Create two channels from source to different destinations
                            val advancer =
                                launch {
                                    repeat(60) {
                                        delay(2000)
                                        ledgerAccept(c)
                                    }
                                }

                            c.submitAndWait(
                                paymentChannelCreate(s.address) {
                                    destination = d1.address
                                    amount = XrpAmount(XrpDrops(50_000_000L))
                                    settleDelay = 3600u
                                    publicKey = s.publicKey.value
                                },
                                s,
                            )

                            c.submitAndWait(
                                paymentChannelCreate(s.address) {
                                    destination = d2.address
                                    amount = XrpAmount(XrpDrops(50_000_000L))
                                    settleDelay = 3600u
                                    publicKey = s.publicKey.value
                                },
                                s,
                            )

                            advancer.cancelAndJoin()

                            // All channels
                            val allChannels = c.accountChannels(s.address).getOrThrow()
                            allChannels.channels shouldHaveSize 2

                            // Filtered to d1 only
                            val filteredChannels =
                                c.accountChannels(
                                    s.address,
                                    destinationAccount = d1.address,
                                ).getOrThrow()
                            filteredChannels.channels shouldHaveSize 1
                            filteredChannels.channels.first().destinationAccount shouldBe d1.address
                        }
                    }
                }
            }
        }
    }
})
