package org.xrpl.sdk.codec.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DecimalStringParserTest : FunSpec({

    test("parse zero") {
        val result = DecimalStringParser.parse("0")
        result.isZero shouldBe true
        result.isNegative shouldBe false
    }

    test("parse negative zero") {
        val result = DecimalStringParser.parse("-0")
        result.isZero shouldBe true
        result.isNegative shouldBe false // negative zero normalizes to positive zero
    }

    test("parse 0.0") {
        val result = DecimalStringParser.parse("0.0")
        result.isZero shouldBe true
    }

    test("parse simple integer 1") {
        val result = DecimalStringParser.parse("1")
        result.isZero shouldBe false
        result.isNegative shouldBe false
        result.mantissa shouldBe 1_000_000_000_000_000L
        result.exponent shouldBe -15
    }

    test("parse 1.5") {
        val result = DecimalStringParser.parse("1.5")
        result.isZero shouldBe false
        result.isNegative shouldBe false
        result.mantissa shouldBe 1_500_000_000_000_000L
        result.exponent shouldBe -15
    }

    test("parse 0.00123") {
        val result = DecimalStringParser.parse("0.00123")
        result.isZero shouldBe false
        result.mantissa shouldBe 1_230_000_000_000_000L
        result.exponent shouldBe -18
    }

    test("parse negative value -1.23e-5") {
        val result = DecimalStringParser.parse("-1.23e-5")
        result.isZero shouldBe false
        result.isNegative shouldBe true
        result.mantissa shouldBe 1_230_000_000_000_000L
        result.exponent shouldBe -20
    }

    test("parse scientific notation 1.23e5") {
        val result = DecimalStringParser.parse("1.23e5")
        result.isZero shouldBe false
        result.mantissa shouldBe 1_230_000_000_000_000L
        result.exponent shouldBe -10
    }

    test("parse maximum mantissa 9999999999999999") {
        val result = DecimalStringParser.parse("9999999999999999")
        result.mantissa shouldBe 9_999_999_999_999_999L
        result.exponent shouldBe 0
    }

    test("parse minimum mantissa 1000000000000000") {
        val result = DecimalStringParser.parse("1000000000000000")
        result.mantissa shouldBe 1_000_000_000_000_000L
        result.exponent shouldBe 0
    }

    test("parse max exponent 9999999999999999e80") {
        val result = DecimalStringParser.parse("9999999999999999e80")
        result.mantissa shouldBe 9_999_999_999_999_999L
        result.exponent shouldBe 80
    }

    test("parse min exponent 1000000000000000e-96") {
        val result = DecimalStringParser.parse("1000000000000000e-96")
        result.mantissa shouldBe 1_000_000_000_000_000L
        result.exponent shouldBe -96
    }

    test("parse uppercase E notation 1.5E3") {
        val result = DecimalStringParser.parse("1.5E3")
        result.mantissa shouldBe 1_500_000_000_000_000L
        result.exponent shouldBe -12
    }

    test("parse 100 normalizes correctly") {
        val result = DecimalStringParser.parse("100")
        result.mantissa shouldBe 1_000_000_000_000_000L
        result.exponent shouldBe -13
    }

    test("reject more than 16 significant digits") {
        shouldThrow<IllegalArgumentException> {
            DecimalStringParser.parse("12345678901234567")
        }
    }

    test("reject blank string") {
        shouldThrow<IllegalArgumentException> {
            DecimalStringParser.parse("")
        }
    }

    test("parse with leading plus sign") {
        val result = DecimalStringParser.parse("+1.5")
        result.isNegative shouldBe false
        result.mantissa shouldBe 1_500_000_000_000_000L
    }

    test("exponent out of range rejects") {
        shouldThrow<IllegalArgumentException> {
            DecimalStringParser.parse("1e200")
        }
    }
})
