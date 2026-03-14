@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.Memo
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.core.type.XrpDrops

class FilledTransactionSerializerTest : FunSpec({

    val sender = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val recipient = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")

    test("serialize XRP Payment to JSON accepted by BinaryCodec") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.Payment,
                account = sender,
                fields =
                    PaymentFields(
                        destination = recipient,
                        amount = XrpAmount(XrpDrops(1_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 1u,
                lastLedgerSequence = 100u,
            )
        val jsonStr = FilledTransactionSerializer.toJsonString(filled)

        // TransactionType is serialized as integer code (Payment = 0), not string name
        jsonStr shouldContain "\"TransactionType\":0"
        // Accounts are 40-char hex account IDs, not r-addresses
        jsonStr shouldNotContain "rHb9"
        jsonStr shouldNotContain "rPT1"
        // Amount is drops as string
        jsonStr shouldContain "\"Amount\":\"1000000\""
        jsonStr shouldContain "\"Fee\":\"12\""

        // Verify BinaryCodec can encode it
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("serialize IOU Payment to JSON") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.Payment,
                account = sender,
                fields =
                    PaymentFields(
                        destination = recipient,
                        amount =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = sender,
                                value = "100",
                            ),
                    ),
                fee = XrpDrops(12),
                sequence = 1u,
                lastLedgerSequence = 100u,
            )
        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        jsonStr shouldContain "\"currency\":\"USD\""
        jsonStr shouldContain "\"value\":\"100\""

        // Verify BinaryCodec accepts it
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("optional fields omitted when null") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.Payment,
                account = sender,
                fields =
                    PaymentFields(
                        destination = recipient,
                        amount = XrpAmount(XrpDrops(1_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 1u,
                lastLedgerSequence = 100u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)
        (map.containsKey("SourceTag")) shouldBe false
        (map.containsKey("AccountTxnID")) shouldBe false
        (map.containsKey("Memos")) shouldBe false
    }

    test("Memos serialized correctly") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.Payment,
                account = sender,
                fields =
                    PaymentFields(
                        destination = recipient,
                        amount = XrpAmount(XrpDrops(1_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 1u,
                lastLedgerSequence = 100u,
                memos =
                    listOf(
                        Memo(memoData = "48656C6C6F", memoType = "746578742F706C61696E"),
                    ),
            )
        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        jsonStr shouldContain "Memos"
        jsonStr shouldContain "MemoData"
        jsonStr shouldContain "48656C6C6F"

        // BinaryCodec should accept it
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("numeric fields use correct types for codec") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.Payment,
                account = sender,
                fields =
                    PaymentFields(
                        destination = recipient,
                        amount = XrpAmount(XrpDrops(1_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 5u,
                lastLedgerSequence = 200u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)
        // TransactionType must be Int (UInt16)
        (map["TransactionType"] is Int) shouldBe true
        // Sequence and LastLedgerSequence must be Long (UInt32)
        (map["Sequence"] is Long) shouldBe true
        (map["LastLedgerSequence"] is Long) shouldBe true
        map["Sequence"] shouldBe 5L
        map["LastLedgerSequence"] shouldBe 200L
    }
})
