package org.xrpl.sdk.client.rpc

import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.client.XrplClient
import org.xrpl.sdk.client.internal.dto.FeatureRequest
import org.xrpl.sdk.client.internal.dto.FeatureResponseDto
import org.xrpl.sdk.client.internal.dto.FeeResponseDto
import org.xrpl.sdk.client.internal.dto.GetAggregatePriceRequest
import org.xrpl.sdk.client.internal.dto.GetAggregatePriceResponseDto
import org.xrpl.sdk.client.internal.dto.ManifestRequest
import org.xrpl.sdk.client.internal.dto.ManifestResponseDto
import org.xrpl.sdk.client.internal.dto.OracleSpecDto
import org.xrpl.sdk.client.internal.dto.ServerDefinitionsResponseDto
import org.xrpl.sdk.client.internal.dto.ServerInfoResponseDto
import org.xrpl.sdk.client.internal.dto.ServerStateResponseDto
import org.xrpl.sdk.client.internal.dto.VaultInfoRequest
import org.xrpl.sdk.client.internal.dto.VaultInfoResponseDto
import org.xrpl.sdk.client.internal.dto.VersionResponseDto
import org.xrpl.sdk.client.internal.executeRpc
import org.xrpl.sdk.client.model.AggregatePriceResult
import org.xrpl.sdk.client.model.AggregatePriceSet
import org.xrpl.sdk.client.model.FeatureEntry
import org.xrpl.sdk.client.model.FeatureResult
import org.xrpl.sdk.client.model.FeeDrops
import org.xrpl.sdk.client.model.FeeResult
import org.xrpl.sdk.client.model.LastCloseInfo
import org.xrpl.sdk.client.model.LedgerSpecifier
import org.xrpl.sdk.client.model.ManifestResult
import org.xrpl.sdk.client.model.OracleSpec
import org.xrpl.sdk.client.model.ServerInfo
import org.xrpl.sdk.client.model.ValidatedLedgerInfo
import org.xrpl.sdk.client.model.VaultInfoResult
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

/**
 * Retrieves the aggregate price of specified Oracle objects, returning
 * mean, median, and trimmed mean statistics.
 *
 * @param baseAsset The currency code of the base asset.
 * @param quoteAsset The currency code of the quote asset.
 * @param oracles The list of oracle specifiers (account + document ID).
 * @param trim The percentage of outliers to trim (1-25).
 * @param trimThreshold Time range in seconds for filtering older data.
 * @return [XrplResult] containing [AggregatePriceResult] or a categorized failure.
 */
public suspend fun XrplClient.getAggregatePrice(
    baseAsset: String,
    quoteAsset: String,
    oracles: List<OracleSpec>,
    trim: Int? = null,
    trimThreshold: Int? = null,
): XrplResult<AggregatePriceResult> {
    val request =
        GetAggregatePriceRequest(
            baseAsset = baseAsset,
            quoteAsset = quoteAsset,
            oracles = oracles.map { OracleSpecDto(account = it.account, oracleDocumentId = it.oracleDocumentId) },
            trim = trim,
            trimThreshold = trimThreshold,
        )
    return executeRpc(
        method = "get_aggregate_price",
        request = request,
        requestSerializer = GetAggregatePriceRequest.serializer(),
        responseDeserializer = GetAggregatePriceResponseDto.serializer(),
    ) { dto ->
        AggregatePriceResult(
            entireSet =
                dto.entireSet?.let {
                    AggregatePriceSet(mean = it.mean, size = it.size, standardDeviation = it.standardDeviation)
                },
            trimmedSet =
                dto.trimmedSet?.let {
                    AggregatePriceSet(mean = it.mean, size = it.size, standardDeviation = it.standardDeviation)
                },
            median = dto.median,
            time = dto.time,
            ledgerCurrentIndex = dto.ledgerCurrentIndex,
            validated = dto.validated,
        )
    }
}

/**
 * Retrieves information about a Vault instance.
 *
 * @param vaultId The object ID of the Vault. Either [vaultId] or [owner]+[seq] must be provided.
 * @param owner The Vault Owner account ID (alternative to [vaultId]).
 * @param seq Sequence number of the vault entry (used with [owner]).
 * @param ledgerSpecifier Which ledger version to use. Defaults to [LedgerSpecifier.Validated].
 * @return [XrplResult] containing [VaultInfoResult] or a categorized failure.
 */
public suspend fun XrplClient.vaultInfo(
    vaultId: String? = null,
    owner: String? = null,
    seq: Long? = null,
    ledgerSpecifier: LedgerSpecifier = LedgerSpecifier.Validated,
): XrplResult<VaultInfoResult> {
    val (paramKey, paramValue) = ledgerSpecifier.toParamPair()
    val request =
        VaultInfoRequest(
            vaultId = vaultId,
            owner = owner,
            seq = seq,
            ledgerIndex = if (paramKey == "ledger_index") paramValue else null,
            ledgerHash = if (paramKey == "ledger_hash") paramValue else null,
        )
    return executeRpc(
        method = "vault_info",
        request = request,
        requestSerializer = VaultInfoRequest.serializer(),
        responseDeserializer = VaultInfoResponseDto.serializer(),
    ) { dto ->
        VaultInfoResult(
            vault = dto.vault,
            ledgerHash = dto.ledgerHash,
            ledgerIndex = dto.ledgerIndex,
            validated = dto.validated,
        )
    }
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
