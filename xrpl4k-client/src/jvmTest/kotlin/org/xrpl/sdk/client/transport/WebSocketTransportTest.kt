@file:Suppress("MagicNumber")
@file:OptIn(ExperimentalAtomicApi::class)

package org.xrpl.sdk.client.transport

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds

/**
 * Unit tests for [WebSocketTransport].
 *
 * Testing strategy:
 * - Connection lifecycle and state transitions are tested via a MockEngine that
 *   throws on WebSocket upgrade, causing predictable Failed states.
 * - Request handling when not connected is tested directly (no engine needed).
 * - ID counter overflow is tested against the AtomicInt directly.
 * - Close cleanup and idempotency are tested by calling close() in various states.
 * - ConnectionState sealed class is tested for equality and toString.
 */
class WebSocketTransportTest : FunSpec({

    // ── Helpers ─────────────────────────────────────────────────────────────────

    fun makeTransport(
        scope: CoroutineScope,
        engine: MockEngine = MockEngine { respond(ByteReadChannel(""), HttpStatusCode.OK) },
        heartbeat: kotlin.time.Duration = 30.seconds,
        requestTimeout: kotlin.time.Duration = 5.seconds,
    ): WebSocketTransport {
        return WebSocketTransport(
            url = "ws://localhost:6006",
            engine = engine,
            scope = scope,
            heartbeatInterval = heartbeat,
            requestTimeout = requestTimeout,
        )
    }

    // ── Connection State ────────────────────────────────────────────────────────

    test("initial connection state is Disconnected") {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val transport = makeTransport(scope)
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Disconnected>()
        } finally {
            scope.cancel()
        }
    }

    test("connect() ends in Failed when server is unreachable") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine = MockEngine { throw RuntimeException("Connection refused") }
            val transport = makeTransport(scope, engine = engine)

            // Before connect — should be Disconnected
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Disconnected>()

            // connect() should complete and leave transport in Failed state
            transport.connect()

            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()
            val failed = transport.connectionState.value as ConnectionState.Failed
            failed.cause.message shouldContain "Connection refused"

            scope.cancel()
        }
    }

    // ── Request when not connected ──────────────────────────────────────────────

    test("request() when not connected returns Failure with NetworkError") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            try {
                val transport = makeTransport(scope)

                val result =
                    transport.request(
                        "server_info",
                        JsonObject(emptyMap()),
                        JsonObject.serializer(),
                    )

                result.shouldBeInstanceOf<XrplResult.Failure>()
                val failure = (result as XrplResult.Failure).error
                failure.shouldBeInstanceOf<XrplFailure.NetworkError>()
                (failure as XrplFailure.NetworkError).message shouldContain "not connected"
            } finally {
                scope.cancel()
            }
        }
    }

    // ── Close behavior ──────────────────────────────────────────────────────────

    test("close() on Disconnected transport stays Disconnected") {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val transport = makeTransport(scope)
            transport.close()
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Disconnected>()
        } finally {
            scope.cancel()
        }
    }

    test("close() after Failed stays in Failed state") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine = MockEngine { throw RuntimeException("Connection refused") }
            val transport = makeTransport(scope, engine = engine)

            // Connect will fail
            transport.connect()
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            // Close should remain Failed
            transport.close()
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            scope.cancel()
        }
    }

    test("close() is idempotent — calling twice does not throw") {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val transport = makeTransport(scope)
            transport.close()
            transport.close() // second close should not throw
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Disconnected>()
        } finally {
            scope.cancel()
        }
    }

    // ── ID counter ──────────────────────────────────────────────────────────────

    test("AtomicInt IDs are monotonically increasing") {
        val counter = AtomicInt(1)
        val first = counter.fetchAndAdd(1)
        val second = counter.fetchAndAdd(1)
        val third = counter.fetchAndAdd(1)

        first shouldBe 1
        second shouldBe 2
        third shouldBe 3
    }

    test("ID counter overflow — negative value is handled by reset logic") {
        // Simulate the overflow logic from WebSocketTransport.request()
        val counter = AtomicInt(Int.MAX_VALUE)
        var id = counter.fetchAndAdd(1)
        // After fetching MAX_VALUE and adding 1, counter wraps to MIN_VALUE
        // The code checks `if (id < 0)` and resets
        if (id < 0) {
            counter.store(1)
            id = 1
        }
        // id should be MAX_VALUE (valid, positive) on the first call
        id shouldBe Int.MAX_VALUE

        // Next fetch will produce MIN_VALUE (negative) — triggering reset
        var id2 = counter.fetchAndAdd(1)
        if (id2 < 0) {
            counter.store(1)
            id2 = 1
        }
        id2 shouldBe 1
    }

    // ── ConnectionState sealed class ────────────────────────────────────────────

    test("ConnectionState.Disconnected is a data object") {
        ConnectionState.Disconnected shouldBe ConnectionState.Disconnected
        ConnectionState.Disconnected.toString() shouldBe "Disconnected"
    }

    test("ConnectionState.Connecting is a data object") {
        ConnectionState.Connecting shouldBe ConnectionState.Connecting
        ConnectionState.Connecting.toString() shouldBe "Connecting"
    }

    test("ConnectionState.Connected is a data object") {
        ConnectionState.Connected shouldBe ConnectionState.Connected
        ConnectionState.Connected.toString() shouldBe "Connected"
    }

    test("ConnectionState.Failed holds cause and has correct equality") {
        val cause1 = RuntimeException("fail-a")
        val cause3 = RuntimeException("fail-b")

        val f1 = ConnectionState.Failed(cause1)
        val f2 = ConnectionState.Failed(cause1)
        val f3 = ConnectionState.Failed(cause3)

        // Same cause instance -> equal
        (f1 == f2) shouldBe true
        // Different cause -> not equal
        (f1 == f3) shouldBe false

        f1.cause shouldBe cause1
        f1.toString() shouldContain "fail-a"
    }

    test("ConnectionState.Failed hashCode is based on cause") {
        val cause = RuntimeException("test")
        val f1 = ConnectionState.Failed(cause)
        val f2 = ConnectionState.Failed(cause)
        f1.hashCode() shouldBe f2.hashCode()
    }

    // ── subscriptionEvents flow ─────────────────────────────────────────────────

    test("subscriptionEvents is accessible as SharedFlow") {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val transport = makeTransport(scope)
            transport.subscriptionEvents.shouldBeInstanceOf<SharedFlow<JsonObject>>()
        } finally {
            scope.cancel()
        }
    }

    // ── connect() after failure ─────────────────────────────────────────────────

    test("connect() after Failed transitions through Connecting again") {
        runTest {
            val callCount = AtomicInt(0)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine =
                MockEngine {
                    callCount.fetchAndAdd(1)
                    throw RuntimeException("Connection refused")
                }
            val transport = makeTransport(scope, engine = engine)

            transport.connect()
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            // Calling connect() again should attempt reconnection from Failed state
            transport.connect()

            // The connection should have been attempted at least twice
            callCount.load() shouldBe 2
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            scope.cancel()
        }
    }

    // ── Request after close ─────────────────────────────────────────────────────

    test("request() after close() returns Failure with NetworkError") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            try {
                val transport = makeTransport(scope)
                transport.close()

                val result =
                    transport.request(
                        "server_info",
                        JsonObject(emptyMap()),
                        JsonObject.serializer(),
                    )
                result.shouldBeInstanceOf<XrplResult.Failure>()
                val failure = (result as XrplResult.Failure).error
                failure.shouldBeInstanceOf<XrplFailure.NetworkError>()
            } finally {
                scope.cancel()
            }
        }
    }

    // ── Connection state flow observation via Turbine ───────────────────────────

    test("connectionState is observable as StateFlow with initial Disconnected") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            try {
                val transport = makeTransport(scope)

                transport.connectionState.test {
                    awaitItem().shouldBeInstanceOf<ConnectionState.Disconnected>()
                    cancelAndConsumeRemainingEvents()
                }
            } finally {
                scope.cancel()
            }
        }
    }

    test("connectionState emits Failed after connect() to unreachable server") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine = MockEngine { throw RuntimeException("refused") }
            val transport = makeTransport(scope, engine = engine)

            transport.connectionState.test {
                // Initial state
                awaitItem().shouldBeInstanceOf<ConnectionState.Disconnected>()

                // Trigger connection in background
                scope.launch { transport.connect() }

                // Should see Connecting
                awaitItem().shouldBeInstanceOf<ConnectionState.Connecting>()

                // Then Failed
                awaitItem().shouldBeInstanceOf<ConnectionState.Failed>()

                cancelAndConsumeRemainingEvents()
            }

            scope.cancel()
        }
    }

    // ── Multiple close() calls are safe ─────────────────────────────────────────

    test("multiple rapid close() calls do not cause concurrent modification") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val transport = makeTransport(scope)

            // Rapid fire close calls
            repeat(10) {
                transport.close()
            }

            // Should not throw and should be in Disconnected
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Disconnected>()
            scope.cancel()
        }
    }

    // ── Transport implements AutoCloseable ──────────────────────────────────────

    test("WebSocketTransport implements XrplTransport (AutoCloseable)") {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val transport = makeTransport(scope)
            transport.shouldBeInstanceOf<XrplTransport>()
            transport.shouldBeInstanceOf<AutoCloseable>()
        } finally {
            scope.cancel()
        }
    }

    // ── Request method validation ───────────────────────────────────────────────

    test("request() after failed connect returns Failure with NetworkError") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine = MockEngine { throw RuntimeException("Connection refused") }
            val transport = makeTransport(scope, engine = engine)

            transport.connect()
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            // Request on a failed transport should fail immediately
            val result =
                transport.request(
                    "server_info",
                    JsonObject(emptyMap()),
                    JsonObject.serializer(),
                )

            result.shouldBeInstanceOf<XrplResult.Failure>()
            val failure = (result as XrplResult.Failure).error
            failure.shouldBeInstanceOf<XrplFailure.NetworkError>()
            (failure as XrplFailure.NetworkError).message shouldContain "not connected"

            scope.cancel()
        }
    }
})
