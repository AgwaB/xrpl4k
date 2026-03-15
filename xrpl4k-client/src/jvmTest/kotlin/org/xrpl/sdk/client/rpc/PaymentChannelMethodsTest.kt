@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.ChannelVerifyResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.XrpDrops

class PaymentChannelMethodsTest : FunSpec({

    // ── channelVerify ───────────────────────────────────────────

    test("channelVerify returns signatureVerified=true for valid signature") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""signature_verified":true"""),
                )
            client.use { c ->
                val result =
                    c.channelVerify(
                        channelId = "5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3",
                        amount = XrpDrops(1_000_000),
                        publicKey = "aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3",
                        signature =
                            "30440220718D264EF05CAED7C781FF6DE298DCAC68D002562C9BF3A07C1E721B420C0DAB" +
                                "02203A5A4779EF4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B",
                    )
                result.shouldBeInstanceOf<XrplResult.Success<ChannelVerifyResult>>()
                val data = (result as XrplResult.Success<ChannelVerifyResult>).value
                data.signatureVerified shouldBe true
            }
        }
    }

    test("channelVerify returns signatureVerified=false for invalid signature") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""signature_verified":false"""),
                )
            client.use { c ->
                val result =
                    c.channelVerify(
                        channelId = "5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3",
                        amount = XrpDrops(1_000_000),
                        publicKey = "aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3",
                        signature = "DEADBEEFDEADBEEFDEADBEEFDEADBEEFDEADBEEFDEADBEEFDEADBEEFDEADBEEF",
                    )
                result.shouldBeInstanceOf<XrplResult.Success<ChannelVerifyResult>>()
                val data = (result as XrplResult.Success<ChannelVerifyResult>).value
                data.signatureVerified shouldBe false
            }
        }
    }
})
