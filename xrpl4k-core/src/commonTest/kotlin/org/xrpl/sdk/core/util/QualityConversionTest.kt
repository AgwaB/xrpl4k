package org.xrpl.sdk.core.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QualityConversionTest : FunSpec({

    context("decimalToTransferRate") {
        test("0 produces 0 (special case: no fee)") {
            decimalToTransferRate("0") shouldBe 0L
        }

        test("0.5 produces 1500000000") {
            decimalToTransferRate("0.5") shouldBe 1_500_000_000L
        }

        test("1 produces 2000000000 (maximum)") {
            decimalToTransferRate("1") shouldBe 2_000_000_000L
        }

        test("0.01 (1% fee)") {
            decimalToTransferRate("0.01") shouldBe 1_010_000_000L
        }

        test("0.000000001 (minimum precision)") {
            decimalToTransferRate("0.000000001") shouldBe 1_000_000_001L
        }

        test("value greater than 1.00 throws") {
            shouldThrow<IllegalArgumentException> {
                decimalToTransferRate("1.01")
            }
        }

        test("negative value throws") {
            shouldThrow<IllegalArgumentException> {
                decimalToTransferRate("-0.1")
            }
        }

        test("empty string throws") {
            shouldThrow<IllegalArgumentException> {
                decimalToTransferRate("")
            }
        }

        test("non-number throws") {
            shouldThrow<IllegalArgumentException> {
                decimalToTransferRate("abc")
            }
        }

        test("precision exceeding 9 decimal places throws") {
            shouldThrow<IllegalArgumentException> {
                decimalToTransferRate("0.0000000001")
            }
        }
    }

    context("transferRateToDecimal") {
        test("0 produces '0' (special case)") {
            transferRateToDecimal(0) shouldBe "0"
        }

        test("1500000000 produces '0.5'") {
            transferRateToDecimal(1_500_000_000L) shouldBe "0.5"
        }

        test("2000000000 produces '1'") {
            transferRateToDecimal(2_000_000_000L) shouldBe "1"
        }

        test("1010000000 produces '0.01'") {
            transferRateToDecimal(1_010_000_000L) shouldBe "0.01"
        }

        test("rate below ONE_BILLION throws") {
            shouldThrow<IllegalArgumentException> {
                transferRateToDecimal(999_999_999L)
            }
        }
    }

    context("percentToTransferRate") {
        test("50% produces 1500000000") {
            percentToTransferRate("50%") shouldBe 1_500_000_000L
        }

        test("0% produces 0") {
            percentToTransferRate("0%") shouldBe 0L
        }

        test("100% produces 2000000000") {
            percentToTransferRate("100%") shouldBe 2_000_000_000L
        }

        test("1% produces 1010000000") {
            percentToTransferRate("1%") shouldBe 1_010_000_000L
        }

        test("missing % throws") {
            shouldThrow<IllegalArgumentException> {
                percentToTransferRate("50")
            }
        }
    }

    context("decimalToQuality") {
        test("1 produces 0 (special case: 1:1 exchange)") {
            decimalToQuality("1") shouldBe 0L
        }

        test("0.5 produces 500000000") {
            decimalToQuality("0.5") shouldBe 500_000_000L
        }

        test("0 produces 0") {
            decimalToQuality("0") shouldBe 0L
        }

        test("0.00034 produces 340000") {
            decimalToQuality("0.00034") shouldBe 340_000L
        }

        test("negative value throws") {
            shouldThrow<IllegalArgumentException> {
                decimalToQuality("-0.5")
            }
        }
    }

    context("qualityToDecimal") {
        test("0 produces '1' (special case: no quality adjustment)") {
            qualityToDecimal(0) shouldBe "1"
        }

        test("500000000 produces '0.5'") {
            qualityToDecimal(500_000_000L) shouldBe "0.5"
        }

        test("1000000000 produces '1'") {
            qualityToDecimal(1_000_000_000L) shouldBe "1"
        }

        test("340000 produces '0.00034'") {
            qualityToDecimal(340_000L) shouldBe "0.00034"
        }

        test("negative quality throws") {
            shouldThrow<IllegalArgumentException> {
                qualityToDecimal(-1)
            }
        }
    }

    context("percentToQuality") {
        test("50% produces 500000000") {
            percentToQuality("50%") shouldBe 500_000_000L
        }

        test("100% produces 0 (special case: 1:1)") {
            percentToQuality("100%") shouldBe 0L
        }

        test("0.034% produces 340000") {
            percentToQuality("0.034%") shouldBe 340_000L
        }
    }

    context("round-trip TransferRate") {
        test("decimal -> transferRate -> decimal") {
            val original = "0.5"
            transferRateToDecimal(decimalToTransferRate(original)) shouldBe original
        }

        test("0.01 round-trip") {
            val original = "0.01"
            transferRateToDecimal(decimalToTransferRate(original)) shouldBe original
        }
    }

    context("round-trip Quality") {
        test("decimal -> quality -> decimal") {
            val original = "0.5"
            qualityToDecimal(decimalToQuality(original)) shouldBe original
        }

        test("special case 0 round-trips (decimalToQuality and qualityToDecimal both treat 0/1 specially)") {
            // decimalToQuality("1") → 0, qualityToDecimal(0) → "1"
            decimalToQuality("1") shouldBe 0L
            qualityToDecimal(0) shouldBe "1"
            qualityToDecimal(decimalToQuality("1")) shouldBe "1"
        }
    }

    context("decimalToQuality — extended") {
        test("quality allows values greater than 1") {
            decimalToQuality("2") shouldBe 2_000_000_000L
        }

        test("quality value 1.5 produces 1500000000") {
            decimalToQuality("1.5") shouldBe 1_500_000_000L
        }

        test("quality 0 and 1 both map to 0") {
            // decimalToQuality("0") → scaled=0, 0 != ONE_BILLION → return 0
            // decimalToQuality("1") → scaled=ONE_BILLION → return 0 (special case)
            decimalToQuality("0") shouldBe 0L
            decimalToQuality("1") shouldBe 0L
        }
    }

    context("qualityToDecimal — extended") {
        test("quality 2_000_000_000 produces '2'") {
            qualityToDecimal(2_000_000_000L) shouldBe "2"
        }

        test("quality 1 produces '0.000000001'") {
            qualityToDecimal(1L) shouldBe "0.000000001"
        }
    }

    context("percentToTransferRate — extended") {
        test("fractional percent 0.5% produces correct rate") {
            percentToTransferRate("0.5%") shouldBe 1_005_000_000L
        }

        test("percent sign in the middle throws") {
            shouldThrow<IllegalArgumentException> {
                percentToTransferRate("50%50%")
            }
        }

        test("empty percent throws") {
            shouldThrow<IllegalArgumentException> {
                percentToTransferRate("%")
            }
        }
    }

    context("whitespace handling") {
        test("leading and trailing whitespace is trimmed") {
            decimalToTransferRate("  0.5  ") shouldBe 1_500_000_000L
        }
    }
})
