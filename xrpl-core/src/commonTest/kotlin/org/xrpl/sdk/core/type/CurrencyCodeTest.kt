package org.xrpl.sdk.core.type

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class CurrencyCodeTest : FunSpec({

    context("standard 3-character currency codes") {
        test("valid ISO currency code USD is accepted") {
            CurrencyCode("USD").value shouldBe "USD"
        }

        test("valid ISO currency code EUR is accepted") {
            CurrencyCode("EUR").value shouldBe "EUR"
        }

        test("non-alphanumeric but printable characters are accepted") {
            shouldNotThrow<IllegalArgumentException> {
                CurrencyCode("+AB")
            }
        }

        test("space character (0x20) is accepted as printable") {
            shouldNotThrow<IllegalArgumentException> {
                CurrencyCode(" AB")
            }
        }

        test("tilde character (0x7E) is accepted as printable") {
            shouldNotThrow<IllegalArgumentException> {
                CurrencyCode("~AB")
            }
        }

        test("\"XRP\" is rejected as reserved") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CurrencyCode("XRP")
                }
            exception.message shouldContain "reserved"
        }

        test("non-printable character (0x00) should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("\u0000AB")
            }
        }

        test("non-printable character (0x7F DEL) should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("\u007FAB")
            }
        }
    }

    context("non-standard 40-character hex currency codes") {
        test("valid 40-char hex is accepted") {
            val hex40 = "0".repeat(40)
            CurrencyCode(hex40).value shouldBe hex40
        }

        test("mixed case 40-char hex is accepted") {
            val hex40 = "aAbBcCdDeEfF0123456789aAbBcCdDeEfF012345"
            shouldNotThrow<IllegalArgumentException> {
                CurrencyCode(hex40)
            }
        }

        test("40-char string with non-hex characters should be rejected") {
            val invalid = "G" + "0".repeat(39)
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CurrencyCode(invalid)
                }
            exception.message shouldContain "hex"
        }
    }

    context("invalid lengths") {
        test("1-character string should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CurrencyCode("A")
                }
            exception.message shouldContain "3 characters"
        }

        test("2-character string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("AB")
            }
        }

        test("4-character string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("ABCD")
            }
        }

        test("39-character string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("0".repeat(39))
            }
        }

        test("41-character string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("0".repeat(41))
            }
        }

        test("empty string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                CurrencyCode("")
            }
        }
    }
})
