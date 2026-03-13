package org.xrpl.sdk.client.pagination

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement
import org.xrpl.sdk.core.result.XrplException
import org.xrpl.sdk.core.result.XrplResult

/**
 * Creates a [Flow] that automatically pages through marker-based XRPL results.
 *
 * Each call to [fetch] returns a pair of (items, nextMarker). The flow emits all items
 * from each page, then fetches the next page using the returned marker. When the marker
 * is `null`, pagination is complete.
 *
 * Supports cooperative cancellation via `take(N)` — remaining pages are not fetched.
 *
 * Errors in any page propagate through the Flow's error channel. Collectors can use
 * `.catch {}` to handle them.
 *
 * @param fetch a suspend function that takes an optional marker and returns items + next marker.
 */
internal fun <T> cursorFlow(fetch: suspend (marker: JsonElement?) -> Pair<List<T>, JsonElement?>): Flow<T> =
    flow {
        var marker: JsonElement? = null
        do {
            val (items, nextMarker) = fetch(marker)
            for (item in items) {
                emit(item)
            }
            marker = nextMarker
        } while (marker != null)
    }

/**
 * Helper to unwrap an [XrplResult] for use inside [cursorFlow].
 * Throws [XrplException] on failure so the Flow terminates with an error.
 */
internal fun <T> XrplResult<T>.getOrThrowForFlow(): T =
    when (this) {
        is XrplResult.Success -> value
        is XrplResult.Failure -> throw XrplException(error)
    }
