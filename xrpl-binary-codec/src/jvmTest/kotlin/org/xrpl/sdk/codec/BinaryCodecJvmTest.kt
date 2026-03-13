package org.xrpl.sdk.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * T16 — BinaryCodec encode/decode tests (JVM-specific test suite).
 *
 * Account IDs are raw 20-byte hex strings (40 chars).
 * TransactionType is an integer code (Payment = 0, OfferCreate = 7, TrustSet = 20).
 */
class BinaryCodecJvmTest : FunSpec({

    // Minimal Payment transaction using raw hex account IDs and integer TransactionType.
    val paymentJson =
        """
        {
            "TransactionType": 0,
            "Flags": 0,
            "Sequence": 1,
            "Fee": "1000000",
            "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
            "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
            "Amount": "1000000"
        }
        """.trimIndent()

    test("encode then decode roundtrip preserves data") {
        val hex = BinaryCodec.encode(paymentJson)
        val decoded = BinaryCodec.decode(hex)
        // Re-encoding the decoded JSON must produce identical bytes.
        BinaryCodec.encode(decoded) shouldBe hex
    }

    test("encode produces valid lowercase hex string") {
        val hex = BinaryCodec.encode(paymentJson)
        hex.isNotEmpty() shouldBe true
        hex.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
    }

    test("encode output starts with TransactionType field header 0x12") {
        // TransactionType is type=1 (UInt16), field=2 → header nibbles 1,2 → byte 0x12.
        val hex = BinaryCodec.encode(paymentJson)
        hex shouldStartWith "12"
    }

    test("decode produces JSON with correct field names") {
        val hex = BinaryCodec.encode(paymentJson)
        val decoded = BinaryCodec.decode(hex)
        decoded shouldContain "TransactionType"
        decoded shouldContain "Account"
        decoded shouldContain "Destination"
        decoded shouldContain "Amount"
        decoded shouldContain "Fee"
    }

    test("decode reconstructs numeric fields correctly") {
        val hex = BinaryCodec.encode(paymentJson)
        val decoded = BinaryCodec.decode(hex)
        val obj = Json.parseToJsonElement(decoded).jsonObject

        obj["Flags"]?.jsonPrimitive?.long shouldBe 0L
        obj["Sequence"]?.jsonPrimitive?.long shouldBe 1L
        obj["Fee"]?.jsonPrimitive?.content shouldBe "1000000"
        obj["Amount"]?.jsonPrimitive?.content shouldBe "1000000"
    }

    test("decode reconstructs account IDs correctly") {
        val hex = BinaryCodec.encode(paymentJson)
        val decoded = BinaryCodec.decode(hex)
        val obj = Json.parseToJsonElement(decoded).jsonObject

        obj["Account"]?.jsonPrimitive?.content shouldBe
            "b5f762798a53d543a014caf8b297cff8f2f937e8"
        obj["Destination"]?.jsonPrimitive?.content shouldBe
            "f667b0ca50cc7709a220b0561b85e53a48461a8f"
    }

    test("IOU Amount encodes and decodes correctly") {
        // Issuer must be a raw 20-byte hex (40 chars).
        val iouJson =
            """
            {
                "TransactionType": 0,
                "Flags": 0,
                "Sequence": 2,
                "Fee": "12",
                "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
                "Amount": {
                    "value": "100",
                    "currency": "USD",
                    "issuer": "f667b0ca50cc7709a220b0561b85e53a48461a8f"
                }
            }
            """.trimIndent()

        val hex = BinaryCodec.encode(iouJson)
        val decoded = BinaryCodec.decode(hex)

        decoded shouldContain "USD"
        decoded shouldContain "value"
        decoded shouldContain "issuer"

        // Roundtrip
        BinaryCodec.encode(decoded) shouldBe hex
    }

    test("OfferCreate encodes and decodes") {
        // TransactionType 7 = OfferCreate
        val offerJson =
            """
            {
                "TransactionType": 7,
                "Flags": 0,
                "Sequence": 3,
                "Fee": "12",
                "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                "TakerPays": "1000000",
                "TakerGets": {
                    "value": "1",
                    "currency": "USD",
                    "issuer": "f667b0ca50cc7709a220b0561b85e53a48461a8f"
                }
            }
            """.trimIndent()

        val hex = BinaryCodec.encode(offerJson)
        val decoded = BinaryCodec.decode(hex)

        decoded shouldContain "TakerPays"
        decoded shouldContain "TakerGets"
        BinaryCodec.encode(decoded) shouldBe hex
    }

    test("TrustSet encodes and decodes") {
        // TransactionType 20 = TrustSet
        val trustSetJson =
            """
            {
                "TransactionType": 20,
                "Flags": 0,
                "Sequence": 4,
                "Fee": "12",
                "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                "LimitAmount": {
                    "value": "1000",
                    "currency": "USD",
                    "issuer": "f667b0ca50cc7709a220b0561b85e53a48461a8f"
                }
            }
            """.trimIndent()

        val hex = BinaryCodec.encode(trustSetJson)
        val decoded = BinaryCodec.decode(hex)

        decoded shouldContain "LimitAmount"
        decoded shouldContain "USD"
        BinaryCodec.encode(decoded) shouldBe hex
    }

    test("encode empty Flags field is preserved") {
        val json =
            """
            {
                "TransactionType": 0,
                "Flags": 0,
                "Sequence": 1,
                "Fee": "12",
                "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
                "Amount": "500000"
            }
            """.trimIndent()

        val hex = BinaryCodec.encode(json)
        val decoded = BinaryCodec.decode(hex)
        val obj = Json.parseToJsonElement(decoded).jsonObject
        obj["Flags"]?.jsonPrimitive?.long shouldBe 0L
    }

    test("two different transactions encode to different hex") {
        val tx1 =
            BinaryCodec.encode(
                """
                {
                    "TransactionType": 0,
                    "Flags": 0,
                    "Sequence": 1,
                    "Fee": "12",
                    "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                    "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
                    "Amount": "100"
                }
                """.trimIndent(),
            )

        val tx2 =
            BinaryCodec.encode(
                """
                {
                    "TransactionType": 0,
                    "Flags": 0,
                    "Sequence": 1,
                    "Fee": "12",
                    "Account": "b5f762798a53d543a014caf8b297cff8f2f937e8",
                    "Destination": "f667b0ca50cc7709a220b0561b85e53a48461a8f",
                    "Amount": "200"
                }
                """.trimIndent(),
            )

        (tx1 == tx2) shouldBe false
    }
})
