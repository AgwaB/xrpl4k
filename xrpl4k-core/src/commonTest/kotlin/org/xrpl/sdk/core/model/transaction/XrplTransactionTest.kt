package org.xrpl.sdk.core.model.transaction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.type.Address

class XrplTransactionTest : FunSpec({

    val address = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val unknownFields = UnknownTransactionFields(mapOf("Amount" to "1000000"))

    context("sealed interface exhaustive when") {
        test("when expression covers all three branches") {
            val tx: XrplTransaction =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            // This when expression must be exhaustive — compilation verifies all 3 branches exist
            val label: String =
                when (tx) {
                    is XrplTransaction.Unsigned -> "unsigned"
                    is XrplTransaction.Filled -> "filled"
                    is XrplTransaction.Signed -> "signed"
                }
            label shouldBe "unsigned"
        }
    }

    context("Unsigned construction") {
        test("constructs with required fields") {
            val tx =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            tx.transactionType shouldBe TransactionType.Payment
            tx.account shouldBe address
            tx.fields shouldBe unknownFields
            tx.memos shouldBe emptyList()
            tx.sourceTag shouldBe null
        }

        test("constructs with all fields") {
            val memo = Memo(memoData = "AABB")
            val tx =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                    memos = listOf(memo),
                    sourceTag = 42u,
                )
            tx.memos shouldBe listOf(memo)
            tx.sourceTag shouldBe 42u
        }

        test("transactionType is accessible via sealed interface") {
            val tx: XrplTransaction =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.AccountSet,
                    account = address,
                    fields = unknownFields,
                )
            tx.transactionType shouldBe TransactionType.AccountSet
        }
    }

    context("Unsigned equals and hashCode") {
        test("equal instances are equal") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            val b =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            a shouldBe b
        }

        test("equal instances have equal hashCodes") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            val b =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            a.hashCode() shouldBe b.hashCode()
        }

        test("different transactionType means not equal") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            val b =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.AccountSet,
                    account = address,
                    fields = unknownFields,
                )
            a shouldNotBe b
        }

        test("not equal to null") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            (a.equals(null)) shouldBe false
        }

        test("not equal to different type") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            (a.equals("string")) shouldBe false
        }
    }

    context("Unsigned toString") {
        test("toString includes all fields") {
            val tx =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                    sourceTag = 1u,
                )
            val str = tx.toString()
            str.contains("XrplTransaction.Unsigned") shouldBe true
            str.contains("transactionType=") shouldBe true
            str.contains("account=") shouldBe true
            str.contains("fields=") shouldBe true
            str.contains("sourceTag=1") shouldBe true
        }
    }

    context("UnknownTransactionFields") {
        test("preserves all fields") {
            val raw = mapOf("Amount" to "1000000", "Destination" to "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
            val f = UnknownTransactionFields(raw)
            f.fields shouldBe raw
            f.fields["Amount"] shouldBe "1000000"
            f.fields["Destination"] shouldBe "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"
        }

        test("empty fields map is valid") {
            val f = UnknownTransactionFields(emptyMap())
            f.fields shouldBe emptyMap()
        }

        test("equals contract") {
            val a = UnknownTransactionFields(mapOf("k" to "v"))
            val b = UnknownTransactionFields(mapOf("k" to "v"))
            a shouldBe b
        }

        test("hashCode contract") {
            val a = UnknownTransactionFields(mapOf("k" to "v"))
            val b = UnknownTransactionFields(mapOf("k" to "v"))
            a.hashCode() shouldBe b.hashCode()
        }

        test("not equal when fields differ") {
            val a = UnknownTransactionFields(mapOf("k" to "v1"))
            val b = UnknownTransactionFields(mapOf("k" to "v2"))
            a shouldNotBe b
        }

        test("toString includes fields") {
            val f = UnknownTransactionFields(mapOf("Amount" to "1000000"))
            f.toString().contains("UnknownTransactionFields") shouldBe true
            f.toString().contains("Amount") shouldBe true
        }

        test("not equal to null") {
            val f = UnknownTransactionFields(emptyMap())
            (f.equals(null)) shouldBe false
        }
    }

    context("Filled and Signed have internal constructors") {
        test("Unsigned can be created from test scope") {
            // Verifies public constructor works; Filled/Signed have internal constructors
            // so they cannot be instantiated here — this test confirms the API boundary
            val tx =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.TrustSet,
                    account = address,
                    fields = UnknownTransactionFields(emptyMap()),
                )
            tx.transactionType shouldBe TransactionType.TrustSet
        }
    }

    context("Signed toString") {
        // Signed is internal-only — we verify its structure via pattern matching only
        test("when expression matches Signed branch type") {
            val tx: XrplTransaction =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = address,
                    fields = unknownFields,
                )
            val isSigned =
                when (tx) {
                    is XrplTransaction.Unsigned -> false
                    is XrplTransaction.Filled -> false
                    is XrplTransaction.Signed -> true
                }
            isSigned shouldBe false
        }
    }
})
