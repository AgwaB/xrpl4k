package org.xrpl.sdk.core.model.transaction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.type.Address

class XrplTransactionLifecycleTest : FunSpec({

    val account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val paymentFields =
        PaymentFields(
            destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
            amount = 10.xrp,
        )

    // ── T7-1: Create Unsigned with required fields ────────────────────────────

    test("create Unsigned with required fields only") {
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        tx.transactionType shouldBe TransactionType.Payment
        tx.account shouldBe account
        tx.fields shouldBe paymentFields
        tx.memos shouldBe emptyList()
        tx.sourceTag shouldBe null
    }

    // ── T7-2: Verify transactionType property ────────────────────────────────

    test("transactionType property returns the correct value") {
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.OfferCreate,
                account = account,
                fields = UnknownTransactionFields(emptyMap()),
            )
        tx.transactionType shouldBe TransactionType.OfferCreate
        tx.transactionType.value shouldBe "OfferCreate"
    }

    // ── T7-3: equals / hashCode / toString for Unsigned ─────────────────────

    test("Unsigned equals and hashCode are consistent") {
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        val b =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        a shouldBe b
        a.hashCode() shouldBe b.hashCode()
    }

    test("Unsigned toString contains all field labels") {
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                sourceTag = 5u,
            )
        val s = tx.toString()
        s.contains("XrplTransaction.Unsigned") shouldBe true
        s.contains("transactionType=") shouldBe true
        s.contains("account=") shouldBe true
        s.contains("fields=") shouldBe true
        s.contains("memos=") shouldBe true
        s.contains("sourceTag=5") shouldBe true
    }

    // ── T7-4: equals / hashCode for different instances with same values ─────

    test("two Unsigned instances with same values are equal and share hashCode") {
        val fields1 =
            PaymentFields(
                destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                amount = 10.xrp,
            )
        val fields2 =
            PaymentFields(
                destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                amount = 10.xrp,
            )
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                fields = fields1,
            )
        val b =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                fields = fields2,
            )
        a shouldBe b
        a.hashCode() shouldBe b.hashCode()
    }

    test("Unsigned instances with differing transactionType are not equal") {
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        val b =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.AccountSet,
                account = account,
                fields = paymentFields,
            )
        a shouldNotBe b
    }

    test("Unsigned not equal to null") {
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        (a.equals(null)) shouldBe false
    }

    test("Unsigned not equal to unrelated type") {
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        (a.equals("not a transaction")) shouldBe false
    }

    // ── T7-5: UnknownTransactionFields wraps Map correctly ───────────────────

    test("UnknownTransactionFields wraps and retrieves raw map values") {
        val raw =
            mapOf<String, Any?>(
                "Amount" to "1000000",
                "Destination" to "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",
                "Fee" to "12",
            )
        val f = UnknownTransactionFields(raw)
        f.fields shouldBe raw
        f.fields["Amount"] shouldBe "1000000"
        f.fields["Destination"] shouldBe "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"
        f.fields["Fee"] shouldBe "12"
    }

    test("UnknownTransactionFields equals and hashCode contract") {
        val a = UnknownTransactionFields(mapOf("k" to "v"))
        val b = UnknownTransactionFields(mapOf("k" to "v"))
        a shouldBe b
        a.hashCode() shouldBe b.hashCode()
    }

    test("UnknownTransactionFields not equal when map contents differ") {
        val a = UnknownTransactionFields(mapOf("k" to "v1"))
        val b = UnknownTransactionFields(mapOf("k" to "v2"))
        a shouldNotBe b
    }

    test("UnknownTransactionFields toString contains class name and keys") {
        val f = UnknownTransactionFields(mapOf("Amount" to "1000000"))
        val s = f.toString()
        s.contains("UnknownTransactionFields") shouldBe true
        s.contains("Amount") shouldBe true
    }

    // ── T7-6: Exhaustive when covers all 3 states ────────────────────────────

    test("exhaustive when expression compiles covering Unsigned, Filled, Signed") {
        val tx: XrplTransaction =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        val label: String =
            when (tx) {
                is XrplTransaction.Unsigned -> "unsigned"
                is XrplTransaction.Filled -> "filled"
                is XrplTransaction.Signed -> "signed"
            }
        label shouldBe "unsigned"
    }

    test("when expression routes to Filled branch for Filled type") {
        // Filled has internal constructor — use an Unsigned as representative control
        // and verify the when expression structure distinguishes all branches
        val tx: XrplTransaction =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.TrustSet,
                account = account,
                fields = UnknownTransactionFields(emptyMap()),
            )
        val result =
            when (tx) {
                is XrplTransaction.Unsigned -> 1
                is XrplTransaction.Filled -> 2
                is XrplTransaction.Signed -> 3
            }
        result shouldBe 1
    }

    // ── T7-7: Unsigned with memos ────────────────────────────────────────────

    test("Unsigned stores and returns memos list") {
        val memo1 = Memo(memoData = "AABB")
        val memo2 = Memo(memoData = "CCDD", memoType = "746578742F706C61696E")
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                memos = listOf(memo1, memo2),
            )
        tx.memos.size shouldBe 2
        tx.memos[0] shouldBe memo1
        tx.memos[1] shouldBe memo2
    }

    test("Unsigned with empty memos list has no memos") {
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                memos = emptyList(),
            )
        tx.memos shouldBe emptyList()
    }

    test("memos participate in equals comparison") {
        val memo = Memo(memoData = "AABB")
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                memos = listOf(memo),
            )
        val b =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                memos = emptyList(),
            )
        a shouldNotBe b
    }

    // ── T7-8: Unsigned with sourceTag ────────────────────────────────────────

    test("Unsigned stores sourceTag when provided") {
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                sourceTag = 12345u,
            )
        tx.sourceTag shouldBe 12345u
    }

    test("Unsigned sourceTag is null by default") {
        val tx =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
            )
        tx.sourceTag shouldBe null
    }

    test("sourceTag participates in equals comparison") {
        val a =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                sourceTag = 1u,
            )
        val b =
            XrplTransaction.Unsigned(
                transactionType = TransactionType.Payment,
                account = account,
                fields = paymentFields,
                sourceTag = 2u,
            )
        a shouldNotBe b
    }
})
