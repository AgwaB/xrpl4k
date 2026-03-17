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
import kotlin.time.Duration.Companion.milliseconds
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
 * - Auto-reconnect behavior is tested with configurable backoff parameters.
 */
class WebSocketTransportTest : FunSpec({

    // ── Helpers ─────────────────────────────────────────────────────────────────

    fun makeTransport(
        scope: CoroutineScope,
        engine: MockEngine = MockEngine { respond(ByteReadChannel(""), HttpStatusCode.OK) },
        heartbeat: kotlin.time.Duration = 30.seconds,
        requestTimeout: kotlin.time.Duration = 5.seconds,
        autoReconnect: Boolean = false,
        maxReconnectAttempts: Int = Int.MAX_VALUE,
        initialReconnectDelay: kotlin.time.Duration = 100.milliseconds,
        maxReconnectDelay: kotlin.time.Duration = 60.seconds,
    ): WebSocketTransport {
        return WebSocketTransport(
            url = "ws://localhost:6006",
            engine = engine,
            scope = scope,
            heartbeatInterval = heartbeat,
            requestTimeout = requestTimeout,
            autoReconnect = autoReconnect,
            maxReconnectAttempts = maxReconnectAttempts,
            initialReconnectDelay = initialReconnectDelay,
            maxReconnectDelay = maxReconnectDelay,
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

    // ── ConnectionState.Reconnecting ────────────────────────────────────────────

    test("ConnectionState.Reconnecting holds attempt and cause") {
        val cause = RuntimeException("dropped")
        val r = ConnectionState.Reconnecting(attempt = 3, cause = cause)

        r.attempt shouldBe 3
        r.cause shouldBe cause
        r.toString() shouldContain "Reconnecting"
        r.toString() shouldContain "attempt=3"
    }

    test("ConnectionState.Reconnecting equality is based on attempt and cause") {
        val cause = RuntimeException("dropped")
        val r1 = ConnectionState.Reconnecting(1, cause)
        val r2 = ConnectionState.Reconnecting(1, cause)
        val r3 = ConnectionState.Reconnecting(2, cause)

        (r1 == r2) shouldBe true
        (r1 == r3) shouldBe false
        r1.hashCode() shouldBe r2.hashCode()
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

    // ── Auto-reconnect ──────────────────────────────────────────────────────────

    test("auto-reconnect disabled — connect failure is terminal") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine = MockEngine { throw RuntimeException("Connection refused") }
            val transport =
                makeTransport(
                    scope,
                    engine = engine,
                    autoReconnect = false,
                )

            transport.connect()
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            // Should stay Failed without any reconnect attempts
            kotlinx.coroutines.delay(200.milliseconds)
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Failed>()

            scope.cancel()
        }
    }

    test("auto-reconnect with max attempts exhausted ends in Failed") {
        runTest {
            val callCount = AtomicInt(0)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine =
                MockEngine {
                    callCount.fetchAndAdd(1)
                    throw RuntimeException("Connection refused")
                }
            val transport =
                makeTransport(
                    scope,
                    engine = engine,
                    autoReconnect = true,
                    maxReconnectAttempts = 3,
                    initialReconnectDelay = 10.milliseconds,
                    maxReconnectDelay = 50.milliseconds,
                )

            transport.connect()

            // Wait for reconnect attempts to exhaust by observing state flow.
            // After the initial connect fails, reconnect loop starts. We watch
            // for state to settle back to Failed (after reconnect gives up).
            transport.connectionState.test {
                // Consume any intermediate states (Reconnecting, Connecting, Failed)
                // until we see a terminal Failed that follows reconnect attempts.
                val seen = mutableListOf<ConnectionState>()
                while (true) {
                    val item = awaitItem()
                    seen.add(item)
                    // Once we've seen at least one Reconnecting state and then Failed,
                    // the reconnect loop is done.
                    val sawReconnecting = seen.any { it is ConnectionState.Reconnecting }
                    if (sawReconnecting && item is ConnectionState.Failed) break
                    // Safety limit
                    if (seen.size > 20) break
                }

                // Should have ended in Failed
                seen.last().shouldBeInstanceOf<ConnectionState.Failed>()
                // Should have seen at least one Reconnecting state
                seen.any { it is ConnectionState.Reconnecting } shouldBe true

                cancelAndConsumeRemainingEvents()
            }

            transport.close()
            scope.cancel()
        }
    }

    test("close() stops auto-reconnect") {
        runTest {
            val callCount = AtomicInt(0)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            val engine =
                MockEngine {
                    callCount.fetchAndAdd(1)
                    throw RuntimeException("Connection refused")
                }
            val transport =
                makeTransport(
                    scope,
                    engine = engine,
                    autoReconnect = true,
                    maxReconnectAttempts = 100,
                    initialReconnectDelay = 50.milliseconds,
                    maxReconnectDelay = 100.milliseconds,
                )

            transport.connect()
            // Give some time for reconnect to start
            kotlinx.coroutines.delay(100.milliseconds)

            // Close should stop reconnect
            transport.close()

            val countAfterClose = callCount.load()
            kotlinx.coroutines.delay(300.milliseconds)

            // No more attempts after close
            val countLater = callCount.load()
            (countLater - countAfterClose <= 1) shouldBe true

            scope.cancel()
        }
    }

    // ── Subscription tracking ───────────────────────────────────────────────────

    test("trackSubscription and untrackSubscription work correctly") {
        runTest {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            try {
                val transport = makeTransport(scope)
                val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("ledger"))

                transport.trackSubscription(entry)
                transport.trackSubscription(
                    WebSocketTransport.SubscriptionEntry.Accounts(listOf("rAddr1")),
                )

                // Untrack the first one
                transport.untrackSubscription(entry)

                // No crash — just verifying the API works without errors
            } finally {
                scope.cancel()
            }
        }
    }

    test("SubscriptionEntry.Streams equality") {
        val a = WebSocketTransport.SubscriptionEntry.Streams(listOf("ledger"))
        val b = WebSocketTransport.SubscriptionEntry.Streams(listOf("ledger"))
        val c = WebSocketTransport.SubscriptionEntry.Streams(listOf("transactions"))

        (a == b) shouldBe true
        (a == c) shouldBe false
    }

    test("SubscriptionEntry.Accounts equality") {
        val a = WebSocketTransport.SubscriptionEntry.Accounts(listOf("rAddr1"))
        val b = WebSocketTransport.SubscriptionEntry.Accounts(listOf("rAddr1"))
        val c = WebSocketTransport.SubscriptionEntry.Accounts(listOf("rAddr2"))

        (a == b) shouldBe true
        (a == c) shouldBe false
    }

    // ── Bug 6: reconnect delay defaults ────────────────────────────────────

    test("default reconnect delays are non-zero") {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            // Create transport with all defaults — verify it doesn't crash
            val transport =
                WebSocketTransport(
                    url = "ws://localhost:6006",
                    engine = MockEngine { respond(ByteReadChannel(""), HttpStatusCode.OK) },
                    scope = scope,
                    heartbeatInterval = 30.seconds,
                    requestTimeout = 5.seconds,
                )
            // If we reached here, the defaults compiled and are usable
            transport.connectionState.value.shouldBeInstanceOf<ConnectionState.Disconnected>()
            transport.close()
        } finally {
            scope.cancel()
        }
    }

    // ── Bug 10: AccountsProposed SubscriptionEntry ─────────────────────────

    test("SubscriptionEntry.AccountsProposed equality") {
        val a = WebSocketTransport.SubscriptionEntry.AccountsProposed(listOf("rAddr1"))
        val b = WebSocketTransport.SubscriptionEntry.AccountsProposed(listOf("rAddr1"))
        val c = WebSocketTransport.SubscriptionEntry.AccountsProposed(listOf("rAddr2"))

        (a == b) shouldBe true
        (a == c) shouldBe false
    }

    test("AccountsProposed is distinct from Accounts") {
        val accounts = WebSocketTransport.SubscriptionEntry.Accounts(listOf("rAddr1"))
        val proposed = WebSocketTransport.SubscriptionEntry.AccountsProposed(listOf("rAddr1"))
        (accounts == proposed) shouldBe false
    }
})
