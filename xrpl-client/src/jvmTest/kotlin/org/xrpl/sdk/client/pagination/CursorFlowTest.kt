@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.pagination

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.xrpl.sdk.core.result.XrplException
import org.xrpl.sdk.core.result.XrplFailure
import org.xrpl.sdk.core.result.XrplResult

class CursorFlowTest : FunSpec({

    test("single page with null marker emits all items then completes") {
        runTest {
            val flow =
                cursorFlow { _ ->
                    Pair(listOf("a", "b", "c"), null)
                }
            val items = flow.toList()
            items shouldBe listOf("a", "b", "c")
        }
    }

    test("multi-page pagination emits all items across pages") {
        runTest {
            val pages =
                listOf(
                    listOf(1, 2) to JsonNull,
                    listOf(3, 4) to null,
                )
            var pageIndex = 0
            val flow =
                cursorFlow { _ ->
                    val (items, marker) = pages[pageIndex++]
                    Pair(items, marker)
                }
            val items = flow.toList()
            items shouldBe listOf(1, 2, 3, 4)
        }
    }

    test("empty page with null marker completes immediately") {
        runTest {
            val flow =
                cursorFlow<String> { _ ->
                    Pair(emptyList(), null)
                }
            val items = flow.toList()
            items shouldBe emptyList()
        }
    }

    test("error in fetch propagates through Flow as XrplException") {
        runTest {
            val flow =
                cursorFlow<String> { _ ->
                    XrplResult.Failure(XrplFailure.NetworkError("fetch failed")).getOrThrowForFlow()
                    Pair(emptyList(), null)
                }
            var caughtException: Throwable? = null
            try {
                flow.toList()
            } catch (e: XrplException) {
                caughtException = e
            }
            caughtException.shouldBeInstanceOf<XrplException>()
        }
    }

    test("flow cancellation via take stops fetching early") {
        runTest {
            var fetchCount = 0
            val flow =
                cursorFlow { marker ->
                    fetchCount++
                    // Each page has 2 items and always returns a marker to continue
                    Pair(listOf(fetchCount * 10, fetchCount * 10 + 1), JsonNull)
                }
            val items = flow.take(3).toList()
            items.size shouldBe 3
            // Should not have fetched all infinite pages
            fetchCount shouldBe 2
        }
    }

    test("getOrThrowForFlow returns value on success") {
        val result: XrplResult<String> = XrplResult.Success("hello")
        result.getOrThrowForFlow() shouldBe "hello"
    }

    test("getOrThrowForFlow throws XrplException on failure") {
        val result: XrplResult<String> = XrplResult.Failure(XrplFailure.NotFound)
        var threw = false
        try {
            result.getOrThrowForFlow()
        } catch (e: XrplException) {
            threw = true
            e.failure shouldBe XrplFailure.NotFound
        }
        threw shouldBe true
    }
})
