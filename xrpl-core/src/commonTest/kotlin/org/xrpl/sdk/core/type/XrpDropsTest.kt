package org.xrpl.sdk.core.type

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class XrpDropsTest : FunSpec({

    context("construction") {
        test("zero drops is valid") {
            XrpDrops(0L).value shouldBe 0L
        }

        test("positive drops is valid") {
            XrpDrops(1_000_000L).value shouldBe 1_000_000L
        }

        test("negative drops should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    XrpDrops(-1L)
                }
            exception.message shouldContain "non-negative"
        }
    }

    context("arithmetic") {
        test("plus adds drops correctly") {
            val result = XrpDrops(100L) + XrpDrops(200L)
            result.value shouldBe 300L
        }

        test("minus subtracts drops correctly") {
            val result = XrpDrops(300L) - XrpDrops(100L)
            result.value shouldBe 200L
        }

        test("minus resulting in negative should throw") {
            shouldThrow<IllegalArgumentException> {
                XrpDrops(100L) - XrpDrops(200L)
            }
        }

        test("minus resulting in zero is valid") {
            val result = XrpDrops(100L) - XrpDrops(100L)
            result.value shouldBe 0L
        }
    }

    context("compareTo") {
        test("greater value compares correctly") {
            XrpDrops(200L) shouldBeGreaterThan XrpDrops(100L)
        }

        test("lesser value compares correctly") {
            XrpDrops(100L) shouldBeLessThan XrpDrops(200L)
        }

        test("equal values compare as equal") {
            (XrpDrops(100L).compareTo(XrpDrops(100L))) shouldBe 0
        }
    }

    context("toXrp") {
        test("0 drops -> \"0\"") {
            XrpDrops(0L).toXrp() shouldBe "0"
        }

        test("1 drop -> \"0.000001\"") {
            XrpDrops(1L).toXrp() shouldBe "0.000001"
        }

        test("1_000_000 drops -> \"1\"") {
            XrpDrops(1_000_000L).toXrp() shouldBe "1"
        }

        test("1_500_000 drops -> \"1.5\"") {
            XrpDrops(1_500_000L).toXrp() shouldBe "1.5"
        }

        test("10_000_000 drops -> \"10\"") {
            XrpDrops(10_000_000L).toXrp() shouldBe "10"
        }

        test("123_456_789 drops -> \"123.456789\"") {
            XrpDrops(123_456_789L).toXrp() shouldBe "123.456789"
        }
    }

    context("MAX_DROPS") {
        test("MAX_DROPS equals 10^17 (100 billion XRP)") {
            XrpDrops.MAX_DROPS shouldBe 100_000_000_000_000_000L
        }

        test("MAX_DROPS value can be constructed") {
            XrpDrops(XrpDrops.MAX_DROPS).value shouldBe XrpDrops.MAX_DROPS
        }
    }
})
