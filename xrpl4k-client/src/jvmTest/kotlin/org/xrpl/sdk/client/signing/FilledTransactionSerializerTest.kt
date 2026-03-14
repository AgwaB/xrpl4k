@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.xrpl.sdk.codec.BinaryCodec
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.model.transaction.CheckCashFields
import org.xrpl.sdk.core.model.transaction.CheckCreateFields
import org.xrpl.sdk.core.model.transaction.EscrowCreateFields
import org.xrpl.sdk.core.model.transaction.Memo
import org.xrpl.sdk.core.model.transaction.NFTokenCreateOfferFields
import org.xrpl.sdk.core.model.transaction.NFTokenMintFields
import org.xrpl.sdk.core.model.transaction.OfferCreateFields
import org.xrpl.sdk.core.model.transaction.PaymentFields
import org.xrpl.sdk.core.model.transaction.TicketCreateFields
import org.xrpl.sdk.core.model.transaction.TransactionType
import org.xrpl.sdk.core.model.transaction.TrustSetFields
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

    // ── EscrowCreate ────────────────────────────────────────────────────────────

    test("EscrowCreate serializes amount, destination, finishAfter, cancelAfter") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.EscrowCreate,
                account = sender,
                fields =
                    EscrowCreateFields(
                        destination = recipient,
                        amount = XrpAmount(XrpDrops(5_000_000)),
                        finishAfter = 533_257_958u,
                        cancelAfter = 533_344_358u,
                    ),
                fee = XrpDrops(12),
                sequence = 10u,
                lastLedgerSequence = 500u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 1 // EscrowCreate = 1
        map["Amount"] shouldBe "5000000"
        map["FinishAfter"] shouldBe 533_257_958L
        map["CancelAfter"] shouldBe 533_344_358L
        // Destination is hex, not r-address
        (map["Destination"] as String).length shouldBe 40

        // Verify BinaryCodec can encode it
        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("EscrowCreate omits optional fields when null") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.EscrowCreate,
                account = sender,
                fields =
                    EscrowCreateFields(
                        destination = recipient,
                        amount = XrpAmount(XrpDrops(1_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 1u,
                lastLedgerSequence = 100u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        (map.containsKey("FinishAfter")) shouldBe false
        (map.containsKey("CancelAfter")) shouldBe false
        (map.containsKey("Condition")) shouldBe false
        (map.containsKey("DestinationTag")) shouldBe false
    }

    // ── CheckCreate ─────────────────────────────────────────────────────────────

    test("CheckCreate serializes destination, sendMax, expiration") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.CheckCreate,
                account = sender,
                fields =
                    CheckCreateFields(
                        destination = recipient,
                        sendMax = XrpAmount(XrpDrops(10_000_000)),
                        expiration = 570_113_521u,
                    ),
                fee = XrpDrops(12),
                sequence = 3u,
                lastLedgerSequence = 150u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 16 // CheckCreate = 16
        map["SendMax"] shouldBe "10000000"
        map["Expiration"] shouldBe 570_113_521L
        (map["Destination"] as String).length shouldBe 40

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    // ── CheckCash ───────────────────────────────────────────────────────────────

    test("CheckCash serializes checkId and amount") {
        val checkId = "49647F0D748DC3FE26BDACBC57F251AADEFFF391BE6A528CFCD698EB5E85A53B"
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.CheckCash,
                account = sender,
                fields =
                    CheckCashFields(
                        checkId = checkId,
                        amount = XrpAmount(XrpDrops(5_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 4u,
                lastLedgerSequence = 200u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 17 // CheckCash = 17
        map["CheckID"] shouldBe checkId
        map["Amount"] shouldBe "5000000"
        (map.containsKey("DeliverMin")) shouldBe false

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    // ── TicketCreate ────────────────────────────────────────────────────────────

    test("TicketCreate serializes ticketCount") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.TicketCreate,
                account = sender,
                fields = TicketCreateFields(ticketCount = 5u),
                fee = XrpDrops(12),
                sequence = 6u,
                lastLedgerSequence = 300u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 10 // TicketCreate = 10
        map["TicketCount"] shouldBe 5L

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    // ── NFTokenMint ─────────────────────────────────────────────────────────────

    test("NFTokenMint serializes nfTokenTaxon, uri, transferFee, flags") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.NFTokenMint,
                account = sender,
                fields =
                    NFTokenMintFields(
                        nfTokenTaxon = 42u,
                        uri = "697066733A2F2F62616679626569",
                        transferFee = 5000u,
                        // tfTransferable
                        flags = 8u,
                    ),
                fee = XrpDrops(12),
                sequence = 7u,
                lastLedgerSequence = 400u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 25 // NFTokenMint = 25
        map["NFTokenTaxon"] shouldBe 42L
        map["URI"] shouldBe "697066733A2F2F62616679626569"
        map["TransferFee"] shouldBe 5000L
        map["Flags"] shouldBe 8L

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("NFTokenMint omits optional fields when null") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.NFTokenMint,
                account = sender,
                fields = NFTokenMintFields(nfTokenTaxon = 0u),
                fee = XrpDrops(12),
                sequence = 8u,
                lastLedgerSequence = 400u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        (map.containsKey("URI")) shouldBe false
        (map.containsKey("TransferFee")) shouldBe false
        (map.containsKey("Issuer")) shouldBe false
    }

    // ── NFTokenCreateOffer ──────────────────────────────────────────────────────

    test("NFTokenCreateOffer serializes nfTokenId, amount, flags") {
        // 64-char NFT ID
        val tokenId =
            "000800006203F49C21D5D6E022CB16DE3538F248662FC73C29ABA6B8000000010"
                .take(64)
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.NFTokenCreateOffer,
                account = sender,
                fields =
                    NFTokenCreateOfferFields(
                        nfTokenId = tokenId,
                        amount = XrpAmount(XrpDrops(1_000_000)),
                        // tfSellNFToken
                        flags = 1u,
                    ),
                fee = XrpDrops(12),
                sequence = 9u,
                lastLedgerSequence = 500u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 27 // NFTokenCreateOffer = 27
        map["NFTokenID"] shouldBe tokenId
        map["Amount"] shouldBe "1000000"
        map["Flags"] shouldBe 1L
        (map.containsKey("Destination")) shouldBe false
        (map.containsKey("Owner")) shouldBe false

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    // ── TrustSet ────────────────────────────────────────────────────────────────

    test("TrustSet serializes limitAmount as IOU") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.TrustSet,
                account = sender,
                fields =
                    TrustSetFields(
                        limitAmount =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = recipient,
                                value = "1000",
                            ),
                    ),
                fee = XrpDrops(12),
                sequence = 11u,
                lastLedgerSequence = 600u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 20 // TrustSet = 20
        @Suppress("UNCHECKED_CAST")
        val limitAmount = map["LimitAmount"] as Map<String, Any?>
        limitAmount["currency"] shouldBe "USD"
        limitAmount["value"] shouldBe "1000"
        // Issuer should be hex, not r-address
        (limitAmount["issuer"] as String).length shouldBe 40

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("TrustSet omits qualityIn and qualityOut when null") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.TrustSet,
                account = sender,
                fields =
                    TrustSetFields(
                        limitAmount =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = recipient,
                                value = "500",
                            ),
                    ),
                fee = XrpDrops(12),
                sequence = 12u,
                lastLedgerSequence = 600u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        (map.containsKey("QualityIn")) shouldBe false
        (map.containsKey("QualityOut")) shouldBe false
    }

    // ── OfferCreate ─────────────────────────────────────────────────────────────

    test("OfferCreate serializes takerGets and takerPays as XRP") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.OfferCreate,
                account = sender,
                fields =
                    OfferCreateFields(
                        takerGets = XrpAmount(XrpDrops(5_000_000)),
                        takerPays =
                            IssuedAmount(
                                currency = CurrencyCode("USD"),
                                issuer = recipient,
                                value = "25",
                            ),
                    ),
                fee = XrpDrops(12),
                sequence = 13u,
                lastLedgerSequence = 700u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        map["TransactionType"] shouldBe 7 // OfferCreate = 7
        map["TakerGets"] shouldBe "5000000"
        @Suppress("UNCHECKED_CAST")
        val takerPays = map["TakerPays"] as Map<String, Any?>
        takerPays["currency"] shouldBe "USD"
        takerPays["value"] shouldBe "25"

        val jsonStr = FilledTransactionSerializer.toJsonString(filled)
        val encoded = BinaryCodec.encodeForSigning(jsonStr)
        encoded.isNotEmpty() shouldBe true
    }

    test("OfferCreate omits optional expiration and offerSequence when null") {
        val filled =
            XrplTransaction.Filled.create(
                transactionType = TransactionType.OfferCreate,
                account = sender,
                fields =
                    OfferCreateFields(
                        takerGets = XrpAmount(XrpDrops(1_000_000)),
                        takerPays = XrpAmount(XrpDrops(2_000_000)),
                    ),
                fee = XrpDrops(12),
                sequence = 14u,
                lastLedgerSequence = 700u,
            )
        val map = FilledTransactionSerializer.toCodecMap(filled)

        (map.containsKey("Expiration")) shouldBe false
        (map.containsKey("OfferSequence")) shouldBe false
    }
})
