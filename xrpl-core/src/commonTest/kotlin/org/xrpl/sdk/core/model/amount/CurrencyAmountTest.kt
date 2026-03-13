package org.xrpl.sdk.core.model.amount

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.core.type.XrpDrops

class CurrencyAmountTest : FunSpec({

    context("XrpAmount construction") {
        test("constructs with given drops") {
            val amount = XrpAmount(XrpDrops(1_000_000L))
            amount.drops.value shouldBe 1_000_000L
        }

        test("constructs with zero drops") {
            val amount = XrpAmount(XrpDrops(0L))
            amount.drops.value shouldBe 0L
        }
    }

    context("XrpAmount arithmetic") {
        test("plus adds drops correctly") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            val b = XrpAmount(XrpDrops(500_000L))
            val result = a + b
            result.drops.value shouldBe 1_500_000L
        }

        test("minus subtracts drops correctly") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            val b = XrpAmount(XrpDrops(400_000L))
            val result = a - b
            result.drops.value shouldBe 600_000L
        }

        test("minus resulting in negative throws") {
            val a = XrpAmount(XrpDrops(100L))
            val b = XrpAmount(XrpDrops(200L))
            shouldThrow<IllegalArgumentException> {
                a - b
            }
        }
    }

    context("XrpAmount compareTo") {
        test("greater amount compares correctly") {
            val a = XrpAmount(XrpDrops(200L))
            val b = XrpAmount(XrpDrops(100L))
            (a.compareTo(b) > 0) shouldBe true
        }

        test("lesser amount compares correctly") {
            val a = XrpAmount(XrpDrops(100L))
            val b = XrpAmount(XrpDrops(200L))
            (a.compareTo(b) < 0) shouldBe true
        }

        test("equal amounts compare as zero") {
            val a = XrpAmount(XrpDrops(100L))
            val b = XrpAmount(XrpDrops(100L))
            a.compareTo(b) shouldBe 0
        }
    }

    context("XrpAmount toString") {
        test("1_000_000 drops formats as 1 XRP") {
            XrpAmount(XrpDrops(1_000_000L)).toString() shouldBe "1 XRP"
        }

        test("0 drops formats as 0 XRP") {
            XrpAmount(XrpDrops(0L)).toString() shouldBe "0 XRP"
        }

        test("1_500_000 drops formats as 1.5 XRP") {
            XrpAmount(XrpDrops(1_500_000L)).toString() shouldBe "1.5 XRP"
        }
    }

    context("XrpAmount equals and hashCode") {
        test("equal amounts are equal") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            val b = XrpAmount(XrpDrops(1_000_000L))
            a shouldBe b
        }

        test("different amounts are not equal") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            val b = XrpAmount(XrpDrops(2_000_000L))
            a shouldNotBe b
        }

        test("equal amounts have equal hashCodes") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            val b = XrpAmount(XrpDrops(1_000_000L))
            a.hashCode() shouldBe b.hashCode()
        }

        test("not equal to null") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            (a.equals(null)) shouldBe false
        }

        test("not equal to different type") {
            val a = XrpAmount(XrpDrops(1_000_000L))
            (a.equals("not an amount")) shouldBe false
        }
    }

    context("IssuedAmount construction") {
        test("constructs with currency, issuer, and value") {
            val amount =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            amount.currency.value shouldBe "USD"
            amount.issuer.value shouldBe "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"
            amount.value shouldBe "1.5"
        }

        test("preserves decimal string value exactly") {
            val value = "1.23456789012345"
            val amount =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = value,
                )
            amount.value shouldBe value
        }

        test("preserves scientific notation string value") {
            val value = "1.23e5"
            val amount =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = value,
                )
            amount.value shouldBe value
        }
    }

    context("IssuedAmount equals and hashCode") {
        test("equal amounts are equal") {
            val a =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            val b =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            a shouldBe b
        }

        test("different value means not equal") {
            val a =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            val b =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "2.0",
                )
            a shouldNotBe b
        }

        test("different currency means not equal") {
            val a =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            val b =
                IssuedAmount(
                    currency = CurrencyCode("EUR"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            a shouldNotBe b
        }

        test("equal amounts have equal hashCodes") {
            val a =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            val b =
                IssuedAmount(
                    currency = CurrencyCode("USD"),
                    issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    value = "1.5",
                )
            a.hashCode() shouldBe b.hashCode()
        }
    }

    context("MptAmount construction") {
        test("constructs with valid 48-char ID") {
            val id = "A".repeat(48)
            val amount = MptAmount(mptIssuanceId = id, value = 100L)
            amount.mptIssuanceId shouldBe id
            amount.value shouldBe 100L
        }

        test("rejects ID shorter than 48 characters") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    MptAmount(mptIssuanceId = "A".repeat(47), value = 1L)
                }
            exception.message shouldBe "MPT issuance ID must be 48 hex characters (192 bits). Got 47 characters."
        }

        test("rejects ID longer than 48 characters") {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    MptAmount(mptIssuanceId = "A".repeat(49), value = 1L)
                }
            exception.message shouldBe "MPT issuance ID must be 48 hex characters (192 bits). Got 49 characters."
        }

        test("rejects empty ID") {
            shouldThrow<IllegalArgumentException> {
                MptAmount(mptIssuanceId = "", value = 1L)
            }
        }
    }

    context("MptAmount equals and hashCode") {
        test("equal amounts are equal") {
            val id = "B".repeat(48)
            val a = MptAmount(mptIssuanceId = id, value = 42L)
            val b = MptAmount(mptIssuanceId = id, value = 42L)
            a shouldBe b
        }

        test("different value means not equal") {
            val id = "B".repeat(48)
            val a = MptAmount(mptIssuanceId = id, value = 42L)
            val b = MptAmount(mptIssuanceId = id, value = 43L)
            a shouldNotBe b
        }

        test("different ID means not equal") {
            val a = MptAmount(mptIssuanceId = "A".repeat(48), value = 42L)
            val b = MptAmount(mptIssuanceId = "B".repeat(48), value = 42L)
            a shouldNotBe b
        }

        test("equal amounts have equal hashCodes") {
            val id = "C".repeat(48)
            val a = MptAmount(mptIssuanceId = id, value = 99L)
            val b = MptAmount(mptIssuanceId = id, value = 99L)
            a.hashCode() shouldBe b.hashCode()
        }
    }

    context("CurrencyAmount sealed exhaustive when") {
        test("when expression covers all three subtypes without else") {
            val amounts: List<CurrencyAmount> =
                listOf(
                    XrpAmount(XrpDrops(1_000_000L)),
                    IssuedAmount(
                        currency = CurrencyCode("USD"),
                        issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                        value = "1.0",
                    ),
                    MptAmount(mptIssuanceId = "D".repeat(48), value = 10L),
                )
            val labels =
                amounts.map { amount ->
                    when (amount) {
                        is XrpAmount -> "xrp"
                        is IssuedAmount -> "issued"
                        is MptAmount -> "mpt"
                    }
                }
            labels shouldBe listOf("xrp", "issued", "mpt")
        }
    }
})
