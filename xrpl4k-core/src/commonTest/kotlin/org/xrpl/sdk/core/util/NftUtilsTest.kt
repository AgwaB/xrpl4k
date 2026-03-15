@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.AccountId

/**
 * Tests for [parseNFTokenID], using known test vectors from xrpl.js.
 *
 * Reference: xrpl.js/packages/xrpl/test/utils/parseNFTokenID.test.ts
 */
class NftUtilsTest : FunSpec({

    context("parseNFTokenID") {
        test("decode a valid NFTokenID (xrpl.js vector 1)") {
            // 000B 0539 C35B55AA096BA6D87A6E6C965A6534150DC56E5E 12C5D09E 0000000C
            // Flags=11, TransferFee=1337, Issuer=rJoxBSzpXhPtAuqFmqxQtGKjA13jUJWthE, Taxon=1337, Seq=12
            val result =
                parseNFTokenID(
                    "000B0539C35B55AA096BA6D87A6E6C965A6534150DC56E5E12C5D09E0000000C",
                )
            result.flags shouldBe 11u
            result.transferFee shouldBe 1337u
            result.issuer shouldBe AccountId("C35B55AA096BA6D87A6E6C965A6534150DC56E5E")
            result.taxon shouldBe 1337u
            result.sequence shouldBe 12u
        }

        test("decode a valid NFTokenID with big taxon (xrpl.js vector 2)") {
            // Flags=0, TransferFee=0, Issuer=r9ewzMXVRAD9CjZQ6LTQ4P21vUUucDuqd4, Taxon=2147483649, Seq=30
            val result =
                parseNFTokenID(
                    "000000005EC8BC31F0415E5DD4A8AAAC3718249F8F27323C2EEE87B80000001E",
                )
            result.flags shouldBe 0u
            result.transferFee shouldBe 0u
            result.issuer shouldBe AccountId("5EC8BC31F0415E5DD4A8AAAC3718249F8F27323C")
            result.taxon shouldBe 2147483649u
            result.sequence shouldBe 30u
        }

        test("decode a valid NFTokenID with big sequence (xrpl.js vector 3)") {
            // Flags=8, TransferFee=5000, Issuer=rJ4urHeGPr69TsC9TY9u8N965AdD7S3XEY, Taxon=96, Seq=81343403
            val result =
                parseNFTokenID(
                    "00081388BE9E48FA0E6C95A3E970EB9503E3D3967E8DF95041FED82604D933AB",
                )
            result.flags shouldBe 8u
            result.transferFee shouldBe 5000u
            result.issuer shouldBe AccountId("BE9E48FA0E6C95A3E970EB9503E3D3967E8DF950")
            result.taxon shouldBe 96u
            result.sequence shouldBe 81343403u
        }

        test("preserves original NFTokenID string") {
            val id = "000B0539C35B55AA096BA6D87A6E6C965A6534150DC56E5E12C5D09E0000000C"
            val result = parseNFTokenID(id)
            result.nfTokenId shouldBe id
        }

        test("handles sequence 0 correctly (no unscramble effect)") {
            // With sequence=0, scramble = (384160001*0 + 2459) mod 2^32 = 2459
            // So taxon = storedTaxon XOR 2459
            // storedTaxon = 0x00000000 -> taxon = 2459
            val id = "0000000000000000000000000000000000000000000000000000000000000000"
            val result = parseNFTokenID(id)
            result.flags shouldBe 0u
            result.transferFee shouldBe 0u
            result.sequence shouldBe 0u
            result.taxon shouldBe 2459u // 0 XOR 2459 = 2459
        }

        test("fails for too-short NFTokenID") {
            shouldThrow<IllegalArgumentException> {
                parseNFTokenID("ABCD")
            }
        }

        test("fails for too-long NFTokenID") {
            shouldThrow<IllegalArgumentException> {
                parseNFTokenID("A".repeat(65))
            }
        }

        test("fails for non-hex characters") {
            shouldThrow<IllegalArgumentException> {
                parseNFTokenID("G".repeat(64))
            }
        }
    }
})
