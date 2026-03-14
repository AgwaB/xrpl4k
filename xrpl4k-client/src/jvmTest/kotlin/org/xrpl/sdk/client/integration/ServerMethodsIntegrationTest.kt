@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.integration

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.xrpl.sdk.client.rpc.fee
import org.xrpl.sdk.client.rpc.ledgerClosed
import org.xrpl.sdk.client.rpc.ledgerCurrent
import org.xrpl.sdk.client.rpc.serverInfo
import org.xrpl.sdk.client.rpc.version
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.result.getOrThrow

/**
 * Integration tests for server-level RPC methods.
 * Requires rippled standalone: `docker compose up -d rippled`
 * Run with: `./gradlew jvmTest -Pintegration`
 */
class ServerMethodsIntegrationTest : IntegrationTestBase({

    test("serverInfo returns valid response with build_version") {
        runTest {
            val client = createClient()
            client.use { c ->
                val result = c.serverInfo()
                result.shouldBeInstanceOf<XrplResult.Success<*>>()
                val info = result.getOrThrow()
                info.buildVersion.shouldNotBeNull()
            }
        }
    }

    test("fee returns current fee with drops field") {
        runTest {
            val client = createClient()
            client.use { c ->
                val result = c.fee()
                result.shouldBeInstanceOf<XrplResult.Success<*>>()
                val feeResult = result.getOrThrow()
                feeResult.drops.shouldNotBeNull()
                feeResult.drops.baseFee.shouldNotBeNull()
            }
        }
    }

    test("ledgerClosed returns ledger index greater than zero") {
        runTest {
            val client = createClient()
            client.use { c ->
                val result = c.ledgerClosed()
                result.shouldBeInstanceOf<XrplResult.Success<*>>()
                val closed = result.getOrThrow()
                (closed.ledgerIndex.value > 0u) shouldBe true
            }
        }
    }

    test("ledgerCurrent returns current ledger index") {
        runTest {
            val client = createClient()
            client.use { c ->
                val result = c.ledgerCurrent()
                result.shouldBeInstanceOf<XrplResult.Success<*>>()
                val index = result.getOrThrow()
                (index.value > 0u) shouldBe true
            }
        }
    }

    test("version returns a non-null version string") {
        runTest {
            val client = createClient()
            client.use { c ->
                val result = c.version()
                result.shouldBeInstanceOf<XrplResult.Success<*>>()
                val v = result.getOrThrow()
                v.shouldNotBeNull()
            }
        }
    }
})
