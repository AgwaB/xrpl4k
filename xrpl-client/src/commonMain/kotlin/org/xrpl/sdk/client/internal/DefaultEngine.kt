package org.xrpl.sdk.client.internal

import io.ktor.client.engine.HttpClientEngine

/**
 * Returns the default [HttpClientEngine] for the current platform.
 *
 * - JVM: CIO
 * - Apple (iOS/macOS): Darwin
 * - Linux: CIO
 * - JS: Js
 *
 * Callers can override by passing an explicit engine (e.g., `MockEngine` for tests).
 */
internal expect fun defaultHttpClientEngine(): HttpClientEngine
