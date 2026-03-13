package org.xrpl.sdk.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.xrpl.sdk.core.util.toHexString

/**
 * T17 — encodeForSigning, encodeForMultiSigning, encodeForSigningClaim,
 *        encodeQuality / decodeQuality, and decodeLedgerData tests.
 */
class SigningAndQualityTest : FunSpec({

    // Base transaction with raw hex account IDs (20 bytes = 40 hex chars).
    val baseTxJson =
        """
        {
            "TransactionType": 0,
            "Flags": 0,
            "Sequence": 1,
            "Fee": "12",
            "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
            "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
            "Amount": "1000000"
        }
        """.trimIndent()

    // ---- Signing tests ----

    test("encodeForSigning starts with TRANSACTION_SIGN prefix 53545800") {
        val signingHex = BinaryCodec.encodeForSigning(baseTxJson)
        signingHex shouldStartWith "53545800"
    }

    test("encodeForSigning excludes TxnSignature") {
        val withSig =
            """
            {
                "TransactionType": 0,
                "Flags": 0,
                "Sequence": 1,
                "Fee": "12",
                "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
                "Amount": "1000000",
                "TxnSignature": "DEADBEEF"
            }
            """.trimIndent()

        val signingWithSig = BinaryCodec.encodeForSigning(withSig)
        val signingWithout = BinaryCodec.encodeForSigning(baseTxJson)

        // TxnSignature is not a signing field — both results must be identical.
        signingWithSig shouldBe signingWithout
    }

    test("encodeForSigning result differs from full encode") {
        val fullHex = BinaryCodec.encode(baseTxJson)
        val signingHex = BinaryCodec.encodeForSigning(baseTxJson)
        // Signing hex has 4-byte prefix prepended, so it can never equal raw encode.
        signingHex shouldNotBe fullHex
    }

    test("encodeForSigning excludes TxnSignature bytes from output") {
        val txWithSignature =
            """
            {
                "TransactionType": 0,
                "Flags": 0,
                "Sequence": 1,
                "Fee": "12",
                "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
                "Amount": "1000000",
                "TxnSignature": "AABBCCDD",
                "SigningPubKey": "0000000000000000000000000000000000000000000000000000000000000000ff"
            }
            """.trimIndent()

        val forSigning = BinaryCodec.encodeForSigning(txWithSignature)
        val fullEncode = BinaryCodec.encode(txWithSignature)

        // Signing output is prefix (8 hex chars) + body without TxnSignature.
        // Full encode includes TxnSignature field header (2 hex) + VL (2 hex) + 4-byte value (8 hex) = 12 hex chars extra.
        val signingBody = forSigning.substring(8) // strip 4-byte prefix
        signingBody.length shouldBe fullEncode.length - (2 + 2 + 8)
    }

    // ---- Multi-signing tests ----

    test("encodeForMultiSigning starts with TRANSACTION_MULTI_SIGN prefix 534d5400") {
        val signerAccountId = "b5f762798a53d543a014caf8b297cff8f2f937e8"
        val multiSignHex = BinaryCodec.encodeForMultiSigning(baseTxJson, signerAccountId)
        multiSignHex shouldStartWith "534d5400"
    }

    test("encodeForMultiSigning appends signer account ID at end") {
        val signerAccountId = "b5f762798a53d543a014caf8b297cff8f2f937e8"
        val multiSignHex = BinaryCodec.encodeForMultiSigning(baseTxJson, signerAccountId)
        multiSignHex.takeLast(40) shouldBe signerAccountId
    }

    test("encodeForMultiSigning with different signer produces different result") {
        val signer1 = "b5f762798a53d543a014caf8b297cff8f2f937e8"
        val signer2 = "f667b0ca50cc7709a220b0561b85e53a48461a8f"
        val hex1 = BinaryCodec.encodeForMultiSigning(baseTxJson, signer1)
        val hex2 = BinaryCodec.encodeForMultiSigning(baseTxJson, signer2)
        hex1 shouldNotBe hex2
    }

    // ---- Payment channel claim tests ----

    test("encodeForSigningClaim starts with PAYMENT_CHANNEL_CLAIM prefix 434c4d00") {
        val claimJson =
            """
            {
                "Channel": "0000000000000000000000000000000000000000000000000000000000000001",
                "Amount": "1000000"
            }
            """.trimIndent()
        val encoded = BinaryCodec.encodeForSigningClaim(claimJson)
        encoded shouldStartWith "434c4d00"
    }

    test("encodeForSigningClaim has correct total length 88 hex chars") {
        val claimJson =
            """
            {
                "Channel": "5db01b7ffed6b67e6b0414ded11e051d2ee2b7619ce0eaa6286d67a3a4d5bdb3",
                "Amount": "1000000"
            }
            """.trimIndent()
        val encoded = BinaryCodec.encodeForSigningClaim(claimJson)
        // 4 bytes prefix + 32 bytes channel + 8 bytes amount = 44 bytes = 88 hex chars
        encoded.length shouldBe 88
    }

    test("encodeForSigningClaim with different channels produces different output") {
        val claim1 =
            BinaryCodec.encodeForSigningClaim(
                """
                {"Channel":"0000000000000000000000000000000000000000000000000000000000000001","Amount":"1000000"}
                """.trimIndent(),
            )
        val claim2 =
            BinaryCodec.encodeForSigningClaim(
                """
                {"Channel":"0000000000000000000000000000000000000000000000000000000000000002","Amount":"1000000"}
                """.trimIndent(),
            )
        claim1 shouldNotBe claim2
    }

    // ---- Quality tests ----

    test("encodeQuality of zero returns 16 zero hex chars") {
        BinaryCodec.encodeQuality("0") shouldBe "0000000000000000"
    }

    test("encodeQuality and decodeQuality roundtrip for integer value") {
        val encoded = BinaryCodec.encodeQuality("1")
        val decoded = BinaryCodec.decodeQuality(encoded)
        decoded shouldBe "1"
    }

    test("encodeQuality and decodeQuality roundtrip for large integer") {
        val encoded = BinaryCodec.encodeQuality("1000000")
        val decoded = BinaryCodec.decodeQuality(encoded)
        decoded shouldBe "1000000"
    }

    test("encodeQuality and decodeQuality roundtrip for decimal value") {
        val quality = "195796912.5171664"
        val encoded = BinaryCodec.encodeQuality(quality)
        val decoded = BinaryCodec.decodeQuality(encoded)
        decoded shouldBe quality
    }

    test("encodeQuality and decodeQuality roundtrip for small decimal") {
        val quality = "0.00000001"
        val encoded = BinaryCodec.encodeQuality(quality)
        val decoded = BinaryCodec.decodeQuality(encoded)
        decoded shouldBe quality
    }

    test("encodeQuality produces 16-char hex string") {
        val encoded = BinaryCodec.encodeQuality("12345")
        encoded.length shouldBe 16
        encoded.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
    }

    test("different quality values produce different encodings") {
        val enc1 = BinaryCodec.encodeQuality("1")
        val enc2 = BinaryCodec.encodeQuality("2")
        enc1 shouldNotBe enc2
    }

    // ---- LedgerData tests ----

    test("decodeLedgerData returns JSON with all header fields") {
        // Build a synthetic ledger header binary.
        val builder = org.xrpl.sdk.codec.binary.BinaryWriter()
        builder.writeUInt32(1000L) // ledger_index
        builder.writeUInt64(99_999_999_999_000_000L) // total_coins
        builder.writeBytes(ByteArray(32) { 0xAA.toByte() }) // parent_hash
        builder.writeBytes(ByteArray(32) { 0xBB.toByte() }) // transaction_hash
        builder.writeBytes(ByteArray(32) { 0xCC.toByte() }) // account_hash
        builder.writeUInt32(750_000_000L) // parent_close_time
        builder.writeUInt32(750_000_001L) // close_time
        builder.writeUInt8(10) // close_time_resolution
        builder.writeUInt8(0) // close_flags

        val hex = builder.toByteArray().toHexString()
        val decoded = BinaryCodec.decodeLedgerData(hex)
        val obj = Json.parseToJsonElement(decoded).jsonObject

        obj["ledger_index"]?.jsonPrimitive?.long shouldBe 1000L
        obj["total_coins"]?.jsonPrimitive?.content shouldBe "99999999999000000"
        obj["parent_hash"]?.jsonPrimitive?.content shouldBe "aa".repeat(32)
        obj["transaction_hash"]?.jsonPrimitive?.content shouldBe "bb".repeat(32)
        obj["account_hash"]?.jsonPrimitive?.content shouldBe "cc".repeat(32)
        obj["parent_close_time"]?.jsonPrimitive?.long shouldBe 750_000_000L
        obj["close_time"]?.jsonPrimitive?.long shouldBe 750_000_001L
        obj["close_time_resolution"]?.jsonPrimitive?.content shouldBe "10"
        obj["close_flags"]?.jsonPrimitive?.content shouldBe "0"
    }

    test("decodeLedgerData total_coins is unsigned string") {
        val builder = org.xrpl.sdk.codec.binary.BinaryWriter()
        builder.writeUInt32(1L)
        builder.writeUInt64(Long.MAX_VALUE) // large value, treated as unsigned
        builder.writeBytes(ByteArray(32))
        builder.writeBytes(ByteArray(32))
        builder.writeBytes(ByteArray(32))
        builder.writeUInt32(0L)
        builder.writeUInt32(0L)
        builder.writeUInt8(1)
        builder.writeUInt8(0)

        val decoded = BinaryCodec.decodeLedgerData(builder.toByteArray().toHexString())
        val obj = Json.parseToJsonElement(decoded).jsonObject

        // total_coins should be the unsigned string representation of Long.MAX_VALUE
        obj["total_coins"]?.jsonPrimitive?.content shouldBe Long.MAX_VALUE.toULong().toString()
    }
})
