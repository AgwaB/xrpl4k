package org.xrpl.sdk.client.internal

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

internal actual fun defaultHttpClientEngine(): HttpClientEngine = Js.create()
