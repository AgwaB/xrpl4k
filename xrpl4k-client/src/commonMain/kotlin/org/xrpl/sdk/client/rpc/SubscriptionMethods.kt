package org.xrpl.sdk.client.rpc

import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.OrderBookSpec
import org.xrpl.sdk.client.internal.dto.SubscribeRequest
import org.xrpl.sdk.client.internal.dto.SubscribeResponse
import org.xrpl.sdk.client.internal.dto.UnsubscribeRequest
import org.xrpl.sdk.client.internal.executeWsRpc
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

/**
 * Sends a `subscribe` command via WebSocket.
 *
 * This is internal — the public API is the Flow-based methods in [org.xrpl.sdk.client.subscription].
 */
internal suspend fun XrplClient.subscribe(
    streams: List<String>? = null,
    accounts: List<Address>? = null,
    books: List<OrderBookSpec>? = null,
): XrplResult<SubscribeResponse> {
    return executeWsRpc(
        method = "subscribe",
        request =
            SubscribeRequest(
                streams = streams,
                accounts = accounts?.map { it.value },
                books = books,
            ),
        requestSerializer = SubscribeRequest.serializer(),
        responseDeserializer = SubscribeResponse.serializer(),
    ) { it }
}

/**
 * Sends an `unsubscribe` command via WebSocket.
 *
 * This is internal — the public API is Flow cancellation.
 */
internal suspend fun XrplClient.unsubscribe(
    streams: List<String>? = null,
    accounts: List<Address>? = null,
): XrplResult<Unit> {
    return executeWsRpc(
        method = "unsubscribe",
        request =
            UnsubscribeRequest(
                streams = streams,
                accounts = accounts?.map { it.value },
            ),
        requestSerializer = UnsubscribeRequest.serializer(),
        responseDeserializer = SubscribeResponse.serializer(),
    ) { }
}
