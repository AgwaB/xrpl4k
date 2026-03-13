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
import org.xrpl.sdk.client.internal.dto.LedgerEventDto
import org.xrpl.sdk.client.internal.dto.TransactionEventDto
import org.xrpl.sdk.client.model.AccountEvent
import org.xrpl.sdk.client.model.LedgerEvent
import org.xrpl.sdk.client.model.OrderBookEvent
import org.xrpl.sdk.client.model.TransactionEvent
import org.xrpl.sdk.client.rpc.subscribe
import org.xrpl.sdk.client.transport.WebSocketTransport
import org.xrpl.sdk.core.result.XrplException
import org.xrpl.sdk.core.result.getOrThrow
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.Hash256
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.TxHash

/**
 * Subscribes to ledger close events via WebSocket.
 *
 * This Flow does not automatically resume after WebSocket reconnection.
 * If the connection drops, the Flow completes exceptionally with [XrplException].
 * Use `Flow.retry {}` to re-subscribe.
 *
 * Supports cooperative cancellation via `take(N)`.
 *
 * @return a cold [Flow] of [LedgerEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToLedger(): Flow<LedgerEvent> =
    callbackFlow {
        subscribe(streams = listOf("ledger")).getOrThrow()

        val job =
            launch {
                getWebSocketTransport().subscriptionEvents
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
            // Unsubscribe is best-effort; connection may already be closed
        }
    }

/**
 * Subscribes to all transaction events via WebSocket.
 *
 * This Flow does not automatically resume after WebSocket reconnection.
 * If the connection drops, the Flow completes exceptionally with [XrplException].
 * Use `Flow.retry {}` to re-subscribe.
 *
 * @return a cold [Flow] of [TransactionEvent].
 */
public fun XrplClient.subscribeToTransactions(): Flow<TransactionEvent> =
    callbackFlow {
        subscribe(streams = listOf("transactions")).getOrThrow()

        val job =
            launch {
                getWebSocketTransport().subscriptionEvents
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
            // Unsubscribe is best-effort; connection may already be closed
        }
    }

/**
 * Subscribes to events affecting a specific account via WebSocket.
 *
 * This Flow does not automatically resume after WebSocket reconnection.
 * If the connection drops, the Flow completes exceptionally with [XrplException].
 * Use `Flow.retry {}` to re-subscribe.
 *
 * @param address the account to monitor.
 * @return a cold [Flow] of [AccountEvent].
 */
public fun XrplClient.subscribeToAccount(address: Address): Flow<AccountEvent> =
    callbackFlow {
        subscribe(accounts = listOf(address)).getOrThrow()

        val job =
            launch {
                getWebSocketTransport().subscriptionEvents
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
            // Unsubscribe is best-effort; connection may already be closed
        }
    }

/**
 * Subscribes to order book changes via WebSocket.
 *
 * This Flow does not automatically resume after WebSocket reconnection.
 * If the connection drops, the Flow completes exceptionally with [XrplException].
 * Use `Flow.retry {}` to re-subscribe.
 *
 * @param takerGets the currency the taker gets.
 * @param takerPays the currency the taker pays.
 * @return a cold [Flow] of [OrderBookEvent].
 */
public fun XrplClient.subscribeToOrderBook(
    takerGets: String,
    takerPays: String,
): Flow<OrderBookEvent> =
    callbackFlow {
        subscribe(streams = listOf("book")).getOrThrow()

        val job =
            launch {
                getWebSocketTransport().subscriptionEvents
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
        }
    }

/**
 * Gets the lazily-initialized WebSocket transport for subscriptions.
 */
internal fun XrplClient.getWebSocketTransport(): WebSocketTransport = webSocketTransport
