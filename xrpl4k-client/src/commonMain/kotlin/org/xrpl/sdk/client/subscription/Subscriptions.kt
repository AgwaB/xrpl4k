package org.xrpl.sdk.client.subscription

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.XrplJson
import org.xrpl.sdk.client.internal.dto.ConsensusEventDto
import org.xrpl.sdk.client.internal.dto.LedgerEventDto
import org.xrpl.sdk.client.internal.dto.OrderBookSpec
import org.xrpl.sdk.client.internal.dto.TransactionEventDto
import org.xrpl.sdk.client.internal.dto.ValidationEventDto
import org.xrpl.sdk.client.model.AccountEvent
import org.xrpl.sdk.client.model.ConsensusEvent
import org.xrpl.sdk.client.model.LedgerEvent
import org.xrpl.sdk.client.model.OrderBookEvent
import org.xrpl.sdk.client.model.TransactionEvent
import org.xrpl.sdk.client.model.ValidationEvent
import org.xrpl.sdk.client.rpc.subscribe
import org.xrpl.sdk.client.transport.WebSocketTransport
import org.xrpl.sdk.core.model.amount.CurrencySpec
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash

/**
 * Subscribes to ledger close events via WebSocket.
 *
 * The subscribe command is sent first, then the collector is activated.
 * The SharedFlow's extraBufferCapacity ensures no events are lost between
 * subscription registration and collector startup.
 *
 * When auto-reconnect is enabled, the subscription is tracked and automatically
 * replayed after reconnection. The Flow continues emitting events transparently.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @return a cold [Flow] of [LedgerEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToLedger(): Flow<LedgerEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("ledger"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("ledger")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "ledgerClosed"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(LedgerEventDto.serializer(), json)
                            val event =
                                LedgerEvent(
                                    ledgerIndex = LedgerIndex(dto.ledgerIndex.toUInt()),
                                    ledgerHash = Hash256(dto.ledgerHash),
                                    txnCount = dto.txnCount,
                                    closeTime = dto.ledgerTime,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

/**
 * Subscribes to all transaction events via WebSocket.
 *
 * The subscribe command is sent first, then the collector is activated.
 * The SharedFlow's extraBufferCapacity ensures no events are lost between
 * subscription registration and collector startup.
 *
 * When auto-reconnect is enabled, the subscription is tracked and automatically
 * replayed after reconnection. The Flow continues emitting events transparently.
 *
 * @return a cold [Flow] of [TransactionEvent].
 */
public fun XrplClient.subscribeToTransactions(): Flow<TransactionEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("transactions"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("transactions")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "transaction"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(TransactionEventDto.serializer(), json)
                            val event =
                                TransactionEvent(
                                    hash = json["hash"]?.jsonPrimitive?.contentOrNull?.let { TxHash(it) },
                                    engineResult = dto.engineResult,
                                    engineResultCode = dto.engineResultCode,
                                    ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
                                    validated = dto.validated ?: false,
                                    transaction = dto.transaction,
                                    meta = dto.meta,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

/**
 * Subscribes to events affecting a specific account via WebSocket.
 *
 * The subscribe command is sent first, then the collector is activated.
 * The SharedFlow's extraBufferCapacity ensures no events are lost between
 * subscription registration and collector startup.
 *
 * When auto-reconnect is enabled, the subscription is tracked and automatically
 * replayed after reconnection. The Flow continues emitting events transparently.
 *
 * @param address the account to monitor.
 * @return a cold [Flow] of [AccountEvent].
 */
public fun XrplClient.subscribeToAccount(address: Address): Flow<AccountEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Accounts(listOf(address.value))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(accounts = listOf(address)).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "transaction"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(TransactionEventDto.serializer(), json)
                            val event =
                                AccountEvent(
                                    hash = json["hash"]?.jsonPrimitive?.contentOrNull?.let { TxHash(it) },
                                    engineResult = dto.engineResult,
                                    engineResultCode = dto.engineResultCode,
                                    ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
                                    validated = dto.validated ?: false,
                                    transaction = dto.transaction,
                                    meta = dto.meta,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

/**
 * Subscribes to order book changes via WebSocket.
 *
 * The subscribe command is sent first, then the collector is activated.
 * The SharedFlow's extraBufferCapacity ensures no events are lost between
 * subscription registration and collector startup.
 *
 * When auto-reconnect is enabled, the subscription is tracked and automatically
 * replayed after reconnection. The Flow continues emitting events transparently.
 *
 * @param takerGets the currency the taker gets.
 * @param takerPays the currency the taker pays.
 * @param snapshot if true, the server sends a snapshot of the current order book before streaming updates.
 * @return a cold [Flow] of [OrderBookEvent].
 */
public fun XrplClient.subscribeToOrderBook(
    takerGets: CurrencySpec,
    takerPays: CurrencySpec,
    snapshot: Boolean = false,
): Flow<OrderBookEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val bookSpec =
            OrderBookSpec(
                takerGets = takerGets.toDto(),
                takerPays = takerPays.toDto(),
                snapshot = if (snapshot) true else null,
            )
        val booksJson =
            XrplJson.encodeToJsonElement(
                kotlinx.serialization.builtins.ListSerializer(OrderBookSpec.serializer()),
                listOf(bookSpec),
            )
        val entry =
            WebSocketTransport.SubscriptionEntry.Books(
                kotlinx.serialization.json.buildJsonObject {
                    put("books", booksJson)
                },
            )

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(books = listOf(bookSpec)).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "transaction"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(TransactionEventDto.serializer(), json)
                            val event =
                                OrderBookEvent(
                                    ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
                                    transaction = dto.transaction,
                                    meta = dto.meta,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

/**
 * Subscribes to order book changes via WebSocket.
 *
 * @param takerGets the currency the taker gets (raw string, e.g. "XRP").
 * @param takerPays the currency the taker pays (raw string, e.g. "USD").
 * @return a cold [Flow] of [OrderBookEvent].
 */
@Deprecated(
    message = "Use the overload with CurrencySpec parameters instead.",
    replaceWith =
        ReplaceWith(
            "subscribeToOrderBook(takerGets = TODO(), takerPays = TODO())",
            "org.xrpl.sdk.core.model.amount.CurrencySpec",
        ),
)
public fun XrplClient.subscribeToOrderBook(
    takerGets: String,
    takerPays: String,
): Flow<OrderBookEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val bookSpec =
            OrderBookSpec(
                takerGets = org.xrpl.sdk.client.internal.dto.CurrencySpec(currency = takerGets),
                takerPays = org.xrpl.sdk.client.internal.dto.CurrencySpec(currency = takerPays),
            )
        val booksJson =
            XrplJson.encodeToJsonElement(
                kotlinx.serialization.builtins.ListSerializer(OrderBookSpec.serializer()),
                listOf(bookSpec),
            )
        val entry =
            WebSocketTransport.SubscriptionEntry.Books(
                kotlinx.serialization.json.buildJsonObject {
                    put("books", booksJson)
                },
            )

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(books = listOf(bookSpec)).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "transaction"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(TransactionEventDto.serializer(), json)
                            val event =
                                OrderBookEvent(
                                    ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
                                    transaction = dto.transaction,
                                    meta = dto.meta,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

/**
 * Subscribes to validation events via WebSocket.
 *
 * The subscribe command is sent first, then the collector is activated.
 * The SharedFlow's extraBufferCapacity ensures no events are lost between
 * subscription registration and collector startup.
 *
 * When auto-reconnect is enabled, the subscription is tracked and automatically
 * replayed after reconnection. The Flow continues emitting events transparently.
 *
 * @return a cold [Flow] of [ValidationEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToValidations(): Flow<ValidationEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("validations"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("validations")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "validationReceived"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(ValidationEventDto.serializer(), json)
                            val event =
                                ValidationEvent(
                                    ledgerHash = dto.ledgerHash,
                                    ledgerIndex = dto.ledgerIndex?.let { LedgerIndex(it.toUInt()) },
                                    signature = dto.signature,
                                    signingTime = dto.signingTime,
                                    validationPublicKey = dto.validationPublicKey,
                                    flags = dto.flags,
                                    full = dto.full,
                                    rawJson = json,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

/**
 * Subscribes to consensus phase events via WebSocket.
 *
 * The subscribe command is sent first, then the collector is activated.
 * The SharedFlow's extraBufferCapacity ensures no events are lost between
 * subscription registration and collector startup.
 *
 * When auto-reconnect is enabled, the subscription is tracked and automatically
 * replayed after reconnection. The Flow continues emitting events transparently.
 *
 * @return a cold [Flow] of [ConsensusEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToConsensus(): Flow<ConsensusEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("consensus"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("consensus")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "consensusPhase"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(ConsensusEventDto.serializer(), json)
                            val event =
                                ConsensusEvent(
                                    phase = dto.consensus,
                                    rawJson = json,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Untrack subscription so it's not replayed after reconnect
            launch { transport.untrackSubscription(entry) }
        }
    }

// TODO: subscribeToPathFind — uses the `path_find` command (create/close/status)
//  rather than `subscribe`. This requires a different WebSocket interaction pattern
//  (path_find create → streaming updates → path_find close) and is deferred for now.

/**
 * Converts a public [CurrencySpec] to the internal DTO [org.xrpl.sdk.client.internal.dto.CurrencySpec].
 */
private fun CurrencySpec.toDto(): org.xrpl.sdk.client.internal.dto.CurrencySpec =
    when (this) {
        is CurrencySpec.Xrp ->
            org.xrpl.sdk.client.internal.dto.CurrencySpec(currency = "XRP")
        is CurrencySpec.Issued ->
            org.xrpl.sdk.client.internal.dto.CurrencySpec(
                currency = currency.value,
                issuer = issuer.value,
            )
    }

/**
 * Gets the lazily-initialized WebSocket transport for subscriptions.
 */
internal fun XrplClient.getWebSocketTransport(): WebSocketTransport = webSocketTransport
