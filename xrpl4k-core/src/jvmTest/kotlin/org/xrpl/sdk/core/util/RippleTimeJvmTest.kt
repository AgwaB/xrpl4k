@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe

/**
 * JVM-specific tests for [RippleTime] utilities, complementing the commonTest suite.
 *
 * Focuses on the [UInt] overload, [currentRippleTime], and round-trip via [UInt].
 */
class RippleTimeJvmTest : FunSpec({

    // ── rippleTimeToUnixTime(UInt) overload ──────────────────────────────────

    context("rippleTimeToUnixTime(UInt)") {
        test("UInt 0 produces same result as Long 0") {
            rippleTimeToUnixTime(0u) shouldBe rippleTimeToUnixTime(0L)
        }

        test("UInt positive value matches Long overload") {
            val rippleTime = 631_152_000u
            rippleTimeToUnixTime(rippleTime) shouldBe rippleTimeToUnixTime(631_152_000L)
        }

        test("UInt max value (year ~2136)") {
            val maxUInt = UInt.MAX_VALUE
            rippleTimeToUnixTime(maxUInt) shouldBe rippleTimeToUnixTime(maxUInt.toLong())
        }

        test("UInt 1 produces expected unix time") {
            rippleTimeToUnixTime(1u) shouldBe 946_684_801_000L
        }
    }

    // ── currentRippleTime() ──────────────────────────────────────────────────

    context("currentRippleTime()") {
        test("returns a value greater than zero") {
            // The Ripple epoch is 2000-01-01, so any time after that is positive.
            currentRippleTime() shouldBeGreaterThan 0u
        }

        test("returns a reasonable value for current year") {
            // 2025-01-01T00:00:00Z = Ripple epoch 788_918_400
            // 2030-01-01T00:00:00Z = Ripple epoch 946_684_800 (wait, that's unix epoch...)
            // Actually: 2025-01-01 = unix 1735689600, ripple = 1735689600 - 946684800 = 788_918_400 (was 2025-01-01)
            // By 2027, ripple time should be > 852_076_800
            val now = currentRippleTime()
            now shouldBeGreaterThan 788_918_400u // after 2025-01-01
            now shouldBeLessThan 1_893_456_000u // before 2060-01-01
        }

        test("two consecutive calls return close values") {
            val a = currentRippleTime()
            val b = currentRippleTime()
            // Should be within 1 second of each other
            val diff = if (b >= a) b - a else a - b
            (diff <= 1u) shouldBe true
        }
    }

    // ── Round-trip via UInt ──────────────────────────────────────────────────

    context("round-trip via UInt") {
        test("rippleTimeToUnixTime -> unixTimeToRippleTime round-trips for UInt") {
            val rippleTime = 631_152_000u
            val unixMs = rippleTimeToUnixTime(rippleTime)
            val roundTripped = unixTimeToRippleTime(unixMs)
            roundTripped shouldBe rippleTime.toLong()
        }

        test("round-trip for UInt.MAX_VALUE") {
            val rippleTime = UInt.MAX_VALUE
            val unixMs = rippleTimeToUnixTime(rippleTime)
            val roundTripped = unixTimeToRippleTime(unixMs)
            roundTripped shouldBe rippleTime.toLong()
        }

        test("round-trip for UInt zero") {
            val unixMs = rippleTimeToUnixTime(0u)
            unixTimeToRippleTime(unixMs) shouldBe 0L
        }
    }
})
