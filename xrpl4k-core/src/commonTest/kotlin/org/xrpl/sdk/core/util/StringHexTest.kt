package org.xrpl.sdk.core.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringHexTest : FunSpec({

    context("convertStringToHex") {
        test("ASCII string") {
            convertStringToHex("hello") shouldBe "68656C6C6F"
        }

        test("empty string") {
            convertStringToHex("") shouldBe ""
        }

        test("single character") {
            convertStringToHex("A") shouldBe "41"
        }

        test("Korean characters (multi-byte UTF-8)") {
            // '한' = U+D55C = UTF-8 bytes: ED 95 9C
            convertStringToHex("한") shouldBe "ED959C"
        }

        test("emoji (4-byte UTF-8)") {
            // '😀' = U+1F600 = UTF-8 bytes: F0 9F 98 80
            convertStringToHex("😀") shouldBe "F09F9880"
        }

        test("mixed ASCII and Unicode") {
            convertStringToHex("hi한") shouldBe "6869ED959C"
        }

        test("typical Memo content") {
            convertStringToHex("test memo") shouldBe "74657374206D656D6F"
        }
    }

    context("convertHexToString") {
        test("ASCII hex") {
            convertHexToString("68656C6C6F") shouldBe "hello"
        }

        test("empty hex") {
            convertHexToString("") shouldBe ""
        }

        test("lowercase hex") {
            convertHexToString("68656c6c6f") shouldBe "hello"
        }

        test("Korean hex") {
            convertHexToString("ED959C") shouldBe "한"
        }

        test("emoji hex") {
            convertHexToString("F09F9880") shouldBe "😀"
        }

        test("odd-length hex throws") {
            shouldThrow<IllegalArgumentException> {
                convertHexToString("abc")
            }
        }

        test("invalid hex character throws") {
            shouldThrow<IllegalArgumentException> {
                convertHexToString("zzzz")
            }
        }
    }

    context("round-trip") {
        test("ASCII round-trip") {
            val original = "hello world"
            convertHexToString(convertStringToHex(original)) shouldBe original
        }

        test("Unicode round-trip") {
            val original = "한글 테스트 😀🎉"
            convertHexToString(convertStringToHex(original)) shouldBe original
        }

        test("empty round-trip") {
            convertHexToString(convertStringToHex("")) shouldBe ""
        }
    }
})
