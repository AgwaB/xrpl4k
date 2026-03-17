package org.xrpl.sdk.client.subscription

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.XrplJson
import org.xrpl.sdk.client.internal.dto.ConsensusEventDto
import org.xrpl.sdk.client.internal.dto.LedgerEventDto
import org.xrpl.sdk.client.internal.dto.ManifestEventDto
import org.xrpl.sdk.client.internal.dto.OrderBookSpec
import org.xrpl.sdk.client.internal.dto.PathFindCloseRequest
import org.xrpl.sdk.client.internal.dto.PathFindCreateRequest
import org.xrpl.sdk.client.internal.dto.PathFindStreamEventDto
import org.xrpl.sdk.client.internal.dto.PeerStatusEventDto
import org.xrpl.sdk.client.internal.dto.ServerEventDto
import org.xrpl.sdk.client.internal.dto.TransactionEventDto
import org.xrpl.sdk.client.internal.dto.ValidationEventDto
import org.xrpl.sdk.client.internal.executeWsRpc
import org.xrpl.sdk.client.model.AccountEvent
import org.xrpl.sdk.client.model.ConsensusEvent
import org.xrpl.sdk.client.model.LedgerEvent
import org.xrpl.sdk.client.model.ManifestEvent
import org.xrpl.sdk.client.model.OrderBookEvent
import org.xrpl.sdk.client.model.PathFindEvent
import org.xrpl.sdk.client.model.PeerStatusEvent
import org.xrpl.sdk.client.model.ServerEvent
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

/**
 * Subscribes to peer status events via WebSocket.
 *
 * Reports changes in the peer-to-peer network status of the connected server
 * (e.g., peer connections/disconnections). Requires admin access on most servers.
 *
 * @return a cold [Flow] of [PeerStatusEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToPeerStatus(): Flow<PeerStatusEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("peer_status"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("peer_status")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "peerStatusChange"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(PeerStatusEventDto.serializer(), json)
                            val event =
                                PeerStatusEvent(
                                    action = dto.action,
                                    date = dto.date,
                                    ledgerHash = dto.ledgerHash,
                                    ledgerIndex = dto.ledgerIndex,
                                    ledgerIndexMax = dto.ledgerIndexMax,
                                    ledgerIndexMin = dto.ledgerIndexMin,
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
 * Subscribes to manifest events via WebSocket.
 *
 * Reports when the server receives a manifest (validator key rotation announcements).
 * Requires admin access on most servers.
 *
 * @return a cold [Flow] of [ManifestEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToManifests(): Flow<ManifestEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("manifests"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("manifests")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "manifestReceived"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(ManifestEventDto.serializer(), json)
                            val event =
                                ManifestEvent(
                                    masterKey = dto.masterKey,
                                    signingKey = dto.signingKey,
                                    seq = dto.seq,
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
 * Subscribes to proposed (unvalidated) transaction events via WebSocket.
 *
 * Similar to [subscribeToTransactions] but includes transactions that have not
 * yet been validated by consensus. The `validated` field on each event indicates
 * whether the transaction has been included in a validated ledger.
 *
 * @return a cold [Flow] of [TransactionEvent].
 */
public fun XrplClient.subscribeToProposedTransactions(): Flow<TransactionEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("transactions_proposed"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("transactions_proposed")).getOrThrow()

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
 * Subscribes to proposed (unvalidated) transactions affecting a specific account via WebSocket.
 *
 * Similar to [subscribeToAccount] but includes transactions that have not yet been
 * validated by consensus. The `validated` field on each event indicates whether the
 * transaction has been included in a validated ledger.
 *
 * Note: Auto-resubscribe on reconnect for accounts_proposed requires an
 * AccountsProposed variant in WebSocketTransport.SubscriptionEntry. Until that
 * is added, this subscription is NOT automatically replayed after reconnect.
 *
 * @param address the account to monitor.
 * @return a cold [Flow] of [AccountEvent].
 */
public fun XrplClient.subscribeToProposedAccount(address: Address): Flow<AccountEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.AccountsProposed(listOf(address.value))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(accountsProposed = listOf(address)).getOrThrow()

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
 * Subscribes to server status events via WebSocket.
 *
 * Reports changes in the connected server's status, such as load factor changes
 * or transitions between states (e.g., "connected", "syncing", "full", "proposing").
 *
 * @return a cold [Flow] of [ServerEvent] that subscribes on first collect.
 */
public fun XrplClient.subscribeToServer(): Flow<ServerEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()
        val entry = WebSocketTransport.SubscriptionEntry.Streams(listOf("server"))

        // Subscribe FIRST — server starts sending events (buffered in SharedFlow)
        subscribe(streams = listOf("server")).getOrThrow()

        // Track for auto-resubscribe on reconnect
        transport.trackSubscription(entry)

        // THEN start collecting — picks up buffered events
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "serverStatus"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(ServerEventDto.serializer(), json)
                            val event =
                                ServerEvent(
                                    serverStatus = dto.serverStatus,
                                    loadFactor = dto.loadFactor,
                                    baseFee = dto.baseFee,
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
 * Opens a streaming path_find session via WebSocket.
 *
 * Sends a `path_find` create command and emits path update events as the server
 * discovers and refines payment paths. When the Flow is cancelled, a `path_find`
 * close command is sent automatically.
 *
 * Unlike other subscription methods, this uses the `path_find` command with
 * `create`/`close` subcommands rather than `subscribe`/`unsubscribe`.
 *
 * @param sourceAccount the account that would send the payment.
 * @param destinationAccount the account that would receive the payment.
 * @param destinationAmount the amount the destination should receive.
 * @return a cold [Flow] of [PathFindEvent] that opens a path_find session on first collect.
 */
public fun XrplClient.streamPathFind(
    sourceAccount: Address,
    destinationAccount: Address,
    destinationAmount: JsonElement,
): Flow<PathFindEvent> =
    callbackFlow {
        val transport = getWebSocketTransport()

        // Send path_find create command
        executeWsRpc(
            method = "path_find",
            request =
                PathFindCreateRequest(
                    sourceAccount = sourceAccount.value,
                    destinationAccount = destinationAccount.value,
                    destinationAmount = destinationAmount,
                ),
            requestSerializer = PathFindCreateRequest.serializer(),
            responseDeserializer = PathFindStreamEventDto.serializer(),
        ) { it }.getOrThrow()

        // path_find is not tracked for auto-resubscribe — it requires create/close lifecycle

        // Collect path updates
        val job =
            launch {
                transport.subscriptionEvents
                    .filter { json ->
                        json["type"]?.jsonPrimitive?.contentOrNull == "path_find"
                    }
                    .collect { json ->
                        try {
                            val dto = XrplJson.decodeFromJsonElement(PathFindStreamEventDto.serializer(), json)
                            val event =
                                PathFindEvent(
                                    alternatives = dto.alternatives,
                                    sourceAccount = dto.sourceAccount?.let { Address(it) },
                                    destinationAccount = dto.destinationAccount?.let { Address(it) },
                                    destinationAmount = dto.destinationAmount,
                                    fullReply = dto.fullReply,
                                )
                            send(event)
                        } catch (_: Exception) {
                            // Skip malformed events
                        }
                    }
            }

        awaitClose {
            job.cancel()
            // Send path_find close to clean up the server-side session
            launch {
                try {
                    executeWsRpc(
                        method = "path_find",
                        request = PathFindCloseRequest(),
                        requestSerializer = PathFindCloseRequest.serializer(),
                        responseDeserializer = PathFindStreamEventDto.serializer(),
                    ) { }
                } catch (_: Exception) {
                    // Best-effort close
                }
            }
        }
    }

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
