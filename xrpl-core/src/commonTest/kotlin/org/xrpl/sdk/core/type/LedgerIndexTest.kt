package org.xrpl.sdk.core.type

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LedgerIndexTest : FunSpec({

    test("zero ledger index is valid") {
        LedgerIndex(0u).value shouldBe 0u
    }

    test("positive ledger index is valid") {
        LedgerIndex(85_000_000u).value shouldBe 85_000_000u
    }

    test("max UInt ledger index is valid") {
        LedgerIndex(UInt.MAX_VALUE).value shouldBe UInt.MAX_VALUE
    }

    test("two LedgerIndex with same value are equal") {
        LedgerIndex(42u) shouldBe LedgerIndex(42u)
    }

    test("two LedgerIndex with different values are not equal") {
        (LedgerIndex(1u) == LedgerIndex(2u)) shouldBe false
    }
})
