package org.xrpl.sdk.core.type

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PublicKeyTest : FunSpec({

    val validHex66 = "A".repeat(66)
    val validLower66 = "a".repeat(66)

    context("valid public keys") {
        test("66-char uppercase hex is accepted") {
            val key = PublicKey(validHex66)
            key.value shouldBe validHex66
        }

        test("66-char lowercase hex is normalized to uppercase") {
            val key = PublicKey(validLower66)
            key.value shouldBe validHex66
        }

        test("mixed-case hex is normalized to uppercase") {
            val mixed = "aAbBcCdDeEfF0123456789".repeat(3) // 22 * 3 = 66 chars
            val key = PublicKey(mixed)
            key.value shouldBe mixed.uppercase()
        }

        test("realistic ed25519 public key is accepted") {
            val ed25519Key = "ED" + "A1B2C3D4".repeat(8) // 2 + 8*8 = 66 chars
            val key = PublicKey(ed25519Key)
            key.value shouldBe ed25519Key.uppercase()
        }
    }

    context("invalid length") {
        test("65-char hex should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PublicKey("A".repeat(65))
                }
            exception.message shouldContain "exactly 66"
        }

        test("67-char hex should be rejected") {
            shouldThrow<IllegalArgumentException> {
                PublicKey("A".repeat(67))
            }
        }

        test("64-char hex should be rejected") {
            shouldThrow<IllegalArgumentException> {
                PublicKey("A".repeat(64))
            }
        }

        test("empty string should be rejected") {
            shouldThrow<IllegalArgumentException> {
                PublicKey("")
            }
        }
    }

    context("invalid characters") {
        test("hex with 'G' should be rejected") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    PublicKey("G" + "A".repeat(65))
                }
            exception.message shouldContain "invalid hex"
        }

        test("hex with spaces should be rejected") {
            shouldThrow<IllegalArgumentException> {
                PublicKey(" " + "A".repeat(65))
            }
        }

        test("hex with special characters should be rejected") {
            shouldThrow<IllegalArgumentException> {
                PublicKey("!" + "A".repeat(65))
            }
        }
    }

    context("equality") {
        test("same logical key in different cases should be equal") {
            PublicKey(validHex66) shouldBe PublicKey(validLower66)
        }

        test("different keys should not be equal") {
            (PublicKey("A".repeat(66)) == PublicKey("B".repeat(66))) shouldBe false
        }
    }
})
