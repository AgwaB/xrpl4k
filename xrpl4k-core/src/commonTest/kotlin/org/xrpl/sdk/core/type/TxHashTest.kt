package org.xrpl.sdk.core.type

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class TxHashTest : FunSpec({

    val validHex64 = "A".repeat(64)
    val validLower64 = "a".repeat(64)

    context("valid hashes") {
        test("64-char uppercase hex is accepted") {
            val hash = TxHash(validHex64)
            hash.value shouldBe validHex64
        }

        test("64-char lowercase hex is normalized to uppercase") {
            val hash = TxHash(validLower64)
            hash.value shouldBe validHex64
        }

        test("mixed-case hex is normalized to uppercase") {
            val mixed = "aAbBcCdDeEfF" + "0123456789" + "aAbBcCdDeEfF" + "0123456789" + "aAbBcCdDeEfF01234567"
            val hash = TxHash(mixed)
            hash.value shouldBe mixed.uppercase()
        }
    }

    context("invalid length") {
        test("63-char hex should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    TxHash("A".repeat(63))
                }
            exception.message shouldContain "exactly 64"
        }

        test("65-char hex should be rejected") {
            shouldThrow<IllegalArgumentException> {
                TxHash("A".repeat(65))
            }
        }

        test("empty string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                TxHash("")
            }
        }
    }

    context("invalid characters") {
        test("hex with 'G' should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    TxHash("G" + "A".repeat(63))
                }
            exception.message shouldContain "invalid hex"
        }

        test("hex with spaces should be rejected") {
            shouldThrow<IllegalArgumentException> {
                TxHash(" " + "A".repeat(63))
            }
        }
    }

    context("String.toTxHash extension") {
        test("valid string converts to TxHash") {
            val hash = validLower64.toTxHash()
            hash.value shouldBe validHex64
        }
    }

    context("equality") {
        test("same logical hash in different cases should be equal") {
            TxHash(validHex64) shouldBe TxHash(validLower64)
        }
    }
})
