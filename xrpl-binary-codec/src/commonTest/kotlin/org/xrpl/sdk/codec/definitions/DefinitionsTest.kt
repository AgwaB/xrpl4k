package org.xrpl.sdk.codec.definitions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class DefinitionsTest : FunSpec({

    test("definitions JSON loads without error") {
        Definitions.typesByName shouldNotBe null
        Definitions.fields shouldNotBe null
    }

    test("all TYPES entries are loaded") {
        // definitions.json has 31 type entries
        Definitions.typesByName.size shouldBeGreaterThanOrEqual 31
    }

    test("known type codes are present") {
        Definitions.typesByName["UInt16"] shouldBe 1
        Definitions.typesByName["UInt32"] shouldBe 2
        Definitions.typesByName["UInt64"] shouldBe 3
        Definitions.typesByName["Hash256"] shouldBe 5
        Definitions.typesByName["Amount"] shouldBe 6
        Definitions.typesByName["Blob"] shouldBe 7
        Definitions.typesByName["AccountID"] shouldBe 8
        Definitions.typesByName["STObject"] shouldBe 14
        Definitions.typesByName["STArray"] shouldBe 15
        Definitions.typesByName["Transaction"] shouldBe 10001
        Definitions.typesByName["LedgerEntry"] shouldBe 10002
        Definitions.typesByName["Validation"] shouldBe 10003
        Definitions.typesByName["Metadata"] shouldBe 10004
    }

    test("reverse type lookup works") {
        Definitions.typesByCode[6] shouldBe "Amount"
        Definitions.typesByCode[5] shouldBe "Hash256"
        Definitions.typesByCode[10001] shouldBe "Transaction"
    }

    test("Amount field has typeCode 6") {
        val amount = Definitions.fields["Amount"]
        amount shouldNotBe null
        amount!!.typeCode shouldBe TypeCode.Amount
        amount.typeCode.value shouldBe 6
    }

    test("Fee field is serialized and is a signing field") {
        val fee = Definitions.fields["Fee"]
        fee shouldNotBe null
        fee!!.isSerialized shouldBe true
        fee.isSigningField shouldBe true
        fee.typeCode shouldBe TypeCode.Amount
    }

    test("Destination field is VL-encoded") {
        val dest = Definitions.fields["Destination"]
        dest shouldNotBe null
        dest!!.isVlEncoded shouldBe true
        dest.typeCode shouldBe TypeCode.AccountID
    }

    test("field ID encoding -- both codes below 16 (1 byte)") {
        // TransactionType: typeCode=1 (UInt16), fieldCode=2
        val fieldId = FieldId(typeCode = 1, fieldCode = 2)
        val bytes = fieldId.toBytes()
        bytes shouldHaveSize 1
        bytes[0] shouldBe 0x12.toByte() // (1 shl 4) or 2

        val (decoded, consumed) = FieldId.fromBytes(bytes)
        decoded shouldBe fieldId
        consumed shouldBe 1
    }

    test("field ID encoding -- typeCode >= 16 fieldCode < 16 (2 bytes)") {
        // UInt8 type = 16, field code = 1
        val fieldId = FieldId(typeCode = 16, fieldCode = 1)
        val bytes = fieldId.toBytes()
        bytes shouldHaveSize 2
        bytes[0] shouldBe 0x01.toByte() // (0 shl 4) or 1
        bytes[1] shouldBe 16.toByte() // typeCode

        val (decoded, consumed) = FieldId.fromBytes(bytes)
        decoded shouldBe fieldId
        consumed shouldBe 2
    }

    test("field ID encoding -- typeCode < 16 fieldCode >= 16 (2 bytes)") {
        val fieldId = FieldId(typeCode = 2, fieldCode = 20)
        val bytes = fieldId.toBytes()
        bytes shouldHaveSize 2
        bytes[0] shouldBe 0x20.toByte() // (2 shl 4) or 0
        bytes[1] shouldBe 20.toByte() // fieldCode

        val (decoded, consumed) = FieldId.fromBytes(bytes)
        decoded shouldBe fieldId
        consumed shouldBe 2
    }

    test("field ID encoding -- both codes >= 16 (3 bytes)") {
        val fieldId = FieldId(typeCode = 20, fieldCode = 30)
        val bytes = fieldId.toBytes()
        bytes shouldHaveSize 3
        bytes[0] shouldBe 0x00.toByte()
        bytes[1] shouldBe 20.toByte()
        bytes[2] shouldBe 30.toByte()

        val (decoded, consumed) = FieldId.fromBytes(bytes)
        decoded shouldBe fieldId
        consumed shouldBe 3
    }

    test("field ID round-trip at non-zero offset") {
        val prefix = byteArrayOf(0xFF.toByte(), 0xAA.toByte())
        val fieldId = FieldId(typeCode = 7, fieldCode = 3)
        val encoded = fieldId.toBytes()
        val combined = prefix + encoded

        val (decoded, consumed) = FieldId.fromBytes(combined, offset = 2)
        decoded shouldBe fieldId
        consumed shouldBe 1
    }

    test("canonical ordering sorts by typeCode then fieldCode") {
        val fieldNames = listOf("Fee", "Destination", "Amount", "TransactionType")
        val ordered = Definitions.getCanonicalFieldOrder(fieldNames)

        // Expected order by (typeCode, fieldCode):
        // TransactionType: (1, 2)  -- UInt16
        // Amount:          (6, 1)  -- Amount type
        // Fee:             (6, 8)  -- Amount type
        // Destination:     (8, 3)  -- AccountID
        ordered.map { it.name } shouldBe
            listOf(
                "TransactionType",
                "Amount",
                "Fee",
                "Destination",
            )
    }

    test("all TRANSACTION_TYPES entries are loaded") {
        Definitions.transactionTypesByName.size shouldBeGreaterThanOrEqual 30
    }

    test("Payment transaction type has code 0") {
        Definitions.transactionTypesByName["Payment"] shouldBe 0
    }

    test("transaction type reverse lookup works") {
        Definitions.transactionTypesByCode[0] shouldBe "Payment"
        Definitions.transactionTypesByCode[3] shouldBe "AccountSet"
    }

    test("all LEDGER_ENTRY_TYPES entries are loaded") {
        Definitions.ledgerEntryTypesByName.size shouldBeGreaterThanOrEqual 20
    }

    test("AccountRoot ledger entry type lookup") {
        Definitions.ledgerEntryTypesByName["AccountRoot"] shouldBe 97
    }

    test("ledger entry type reverse lookup works") {
        Definitions.ledgerEntryTypesByCode[97] shouldBe "AccountRoot"
        Definitions.ledgerEntryTypesByCode[111] shouldBe "Offer"
    }

    test("fieldsByFieldId lookup works") {
        val feeFieldId = FieldId(typeCode = 6, fieldCode = 8)
        val fee = Definitions.fieldsByFieldId[feeFieldId]
        fee shouldNotBe null
        fee!!.name shouldBe "Fee"
    }
})
