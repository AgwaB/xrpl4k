package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.RipplePathFindRequest
import org.xrpl.sdk.client.internal.dto.RipplePathFindResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.PathAlternative
import org.xrpl.sdk.client.model.PathFindResult
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.Address

/**
 * Finds the best paths for making a payment from a source account to a destination account.
 *
 * **Note:** The XRPL `path_find` command is a WebSocket-only subscription command requiring
 * subcommands (`create`, `close`, `status`). This method delegates to [ripplePathFind],
 * which is the HTTP-compatible one-shot equivalent.
 *
 * @param sourceAccount The account that would send the payment.
 * @param destinationAccount The account that would receive the payment.
 * @param destinationAmount The amount the destination should receive.
 * @return [XrplResult] containing [PathFindResult] on success.
 */
@Deprecated(
    "path_find is a WebSocket-only subscription command. Use ripplePathFind instead.",
    replaceWith = ReplaceWith("ripplePathFind(sourceAccount, destinationAccount, destinationAmount)"),
)
public suspend fun XrplClient.pathFind(
    sourceAccount: Address,
    destinationAccount: Address,
    destinationAmount: JsonElement,
): XrplResult<PathFindResult> =
    ripplePathFind(
        sourceAccount = sourceAccount,
        destinationAccount = destinationAccount,
        destinationAmount = destinationAmount,
    )

/**
 * Finds the cheapest paths for sending a currency without holding that currency directly.
 *
 * @param sourceAccount The account that would send the payment.
 * @param destinationAccount The account that would receive the payment.
 * @param destinationAmount The amount the destination should receive.
 * @param sourceCurrencies Optional list of source currency specifiers to consider.
 * @return [XrplResult] containing [PathFindResult] on success.
 */
public suspend fun XrplClient.ripplePathFind(
    sourceAccount: Address,
    destinationAccount: Address,
    destinationAmount: JsonElement,
    sourceCurrencies: List<JsonElement>? = null,
): XrplResult<PathFindResult> =
    executeRpc(
        method = "ripple_path_find",
        request =
            RipplePathFindRequest(
                sourceAccount = sourceAccount.value,
                destinationAccount = destinationAccount.value,
                destinationAmount = destinationAmount,
                sourceCurrencies = sourceCurrencies,
            ),
        requestSerializer = RipplePathFindRequest.serializer(),
        responseDeserializer = RipplePathFindResponseDto.serializer(),
    ) { resp ->
        PathFindResult(
            alternatives =
                resp.alternatives.map { a ->
                    PathAlternative(
                        pathsComputed = a.pathsComputed,
                        sourceAmount = a.sourceAmount,
                        destinationAmount = a.destinationAmount,
                    )
                },
            sourceAccount = resp.sourceAccount?.let { Address(it) },
            destinationAccount = resp.destinationAccount?.let { Address(it) },
            destinationAmount = resp.destinationAmount,
        )
    }
