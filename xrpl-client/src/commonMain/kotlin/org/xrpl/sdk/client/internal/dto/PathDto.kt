package org.xrpl.sdk.client.internal.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// PathFind (path_find)

@Serializable
internal data class PathFindRequest(
    @SerialName("source_account") val sourceAccount: String,
    @SerialName("destination_account") val destinationAccount: String,
    @SerialName("destination_amount") val destinationAmount: JsonElement,
)

@Serializable
internal data class PathFindResponseDto(
    val alternatives: List<PathAlternativeDto> = emptyList(),
    @SerialName("destination_account") val destinationAccount: String? = null,
    @SerialName("destination_amount") val destinationAmount: JsonElement? = null,
    @SerialName("source_account") val sourceAccount: String? = null,
)

// RipplePathFind (ripple_path_find)

@Serializable
internal data class RipplePathFindRequest(
    @SerialName("source_account") val sourceAccount: String,
    @SerialName("destination_account") val destinationAccount: String,
    @SerialName("destination_amount") val destinationAmount: JsonElement,
    @SerialName("source_currencies") val sourceCurrencies: List<JsonElement>? = null,
)

@Serializable
internal data class RipplePathFindResponseDto(
    val alternatives: List<PathAlternativeDto> = emptyList(),
    @SerialName("destination_account") val destinationAccount: String? = null,
    @SerialName("destination_amount") val destinationAmount: JsonElement? = null,
    @SerialName("source_account") val sourceAccount: String? = null,
)

@Serializable
internal data class PathAlternativeDto(
    @SerialName("paths_computed") val pathsComputed: List<List<JsonElement>> = emptyList(),
    @SerialName("source_amount") val sourceAmount: JsonElement? = null,
    @SerialName("destination_amount") val destinationAmount: JsonElement? = null,
)
