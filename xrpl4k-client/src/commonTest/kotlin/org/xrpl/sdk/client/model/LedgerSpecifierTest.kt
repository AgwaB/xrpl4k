package org.xrpl.sdk.client.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.xrpl.sdk.core.type.Hash256

class LedgerSpecifierTest : FunSpec({

    context("Validated") {
        test("is a LedgerSpecifier") {
            LedgerSpecifier.Validated.shouldBeInstanceOf<LedgerSpecifier>()
        }

        test("equals itself") {
            LedgerSpecifier.Validated shouldBe LedgerSpecifier.Validated
        }

        test("toParamPair returns ledger_index validated") {
            LedgerSpecifier.Validated.toParamPair() shouldBe ("ledger_index" to "validated")
        }
    }

    context("Current") {
        test("is a LedgerSpecifier") {
            LedgerSpecifier.Current.shouldBeInstanceOf<LedgerSpecifier>()
        }

        test("equals itself") {
            LedgerSpecifier.Current shouldBe LedgerSpecifier.Current
        }

        test("toParamPair returns ledger_index current") {
            LedgerSpecifier.Current.toParamPair() shouldBe ("ledger_index" to "current")
        }
    }

    context("Closed") {
        test("is a LedgerSpecifier") {
            LedgerSpecifier.Closed.shouldBeInstanceOf<LedgerSpecifier>()
        }

        test("equals itself") {
            LedgerSpecifier.Closed shouldBe LedgerSpecifier.Closed
        }

        test("toParamPair returns ledger_index closed") {
            LedgerSpecifier.Closed.toParamPair() shouldBe ("ledger_index" to "closed")
        }
    }

    context("Index") {
        test("stores the ledger index") {
            val specifier = LedgerSpecifier.Index(85_000_000u)
            specifier.index shouldBe 85_000_000u
        }

        test("equal indices are equal") {
            LedgerSpecifier.Index(42u) shouldBe LedgerSpecifier.Index(42u)
        }

        test("different indices are not equal") {
            LedgerSpecifier.Index(1u) shouldNotBe LedgerSpecifier.Index(2u)
        }

        test("equal indices have equal hashCodes") {
            LedgerSpecifier.Index(42u).hashCode() shouldBe LedgerSpecifier.Index(42u).hashCode()
        }

        test("toString includes index value") {
            LedgerSpecifier.Index(100u).toString() shouldBe "LedgerSpecifier.Index(100)"
        }

        test("toParamPair returns ledger_index with number") {
            LedgerSpecifier.Index(85_000_000u).toParamPair() shouldBe ("ledger_index" to "85000000")
        }
    }

    context("Hash") {
        val validHash = Hash256("A".repeat(64))

        test("stores the ledger hash") {
            val specifier = LedgerSpecifier.Hash(validHash)
            specifier.hash shouldBe validHash
        }

        test("equal hashes are equal") {
            LedgerSpecifier.Hash(validHash) shouldBe LedgerSpecifier.Hash(validHash)
        }

        test("different hashes are not equal") {
            val otherHash = Hash256("B".repeat(64))
            LedgerSpecifier.Hash(validHash) shouldNotBe LedgerSpecifier.Hash(otherHash)
        }

        test("equal hashes have equal hashCodes") {
            LedgerSpecifier.Hash(validHash).hashCode() shouldBe LedgerSpecifier.Hash(validHash).hashCode()
        }

        test("toParamPair returns ledger_hash with hash value") {
            LedgerSpecifier.Hash(validHash).toParamPair() shouldBe ("ledger_hash" to validHash.value)
        }
    }

    context("different variants are not equal") {
        test("Validated is not equal to Current") {
            (LedgerSpecifier.Validated == LedgerSpecifier.Current) shouldBe false
        }

        test("Index is not equal to Hash") {
            val index = LedgerSpecifier.Index(1u)
            val hash = LedgerSpecifier.Hash(Hash256("A".repeat(64)))
            (index == hash) shouldBe false
        }
    }
})
