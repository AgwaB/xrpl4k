package org.xrpl.sdk.codec.hashing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.util.toHexString

class HashPrefixTest : FunSpec({

    test("TRANSACTION_ID bytes match 'TXN\\0' = 0x54584E00") {
        HashPrefix.TRANSACTION_ID.bytes.toHexString() shouldBe "54584e00"
    }

    test("TRANSACTION_NODE bytes match 'SND\\0' = 0x534E4400") {
        HashPrefix.TRANSACTION_NODE.bytes.toHexString() shouldBe "534e4400"
    }

    test("INNER_NODE bytes match 'MIN\\0' = 0x4D494E00") {
        HashPrefix.INNER_NODE.bytes.toHexString() shouldBe "4d494e00"
    }

    test("LEAF_NODE bytes match 'MLN\\0' = 0x4D4C4E00") {
        HashPrefix.LEAF_NODE.bytes.toHexString() shouldBe "4d4c4e00"
    }

    test("TRANSACTION_SIGN bytes match 'STX\\0' = 0x53545800") {
        HashPrefix.TRANSACTION_SIGN.bytes.toHexString() shouldBe "53545800"
    }

    test("TRANSACTION_SIGN_TESTNET bytes match 'stx\\0' = 0x73747800") {
        HashPrefix.TRANSACTION_SIGN_TESTNET.bytes.toHexString() shouldBe "73747800"
    }

    test("TRANSACTION_MULTISIGN bytes match 'SMT\\0' = 0x534D5400") {
        HashPrefix.TRANSACTION_MULTISIGN.bytes.toHexString() shouldBe "534d5400"
    }

    test("LEDGER bytes match 'LWR\\0' = 0x4C575200") {
        HashPrefix.LEDGER.bytes.toHexString() shouldBe "4c575200"
    }

    test("all prefixes are exactly 4 bytes") {
        HashPrefix.entries.forEach { prefix ->
            prefix.bytes.size shouldBe 4
        }
    }

    test("all prefixes end with 0x00") {
        HashPrefix.entries.forEach { prefix ->
            prefix.bytes[3] shouldBe 0x00.toByte()
        }
    }
})
