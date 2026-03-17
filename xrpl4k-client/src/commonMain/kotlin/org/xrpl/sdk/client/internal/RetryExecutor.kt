package org.xrpl.sdk.client.internal

import kotlinx.coroutines.delay
import org.xrpl.sdk.client.RetryConfig
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Wraps an RPC call with configurable retry logic using exponential backoff + jitter.
 *
 * Retries only on:
 * - [XrplFailure.NetworkError] (transient network issues)
 * - [XrplFailure.RpcError] where [isTransientRpcError] returns true
 *
 * Does NOT retry:
 * - [XrplFailure.ValidationError], [XrplFailure.TecError], [XrplFailure.NotFound]
 * - Non-idempotent methods (caller is responsible for excluding submit/submitMultisigned)
 *
 * Respects [kotlinx.coroutines.CancellationException] (rethrows immediately).
 */
internal suspend fun <T> withRetry(
    config: RetryConfig,
    block: suspend () -> XrplResult<T>,
): XrplResult<T> {
    var lastResult: XrplResult<T>? = null

    repeat(config.maxAttempts) { attempt ->
        val result = block()

        when {
            result is XrplResult.Success -> return result
            result is XrplResult.Failure && shouldRetry(result.error) && attempt < config.maxAttempts - 1 -> {
                lastResult = result
                val delayMs = calculateDelay(attempt, config)
                delay(delayMs)
            }
            else -> return result
        }
    }

    return lastResult ?: block()
}

private fun shouldRetry(error: XrplFailure): Boolean =
    when (error) {
        is XrplFailure.NetworkError -> true
        is XrplFailure.RpcError -> isTransientRpcError(error)
        is XrplFailure.ValidationError -> false
        is XrplFailure.TecError -> false
        is XrplFailure.NotFound -> false
    }

private fun calculateDelay(
    attempt: Int,
    config: RetryConfig,
): Long {
    val baseDelay = config.initialDelay.inWholeMilliseconds * 2.0.pow(attempt).toLong()
    val cappedDelay = min(baseDelay, config.maxDelay.inWholeMilliseconds)
    val jitteredDelay = (cappedDelay * (0.75 + Random.nextDouble() * 0.25)).toLong()
    return jitteredDelay
}
