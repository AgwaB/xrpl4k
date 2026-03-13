@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.rpc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.TestHelper.clientWithMockEngine
import org.xrpl.sdk.client.TestHelper.successResponse
import org.xrpl.sdk.core.result.XrplResult

class UtilityMethodsTest : FunSpec({

    // ── ping ─────────────────────────────────────────────────────

    test("ping returns XrplResult.Success with Unit") {
        runTest {
            val client = clientWithMockEngine(successResponse(""""status":"success""""))
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Success<Unit>>()
                (result as XrplResult.Success<Unit>).value shouldBe Unit
            }
        }
    }

    test("ping with empty result object returns XrplResult.Success") {
        runTest {
            val client = clientWithMockEngine(successResponse(""))
            client.use { c ->
                val result = c.ping()
                result.shouldBeInstanceOf<XrplResult.Success<Unit>>()
            }
        }
    }

    // ── random ───────────────────────────────────────────────────

    test("random returns XrplResult.Success with 64-character hex value") {
        runTest {
            val randomHex = "A6C51BBEA8C6C9E00B0F51CD8DCFC70AA6C51BBEA8C6C9E00B0F51CD8DCFC70A"
            val client =
                clientWithMockEngine(
                    successResponse(""""random":"$randomHex""""),
                )
            client.use { c ->
                val result = c.random()
                result.shouldBeInstanceOf<XrplResult.Success<String>>()
                val value = (result as XrplResult.Success<String>).value
                value shouldBe randomHex
                value.length shouldBe 64
            }
        }
    }

    test("random with missing random field returns empty string") {
        runTest {
            val client = clientWithMockEngine(successResponse(""))
            client.use { c ->
                val result = c.random()
                result.shouldBeInstanceOf<XrplResult.Success<String>>()
                (result as XrplResult.Success<String>).value shouldBe ""
            }
        }
    }

    test("random value is non-null and non-empty when present") {
        runTest {
            val randomHex = "B1D2E3F4A5B6C7D8E9F0A1B2C3D4E5F6B1D2E3F4A5B6C7D8E9F0A1B2C3D4E5F6"
            val client =
                clientWithMockEngine(
                    successResponse(""""random":"$randomHex""""),
                )
            client.use { c ->
                val result = c.random()
                result.shouldBeInstanceOf<XrplResult.Success<String>>()
                val value = (result as XrplResult.Success<String>).value
                value shouldNotBe null
                value.isNotEmpty() shouldBe true
            }
        }
    }
})
