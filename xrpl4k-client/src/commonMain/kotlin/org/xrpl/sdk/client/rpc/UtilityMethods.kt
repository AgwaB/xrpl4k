package org.xrpl.sdk.client.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.core.result.XrplResult

// ---------------------------------------------------------------------------
// Internal response DTOs (trivial, defined locally)
// ---------------------------------------------------------------------------

@Serializable
private data class PingResponseDto(
    val status: String? = null,
)

@Serializable
private data class RandomResponseDto(
    @SerialName("random") val random: String? = null,
)

// ---------------------------------------------------------------------------
// Public extension functions
// ---------------------------------------------------------------------------

/**
 * Sends a ping to the XRPL node to confirm the connection is alive.
 *
 * The XRPL `ping` command returns an empty result object on success.
 *
 * @return [XrplResult] containing [Unit] on success or a categorized failure.
 */
public suspend fun XrplClient.ping(): XrplResult<Unit> =
    executeRpc(
        method = "ping",
        responseDeserializer = PingResponseDto.serializer(),
    ) {
        Unit
    }

/**
 * Returns a random 256-bit value from the XRPL node.
 *
 * @return [XrplResult] containing the 64-character hex random value or a categorized failure.
 */
public suspend fun XrplClient.random(): XrplResult<String> =
    executeRpc(
        method = "random",
        responseDeserializer = RandomResponseDto.serializer(),
    ) { dto ->
        dto.random ?: ""
    }
