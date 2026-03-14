package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.FeatureRequest
import org.xrpl.sdk.client.internal.dto.FeatureResponseDto
import org.xrpl.sdk.client.internal.dto.FeeResponseDto
import org.xrpl.sdk.client.internal.dto.ManifestRequest
import org.xrpl.sdk.client.internal.dto.ManifestResponseDto
import org.xrpl.sdk.client.internal.dto.ServerDefinitionsResponseDto
import org.xrpl.sdk.client.internal.dto.ServerInfoResponseDto
import org.xrpl.sdk.client.internal.dto.ServerStateResponseDto
import org.xrpl.sdk.client.internal.dto.VersionResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.FeatureEntry
import org.xrpl.sdk.client.model.FeatureResult
import org.xrpl.sdk.client.model.FeeDrops
import org.xrpl.sdk.client.model.FeeResult
import org.xrpl.sdk.client.model.LastCloseInfo
import org.xrpl.sdk.client.model.ManifestResult
import org.xrpl.sdk.client.model.ServerInfo
import org.xrpl.sdk.client.model.ValidatedLedgerInfo
import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.result.XrplResult
import org.xrpl.sdk.core.type.LedgerIndex
import org.xrpl.sdk.core.type.XrpDrops

/**
 * Returns general information about the current state of the rippled server.
 *
 * @return [XrplResult] containing [ServerInfo] or a categorized failure.
 */
public suspend fun XrplClient.serverInfo(): XrplResult<ServerInfo> =
    executeRpc(
        method = "server_info",
        responseDeserializer = ServerInfoResponseDto.serializer(),
    ) { dto ->
        mapServerInfo(dto.info)
    }

/**
 * Returns server state information using the internal (machine-readable) format.
 *
 * @return [XrplResult] containing [ServerInfo] or a categorized failure.
 */
public suspend fun XrplClient.serverState(): XrplResult<ServerInfo> =
    executeRpc(
        method = "server_state",
        responseDeserializer = ServerStateResponseDto.serializer(),
    ) { dto ->
        mapServerInfo(dto.state)
    }

/**
 * Returns the server's serialization field definitions and type codes.
 *
 * This is an experimental API that may change without notice.
 *
 * @return [XrplResult] containing a raw [JsonElement] definitions object or a categorized failure.
 */
@ExperimentalXrplApi
public suspend fun XrplClient.serverDefinitions(): XrplResult<JsonElement?> =
    executeRpc(
        method = "server_definitions",
        responseDeserializer = ServerDefinitionsResponseDto.serializer(),
    ) { dto ->
        dto.types
    }

/**
 * Returns the current transaction fee schedule and queue state.
 *
 * @return [XrplResult] containing [FeeResult] or a categorized failure.
 */
public suspend fun XrplClient.fee(): XrplResult<FeeResult> =
    executeRpc(
        method = "fee",
        responseDeserializer = FeeResponseDto.serializer(),
    ) { dto ->
        val dropsDto = dto.drops
        FeeResult(
            currentLedgerSize = dto.currentLedgerSize,
            currentQueueSize = dto.currentQueueSize,
            drops =
                FeeDrops(
                    baseFee = dropsDto?.baseFee?.toLongOrNull()?.let { XrpDrops(it) },
                    medianFee = dropsDto?.medianFee?.toLongOrNull()?.let { XrpDrops(it) },
                    minimumFee = dropsDto?.minimumFee?.toLongOrNull()?.let { XrpDrops(it) },
                    openLedgerFee = dropsDto?.openLedgerFee?.toLongOrNull()?.let { XrpDrops(it) },
                ),
            expectedLedgerSize = dto.expectedLedgerSize,
            ledgerCurrentIndex = dto.ledgerCurrentIndex?.let { LedgerIndex(it) },
            maxQueueSize = dto.maxQueueSize,
        )
    }

/**
 * Returns the manifest for the given validator public key.
 *
 * @param publicKey The base58-encoded public key of the validator.
 * @return [XrplResult] containing [ManifestResult] or a categorized failure.
 */
public suspend fun XrplClient.manifest(publicKey: String): XrplResult<ManifestResult> {
    val request = ManifestRequest(publicKey = publicKey)
    return executeRpc(
        method = "manifest",
        request = request,
        requestSerializer = ManifestRequest.serializer(),
        responseDeserializer = ManifestResponseDto.serializer(),
    ) { dto ->
        ManifestResult(
            manifest = dto.manifest,
            requested = dto.requested,
        )
    }
}

/**
 * Returns information about amendments on the XRPL.
 *
 * @param feature Optional hex-string amendment ID to query a single feature.
 *                Pass null to return all features.
 * @return [XrplResult] containing [FeatureResult] or a categorized failure.
 */
public suspend fun XrplClient.feature(feature: String? = null): XrplResult<FeatureResult> {
    val request = FeatureRequest(feature = feature)
    return executeRpc(
        method = "feature",
        request = request,
        requestSerializer = FeatureRequest.serializer(),
        responseDeserializer = FeatureResponseDto.serializer(),
    ) { dto ->
        FeatureResult(
            features =
                dto.features.mapValues { (_, entry) ->
                    FeatureEntry(
                        enabled = entry.enabled,
                        name = entry.name,
                        supported = entry.supported,
                        vetoed =
                            when (val v = entry.vetoed) {
                                is kotlinx.serialization.json.JsonPrimitive -> {
                                    val content = v.content
                                    content == "true"
                                }
                                else -> null
                            },
                    )
                },
        )
    }
}

/**
 * Returns the rippled server version string.
 *
 * @return [XrplResult] containing the version string or a categorized failure.
 */
public suspend fun XrplClient.version(): XrplResult<String?> =
    executeRpc(
        method = "version",
        responseDeserializer = VersionResponseDto.serializer(),
    ) { dto ->
        dto.version?.good
    }

// ---------------------------------------------------------------------------
// Internal mapper
// ---------------------------------------------------------------------------

private fun mapServerInfo(dto: org.xrpl.sdk.client.internal.dto.ServerInfoStateDto?): ServerInfo =
    ServerInfo(
        buildVersion = dto?.buildVersion,
        completeLedgers = dto?.completeLedgers,
        hostId = dto?.hostId,
        ioLatencyMs = dto?.ioLatencyMs,
        lastClose =
            dto?.lastClose?.let { lc ->
                LastCloseInfo(
                    convergeTime = lc.convergeTime,
                    proposers = lc.proposers,
                )
            },
        loadFactor = dto?.loadFactor,
        peers = dto?.peers,
        pubkeyNode = dto?.pubkeyNode,
        serverState = dto?.serverState,
        uptime = dto?.uptime,
        validatedLedger =
            dto?.validatedLedger?.let { vl ->
                ValidatedLedgerInfo(
                    age = vl.age,
                    baseFeeXrp = vl.baseFeeXrp,
                    hash = vl.hash,
                    reserveBaseXrp = vl.reserveBaseXrp,
                    reserveIncXrp = vl.reserveIncXrp,
                    seq = vl.seq?.let { LedgerIndex(it) },
                )
            },
        validationQuorum = dto?.validationQuorum,
        networkId = dto?.networkId,
    )
