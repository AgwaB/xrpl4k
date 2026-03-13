package org.xrpl.sdk.core.model.transaction

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.TxHash
import org.xrpl.sdk.core.type.XrpDrops

private val ACCOUNT = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
private val FEE = XrpDrops(12L)
private const val SEQUENCE = 1u
private const val LAST_LEDGER = 1000u

class CommonTransactionFieldsTest : FunSpec({

    context("Construction with required fields") {
        test("constructs with only required fields") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.account shouldBe ACCOUNT
            fields.fee shouldBe FEE
            fields.sequence shouldBe SEQUENCE
            fields.lastLedgerSequence shouldBe LAST_LEDGER
        }
    }

    context("Default optional fields are null or empty") {
        test("accountTxnId defaults to null") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.accountTxnId shouldBe null
        }

        test("memos defaults to empty list") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.memos shouldBe emptyList()
        }

        test("signers defaults to empty list") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.signers shouldBe emptyList()
        }

        test("sourceTag defaults to null") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.sourceTag shouldBe null
        }

        test("ticketSequence defaults to null") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.ticketSequence shouldBe null
        }

        test("networkId defaults to null") {
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            fields.networkId shouldBe null
        }
    }

    context("Construction with all optional fields") {
        test("constructs with all fields set") {
            val txHash = TxHash("A".repeat(64))
            val memo = Memo(memoData = "AABB")
            val signer =
                Signer(
                    account = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
                    txnSignature = "DEADBEEF",
                    signingPubKey = "CAFEBABE",
                )
            val fields =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                    accountTxnId = txHash,
                    memos = listOf(memo),
                    signers = listOf(signer),
                    sourceTag = 42u,
                    ticketSequence = 5u,
                    networkId = 1u,
                )
            fields.accountTxnId shouldBe txHash
            fields.memos shouldBe listOf(memo)
            fields.signers shouldBe listOf(signer)
            fields.sourceTag shouldBe 42u
            fields.ticketSequence shouldBe 5u
            fields.networkId shouldBe 1u
        }
    }

    context("equals and hashCode contract") {
        test("two instances with same required fields are equal") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            val b =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            a shouldBe b
        }

        test("equal instances have equal hashCodes") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            val b =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            a.hashCode() shouldBe b.hashCode()
        }

        test("different fee means not equal") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = XrpDrops(12L),
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            val b =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = XrpDrops(100L),
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            a shouldNotBe b
        }

        test("different sequence means not equal") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = 1u,
                    lastLedgerSequence = LAST_LEDGER,
                )
            val b =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = 2u,
                    lastLedgerSequence = LAST_LEDGER,
                )
            a shouldNotBe b
        }

        test("not equal to null") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            (a.equals(null)) shouldBe false
        }

        test("not equal to different type") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            (a.equals("string")) shouldBe false
        }

        test("reflexive: instance equals itself") {
            val a =
                CommonTransactionFields(
                    account = ACCOUNT,
                    fee = FEE,
                    sequence = SEQUENCE,
                    lastLedgerSequence = LAST_LEDGER,
                )
            a shouldBe a
        }
    }
})
