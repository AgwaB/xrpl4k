package org.xrpl.sdk.core.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant

class RippleTimeTest : FunSpec({

    context("RIPPLE_EPOCH_DIFF") {
        test("equals 946684800 (0x386D4380)") {
            RIPPLE_EPOCH_DIFF shouldBe 946_684_800L
            RIPPLE_EPOCH_DIFF shouldBe 0x386D4380L
        }
    }

    context("rippleTimeToUnixTime") {
        test("ripple epoch 0 is 2000-01-01 in unix ms") {
            rippleTimeToUnixTime(0) shouldBe 946_684_800_000L
        }

        test("positive ripple time") {
            // 2020-01-01T00:00:00Z = Unix 1577836800s = Ripple 631152000s
            rippleTimeToUnixTime(631_152_000L) shouldBe 1_577_836_800_000L
        }

        test("negative ripple time (before year 2000)") {
            // -1 second = 1999-12-31T23:59:59Z
            rippleTimeToUnixTime(-1) shouldBe 946_684_799_000L
        }
    }

    context("unixTimeToRippleTime") {
        test("2000-01-01 unix ms produces ripple time 0") {
            unixTimeToRippleTime(946_684_800_000L) shouldBe 0L
        }

        test("2020-01-01 unix ms") {
            unixTimeToRippleTime(1_577_836_800_000L) shouldBe 631_152_000L
        }

        test("before ripple epoch produces negative") {
            unixTimeToRippleTime(946_684_799_000L) shouldBe -1L
        }
    }

    context("round-trip rippleTime <-> unixTime") {
        test("positive value round-trips") {
            val rippleTime = 631_152_000L
            unixTimeToRippleTime(rippleTimeToUnixTime(rippleTime)) shouldBe rippleTime
        }

        test("zero round-trips") {
            unixTimeToRippleTime(rippleTimeToUnixTime(0)) shouldBe 0L
        }

        test("negative value round-trips") {
            val rippleTime = -86400L
            unixTimeToRippleTime(rippleTimeToUnixTime(rippleTime)) shouldBe rippleTime
        }
    }

    context("rippleTimeToInstant / instantToRippleTime") {
        test("ripple time 0 is 2000-01-01T00:00:00Z") {
            val instant = rippleTimeToInstant(0)
            instant shouldBe Instant.parse("2000-01-01T00:00:00Z")
        }

        test("instant to ripple time") {
            val instant = Instant.parse("2020-01-01T00:00:00Z")
            instantToRippleTime(instant) shouldBe 631_152_000L
        }

        test("round-trip via Instant") {
            val rippleTime = 631_152_000L
            instantToRippleTime(rippleTimeToInstant(rippleTime)) shouldBe rippleTime
        }
    }

    context("rippleTimeToISOTime") {
        test("ripple time 0 produces 2000-01-01T00:00:00Z") {
            rippleTimeToISOTime(0) shouldBe "2000-01-01T00:00:00Z"
        }

        test("positive ripple time") {
            rippleTimeToISOTime(631_152_000L) shouldBe "2020-01-01T00:00:00Z"
        }
    }

    context("isoTimeToRippleTime") {
        test("2000-01-01T00:00:00Z produces 0") {
            isoTimeToRippleTime("2000-01-01T00:00:00Z") shouldBe 0L
        }

        test("2020-01-01T00:00:00Z") {
            isoTimeToRippleTime("2020-01-01T00:00:00Z") shouldBe 631_152_000L
        }

        test("invalid ISO string throws") {
            shouldThrow<Exception> {
                isoTimeToRippleTime("not-a-date")
            }
        }
    }

    context("round-trip ISO") {
        test("rippleTime -> ISO -> rippleTime identity") {
            val rippleTime = 631_152_000L
            isoTimeToRippleTime(rippleTimeToISOTime(rippleTime)) shouldBe rippleTime
        }
    }

    context("edge cases") {
        test("unixTimeToRippleTime truncates sub-second milliseconds") {
            // 946684800500ms = ripple epoch + 500ms → truncated to 0
            unixTimeToRippleTime(946_684_800_500L) shouldBe 0L
        }

        test("large ripple time (year ~2136)") {
            // UInt32 max = 4294967295 → 2136-02-07T06:28:15Z
            val maxRippleTime = 4_294_967_295L
            val iso = rippleTimeToISOTime(maxRippleTime)
            iso.startsWith("2136") shouldBe true
            isoTimeToRippleTime(iso) shouldBe maxRippleTime
        }

        test("isoTimeToRippleTime with fractional seconds in ISO string") {
            // kotlinx.datetime Instant.parse supports fractional seconds
            // 2020-01-01T00:00:00.500Z → epochSeconds = 1577836800
            isoTimeToRippleTime("2020-01-01T00:00:00.500Z") shouldBe 631_152_000L
        }

        test("isoTimeToRippleTime with empty string throws") {
            shouldThrow<Exception> {
                isoTimeToRippleTime("")
            }
        }

        test("rippleTimeToUnixTime and back for boundary: 1 second") {
            rippleTimeToUnixTime(1) shouldBe 946_684_801_000L
            unixTimeToRippleTime(946_684_801_000L) shouldBe 1L
        }
    }
})
