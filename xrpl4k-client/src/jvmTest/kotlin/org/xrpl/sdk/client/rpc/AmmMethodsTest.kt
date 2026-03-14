@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.AmmInfo
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

private val AMM_ACCOUNT = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")

class AmmMethodsTest : FunSpec({

    // ── ammInfo ───────────────────────────────────────────────────

    test("ammInfo returns AmmInfo with all fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"amm":{"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"amount":"1000000000","amount2":"500000000",""" +
                            """"trading_fee":500,""" +
                            """"lp_token":"10000000"},"ledger_index":5000""",
                    ),
                )
            client.use { c ->
                val result = c.ammInfo(asset = JsonPrimitive("XRP"))
                result.shouldBeInstanceOf<XrplResult.Success<AmmInfo>>()
                val info = (result as XrplResult.Success<AmmInfo>).value
                info.account shouldBe AMM_ACCOUNT
                info.tradingFee shouldBe 500L
                info.ledgerIndex?.value shouldBe 5000u
            }
        }
    }

    test("ammInfo with no amm object returns null fields") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(""""ledger_index":1000"""),
                )
            client.use { c ->
                val result = c.ammInfo(asset = JsonPrimitive("XRP"))
                result.shouldBeInstanceOf<XrplResult.Success<AmmInfo>>()
                val info = (result as XrplResult.Success<AmmInfo>).value
                info.account shouldBe null
                info.amount shouldBe null
                info.amount2 shouldBe null
                info.tradingFee shouldBe null
                info.lpToken shouldBe null
                info.ledgerIndex?.value shouldBe 1000u
            }
        }
    }

    test("ammInfo with two assets maps both assets correctly") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"amm":{"account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"amount":"2000000","amount2":"1000000","trading_fee":200},""" +
                            """"ledger_index":9999""",
                    ),
                )
            client.use { c ->
                val result =
                    c.ammInfo(
                        asset = JsonPrimitive("XRP"),
                        asset2 = JsonPrimitive("USD"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<AmmInfo>>()
                val info = (result as XrplResult.Success<AmmInfo>).value
                info.account shouldBe Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
                info.tradingFee shouldBe 200L
                info.ledgerIndex?.value shouldBe 9999u
            }
        }
    }

    test("ammInfo ledgerIndex is null when not present in response") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"amm":{"account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","trading_fee":100}""",
                    ),
                )
            client.use { c ->
                val result = c.ammInfo(asset = JsonPrimitive("XRP"))
                result.shouldBeInstanceOf<XrplResult.Success<AmmInfo>>()
                val info = (result as XrplResult.Success<AmmInfo>).value
                info.account shouldBe AMM_ACCOUNT
                info.tradingFee shouldBe 100L
                info.ledgerIndex shouldBe null
            }
        }
    }
})
