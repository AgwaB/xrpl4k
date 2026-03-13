@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.client.model.PathFindResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

private val SOURCE_ADDRESS = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
private val DESTINATION_ADDRESS = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")

class PathMethodsTest : FunSpec({

    // ── pathFind ──────────────────────────────────────────────────

    test("pathFind returns PathFindResult with alternatives") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"alternatives":[{"paths_computed":[[{"currency":"USD",""" +
                            """"issuer":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"}]],""" +
                            """"source_amount":"100","destination_amount":"200"}],""" +
                            """"source_account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"destination_amount":"200"""",
                    ),
                )
            client.use { c ->
                val result =
                    c.pathFind(
                        sourceAccount = SOURCE_ADDRESS,
                        destinationAccount = DESTINATION_ADDRESS,
                        destinationAmount = JsonPrimitive("200"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<PathFindResult>>()
                val value = (result as XrplResult.Success<PathFindResult>).value
                value.alternatives shouldHaveSize 1
                val alt = value.alternatives.first()
                alt.pathsComputed shouldHaveSize 1
                alt.pathsComputed.first() shouldHaveSize 1
                value.sourceAccount shouldBe SOURCE_ADDRESS
                value.destinationAccount shouldBe DESTINATION_ADDRESS
            }
        }
    }

    test("pathFind with empty alternatives returns empty list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"alternatives":[],""" +
                            """"source_account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"""",
                    ),
                )
            client.use { c ->
                val result =
                    c.pathFind(
                        sourceAccount = SOURCE_ADDRESS,
                        destinationAccount = DESTINATION_ADDRESS,
                        destinationAmount = JsonPrimitive("100000000"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<PathFindResult>>()
                (result as XrplResult.Success<PathFindResult>).value.alternatives shouldHaveSize 0
            }
        }
    }

    test("pathFind alternative sourceAmount and destinationAmount are mapped") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"alternatives":[{"paths_computed":[],""" +
                            """"source_amount":"50000000","destination_amount":"49000000"}],""" +
                            """"source_account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"""",
                    ),
                )
            client.use { c ->
                val result =
                    c.pathFind(
                        sourceAccount = SOURCE_ADDRESS,
                        destinationAccount = DESTINATION_ADDRESS,
                        destinationAmount = JsonPrimitive("49000000"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<PathFindResult>>()
                val alt = (result as XrplResult.Success<PathFindResult>).value.alternatives.first()
                alt.sourceAmount shouldBe JsonPrimitive("50000000")
                alt.destinationAmount shouldBe JsonPrimitive("49000000")
            }
        }
    }

    // ── ripplePathFind ────────────────────────────────────────────

    test("ripplePathFind returns PathFindResult with source and destination accounts") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"alternatives":[{"paths_computed":[[{"currency":"EUR"}]],"source_amount":"1000"}],""" +
                            """"source_account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",""" +
                            """"destination_amount":"900"""",
                    ),
                )
            client.use { c ->
                val result =
                    c.ripplePathFind(
                        sourceAccount = SOURCE_ADDRESS,
                        destinationAccount = DESTINATION_ADDRESS,
                        destinationAmount = JsonPrimitive("900"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<PathFindResult>>()
                val value = (result as XrplResult.Success<PathFindResult>).value
                value.sourceAccount shouldBe SOURCE_ADDRESS
                value.destinationAccount shouldBe DESTINATION_ADDRESS
                value.alternatives shouldHaveSize 1
            }
        }
    }

    test("ripplePathFind with empty alternatives returns empty list") {
        runTest {
            val client =
                clientWithMockEngine(
                    successResponse(
                        """"alternatives":[],""" +
                            """"source_account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
                            """"destination_account":"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"""",
                    ),
                )
            client.use { c ->
                val result =
                    c.ripplePathFind(
                        sourceAccount = SOURCE_ADDRESS,
                        destinationAccount = DESTINATION_ADDRESS,
                        destinationAmount = JsonPrimitive("1000000"),
                    )
                result.shouldBeInstanceOf<XrplResult.Success<PathFindResult>>()
                (result as XrplResult.Success<PathFindResult>).value.alternatives shouldHaveSize 0
            }
        }
    }
})
