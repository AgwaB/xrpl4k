package org.xrpl.sdk.core.model.amount

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LiteralsTest : FunSpec({

    context("Int.xrp") {
        test("10.xrp produces XrpAmount with 10_000_000 drops") {
            val amount = 10.xrp
            amount.drops.value shouldBe 10_000_000L
        }

        test("0.xrp produces XrpAmount with 0 drops") {
            val amount = 0.xrp
            amount.drops.value shouldBe 0L
        }

        test("100_000_000.xrp works correctly") {
            val amount = 100_000_000.xrp
            amount.drops.value shouldBe 100_000_000_000_000L
        }

        test("1.xrp produces XrpAmount with 1_000_000 drops") {
            val amount = 1.xrp
            amount.drops.value shouldBe 1_000_000L
        }
    }

    context("Long.drops") {
        test("1L.drops produces XrpAmount with 1 drop") {
            val amount = 1L.drops
            amount.drops.value shouldBe 1L
        }

        test("0L.drops produces XrpAmount with 0 drops") {
            val amount = 0L.drops
            amount.drops.value shouldBe 0L
        }

        test("1_000_000L.drops produces XrpAmount with 1_000_000 drops") {
            val amount = 1_000_000L.drops
            amount.drops.value shouldBe 1_000_000L
        }
    }

    context("arithmetic on literals") {
        test("10.xrp + 5.xrp == 15.xrp") {
            val result = 10.xrp + 5.xrp
            result shouldBe 15.xrp
        }

        test("10.xrp - 3.xrp == 7.xrp") {
            val result = 10.xrp - 3.xrp
            result shouldBe 7.xrp
        }

        test("0L.drops + 1L.drops == 1L.drops") {
            val result = 0L.drops + 1L.drops
            result shouldBe 1L.drops
        }
    }
})
