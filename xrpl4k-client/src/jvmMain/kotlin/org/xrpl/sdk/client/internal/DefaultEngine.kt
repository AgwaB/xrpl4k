package org.xrpl.sdk.client.internal

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

internal actual fun defaultHttpClientEngine(): HttpClientEngine = CIO.create()
