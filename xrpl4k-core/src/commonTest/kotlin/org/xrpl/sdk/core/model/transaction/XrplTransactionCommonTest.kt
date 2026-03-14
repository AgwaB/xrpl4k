@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.model.transaction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.model.amount.xrp
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XrpDrops

class XrplTransactionCommonTest : FunSpec({

    val account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val paymentFields =
        PaymentFields(
            destination = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
            amount = 10.xrp,
        )

    // -- Unsigned with flags --

    context("Unsigned flags preservation") {
        test("flags is null by default") {
            val tx =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                )
            tx.flags shouldBe null
        }

        test("flags preserves the value") {
            val flags =
                TransactionFlags.Payment.tfPartialPayment or
                    TransactionFlags.Payment.tfLimitQuality
            val tx =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    flags = flags,
                )
            tx.flags shouldBe flags
            tx.flags shouldBe 0x00060000u
        }

        test("flags participates in equals") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    flags = TransactionFlags.Payment.tfPartialPayment,
                )
            val b =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    flags = TransactionFlags.Payment.tfLimitQuality,
                )
            a shouldNotBe b
        }

        test("null flags vs non-null flags are not equal") {
            val a =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    flags = null,
                )
            val b =
                XrplTransaction.Unsigned(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    flags = 0u,
                )
            a shouldNotBe b
        }
    }

    // -- Filled.create with flags --

    context("Filled.create flags preservation") {
        test("flags preserves the value through Filled.create") {
            val flags =
                TransactionFlags.NFTokenMint.tfTransferable or
                    TransactionFlags.NFTokenMint.tfBurnable
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                    flags = flags,
                )
            filled.flags shouldBe flags
            filled.flags shouldBe 0x00000009u
        }

        test("Filled.create with null flags") {
            val filled =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                )
            filled.flags shouldBe null
        }

        test("Filled equals includes flags") {
            val a =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                    flags = 1u,
                )
            val b =
                XrplTransaction.Filled.create(
                    transactionType = TransactionType.Payment,
                    account = account,
                    fields = paymentFields,
                    fee = XrpDrops(12),
                    sequence = 1u,
                    lastLedgerSequence = 100u,
                    flags = 2u,
                )
            a shouldNotBe b
        }
    }
})
