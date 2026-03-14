@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConstantTimeUtilsTest : FunSpec({

    test("same arrays return true") {
        val a = byteArrayOf(1, 2, 3, 4, 5)
        val b = byteArrayOf(1, 2, 3, 4, 5)
        constantTimeEquals(a, b) shouldBe true
    }

    test("different arrays return false") {
        val a = byteArrayOf(1, 2, 3, 4, 5)
        val b = byteArrayOf(1, 2, 3, 4, 6)
        constantTimeEquals(a, b) shouldBe false
    }

    test("empty arrays return true") {
        constantTimeEquals(byteArrayOf(), byteArrayOf()) shouldBe true
    }

    test("different length arrays return false") {
        val a = byteArrayOf(1, 2, 3)
        val b = byteArrayOf(1, 2, 3, 4)
        constantTimeEquals(a, b) shouldBe false
    }
})
