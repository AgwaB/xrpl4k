package org.xrpl.sdk.client.internal

import kotlinx.serialization.json.Json

/**
 * Project-wide [Json] instance for XRPL JSON-RPC serialization.
 *
 * Configuration follows conventions section 11.1:
 * - `ignoreUnknownKeys`: XRPL responses may contain fields not yet modeled.
 * - `isLenient`: Tolerates minor JSON format variations.
 * - `encodeDefaults = false`: Omits default-value fields from requests.
 * - `explicitNulls = false`: Omits null fields from encoded output.
 * - `coerceInputValues`: Maps unexpected enum values to defaults instead of crashing.
 */
internal val XrplJson: Json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
        coerceInputValues = true
    }
