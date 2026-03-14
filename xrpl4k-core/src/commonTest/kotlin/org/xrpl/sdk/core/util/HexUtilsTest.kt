package org.xrpl.sdk.core.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HexUtilsTest : FunSpec({

    context("ByteArray.toHexString") {
        test("empty byte array produces empty string") {
            byteArrayOf().toHexString() shouldBe ""
        }

        test("single byte produces two hex characters") {
            byteArrayOf(0x0A).toHexString() shouldBe "0a"
        }

        test("multiple bytes produce lowercase hex") {
            byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
                .toHexString() shouldBe "deadbeef"
        }

        test("zero byte produces 00") {
            byteArrayOf(0x00).toHexString() shouldBe "00"
        }

        test("0xFF produces ff") {
            byteArrayOf(0xFF.toByte()).toHexString() shouldBe "ff"
        }
    }

    context("String.hexToByteArray") {
        test("empty string produces empty byte array") {
            "".hexToByteArray() shouldBe byteArrayOf()
        }

        test("valid lowercase hex decodes correctly") {
            "deadbeef".hexToByteArray() shouldBe
                byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
        }

        test("valid uppercase hex decodes correctly") {
            "DEADBEEF".hexToByteArray() shouldBe
                byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
        }

        test("mixed case hex decodes correctly") {
            "DeAdBeEf".hexToByteArray() shouldBe
                byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
        }

        test("odd-length hex string should throw") {
            shouldThrow<IllegalArgumentException> {
                "abc".hexToByteArray()
            }
        }

        test("invalid hex character should throw") {
            shouldThrow<IllegalArgumentException> {
                "zz".hexToByteArray()
            }
        }
    }

    context("String.isHex") {
        test("valid lowercase hex returns true") {
            "0123456789abcdef".isHex() shouldBe true
        }

        test("valid uppercase hex returns true") {
            "0123456789ABCDEF".isHex() shouldBe true
        }

        test("empty string returns true") {
            "".isHex() shouldBe true
        }

        test("string with non-hex character returns false") {
            "0g".isHex() shouldBe false
        }

        test("string with spaces returns false") {
            "ab cd".isHex() shouldBe false
        }
    }

    context("round-trip") {
        test("encode then decode is identity") {
            val original =
                byteArrayOf(
                    0x01,
                    0x23,
                    0x45,
                    0x67,
                    0x89.toByte(),
                    0xAB.toByte(),
                    0xCD.toByte(),
                    0xEF.toByte(),
                )
            original.toHexString().hexToByteArray() shouldBe original
        }
    }
})
