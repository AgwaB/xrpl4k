package org.xrpl.sdk.core.model.transaction

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class MemoTest : FunSpec({

    context("Memo construction with all fields") {
        test("constructs with all three fields set") {
            val memo =
                Memo(
                    memoData = "AABB",
                    memoType = "746578742F706C61696E",
                    memoFormat = "CCDD",
                )
            memo.memoData shouldBe "AABB"
            memo.memoType shouldBe "746578742F706C61696E"
            memo.memoFormat shouldBe "CCDD"
        }
    }

    context("Memo construction with partial fields") {
        test("constructs with only memoData") {
            val memo = Memo(memoData = "AABB")
            memo.memoData shouldBe "AABB"
            memo.memoType shouldBe null
            memo.memoFormat shouldBe null
        }

        test("constructs with only memoType") {
            val memo = Memo(memoType = "746578742F706C61696E")
            memo.memoData shouldBe null
            memo.memoType shouldBe "746578742F706C61696E"
            memo.memoFormat shouldBe null
        }

        test("constructs with only memoFormat") {
            val memo = Memo(memoFormat = "CCDD")
            memo.memoData shouldBe null
            memo.memoType shouldBe null
            memo.memoFormat shouldBe "CCDD"
        }

        test("constructs with memoData and memoType only") {
            val memo = Memo(memoData = "AABB", memoType = "746578742F706C61696E")
            memo.memoData shouldBe "AABB"
            memo.memoType shouldBe "746578742F706C61696E"
            memo.memoFormat shouldBe null
        }
    }

    context("Memo with all null fields is rejected") {
        test("throws when all fields are null") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    Memo()
                }
            exception.message shouldBe "At least one memo field must be set."
        }

        test("throws when explicitly passing all nulls") {
            shouldThrow<IllegalArgumentException> {
                Memo(memoData = null, memoType = null, memoFormat = null)
            }
        }
    }

    context("MemoBuilder DSL") {
        test("builds memo with memoData and memoType") {
            val m =
                memo {
                    memoData = "AABB"
                    memoType = "746578742F706C61696E"
                }
            m.memoData shouldBe "AABB"
            m.memoType shouldBe "746578742F706C61696E"
            m.memoFormat shouldBe null
        }

        test("builds memo with all fields") {
            val m =
                memo {
                    memoData = "AABB"
                    memoType = "746578742F706C61696E"
                    memoFormat = "CCDD"
                }
            m.memoData shouldBe "AABB"
            m.memoType shouldBe "746578742F706C61696E"
            m.memoFormat shouldBe "CCDD"
        }

        test("builder with no fields set throws") {
            shouldThrow<IllegalArgumentException> {
                memo { }
            }
        }
    }

    context("Memo equals and hashCode") {
        test("equal memos are equal") {
            val a = Memo(memoData = "AABB", memoType = "746578742F706C61696E")
            val b = Memo(memoData = "AABB", memoType = "746578742F706C61696E")
            a shouldBe b
        }

        test("different memoData means not equal") {
            val a = Memo(memoData = "AABB")
            val b = Memo(memoData = "CCDD")
            a shouldNotBe b
        }

        test("equal memos have equal hashCodes") {
            val a = Memo(memoData = "AABB", memoType = "746578742F706C61696E")
            val b = Memo(memoData = "AABB", memoType = "746578742F706C61696E")
            a.hashCode() shouldBe b.hashCode()
        }

        test("not equal to null") {
            val a = Memo(memoData = "AABB")
            (a.equals(null)) shouldBe false
        }

        test("not equal to different type") {
            val a = Memo(memoData = "AABB")
            (a.equals("string")) shouldBe false
        }
    }

    context("Memo toString") {
        test("toString includes all fields") {
            val m = Memo(memoData = "AABB", memoType = "746578742F706C61696E", memoFormat = "CCDD")
            m.toString() shouldBe "Memo(memoData=AABB, memoType=746578742F706C61696E, memoFormat=CCDD)"
        }

        test("toString with null fields shows null") {
            val m = Memo(memoData = "AABB")
            m.toString() shouldBe "Memo(memoData=AABB, memoType=null, memoFormat=null)"
        }
    }
})
