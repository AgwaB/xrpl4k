package org.xrpl.sdk.core.type

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class Hash256Test : FunSpec({

    val validHex64 = "B".repeat(64)
    val validLower64 = "b".repeat(64)

    context("valid hashes") {
        test("64-char uppercase hex is accepted") {
            val hash = Hash256(validHex64)
            hash.value shouldBe validHex64
        }

        test("64-char lowercase hex is normalized to uppercase") {
            val hash = Hash256(validLower64)
            hash.value shouldBe validHex64
        }
    }

    context("invalid length") {
        test("63-char hex should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    Hash256("B".repeat(63))
                }
            exception.message shouldContain "exactly 64"
        }

        test("65-char hex should be rejected") {
            shouldThrow<IllegalArgumentException> {
                Hash256("B".repeat(65))
            }
        }
    }

    context("invalid characters") {
        test("hex with non-hex character should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    Hash256("Z" + "B".repeat(63))
                }
            exception.message shouldContain "invalid hex"
        }
    }

    context("String.toHash256 extension") {
        test("valid string converts to Hash256") {
            val hash = validLower64.toHash256()
            hash.value shouldBe validHex64
        }
    }

    context("equality") {
        test("same logical hash in different cases should be equal") {
            Hash256(validHex64) shouldBe Hash256(validLower64)
        }
    }
})
