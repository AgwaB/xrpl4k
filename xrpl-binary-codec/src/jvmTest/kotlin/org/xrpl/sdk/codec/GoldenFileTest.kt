package org.xrpl.sdk.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

/**
 * T18 — Golden file tests.
 *
 * Each vector encodes a known JSON transaction and verifies:
 *   1. encode(json) produces a non-empty hex string.
 *   2. encode(decode(encode(json))) == encode(json)  (stable roundtrip).
 *
 * Account and Destination values are 20-byte hex strings (40 hex chars) as
 * required by AccountIdSerializer — the codec does not perform base58 decoding.
 *
 * The two canonical test accounts:
 *   ACCOUNT_A = 0000000000000000000000000000000000000001
 *   ACCOUNT_B = 0000000000000000000000000000000000000002
 *
 * IOU issuer is also expressed as a 40-char hex string.
 */
class GoldenFileTest : FunSpec({

    // ── canonical hex account IDs ──────────────────────────────────────────
    val accountA = "0000000000000000000000000000000000000001"
    val accountB = "0000000000000000000000000000000000000002"
    val issuer = "0000000000000000000000000000000000000003"

    // ── golden vectors ─────────────────────────────────────────────────────
    val goldenVectors =
        listOf(
            GoldenVector(
                name = "Basic XRP Payment",
                json =
                    """{"TransactionType":0,"Flags":0,"Sequence":1,"Fee":"12",""" +
                        """"Account":"$accountA","Destination":"$accountB","Amount":"1000000"}""",
            ),
            GoldenVector(
                name = "XRP Payment with larger amount",
                json =
                    """{"TransactionType":0,"Flags":0,"Sequence":42,"Fee":"100",""" +
                        """"Account":"$accountA","Destination":"$accountB","Amount":"999000000"}""",
            ),
            GoldenVector(
                name = "Payment with IOU amount",
                json =
                    """{"TransactionType":0,"Flags":0,"Sequence":5,"Fee":"12",""" +
                        """"Account":"$accountA","Destination":"$accountB",""" +
                        """"Amount":{"value":"100","currency":"USD","issuer":"$issuer"}}""",
            ),
            GoldenVector(
                name = "OfferCreate XRP/IOU",
                json =
                    """{"TransactionType":7,"Flags":0,"Sequence":2,"Fee":"12",""" +
                        """"Account":"$accountA","TakerGets":"1000000",""" +
                        """"TakerPays":{"value":"100","currency":"USD","issuer":"$issuer"}}""",
            ),
            GoldenVector(
                name = "TrustSet",
                json =
                    """{"TransactionType":20,"Flags":0,"Sequence":3,"Fee":"12",""" +
                        """"Account":"$accountA",""" +
                        """"LimitAmount":{"value":"1000000","currency":"USD","issuer":"$issuer"}}""",
            ),
            GoldenVector(
                name = "AccountSet with SetFlag",
                json =
                    """{"TransactionType":3,"Flags":0,"Sequence":4,"Fee":"12",""" +
                        """"Account":"$accountA","SetFlag":8}""",
            ),
            GoldenVector(
                name = "OfferCancel",
                json =
                    """{"TransactionType":8,"Flags":0,"Sequence":6,"Fee":"12",""" +
                        """"Account":"$accountA","OfferSequence":7}""",
            ),
            GoldenVector(
                name = "XRP Payment zero flags",
                json =
                    """{"TransactionType":0,"Flags":0,"Sequence":100,"Fee":"1000",""" +
                        """"Account":"$accountA","Destination":"$accountB","Amount":"1"}""",
            ),
        )

    // ── roundtrip tests ────────────────────────────────────────────────────
    for (vector in goldenVectors) {
        test("golden: ${vector.name} — encode produces hex") {
            val encoded = BinaryCodec.encode(vector.json)
            encoded.isNotEmpty() shouldBe true
            encoded.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
        }

        test("golden: ${vector.name} — encode/decode/re-encode is stable") {
            val encoded = BinaryCodec.encode(vector.json)
            val decoded = BinaryCodec.decode(encoded)
            val reEncoded = BinaryCodec.encode(decoded)
            reEncoded shouldBe encoded
        }
    }

    // ── signing prefix tests ───────────────────────────────────────────────
    test("golden: encodeForSigning starts with TRANSACTION_SIGN prefix 53545800") {
        val json =
            """{"TransactionType":0,"Flags":0,"Sequence":1,"Fee":"12",""" +
                """"Account":"$accountA","Destination":"$accountB","Amount":"1000000"}"""
        val forSigning = BinaryCodec.encodeForSigning(json)
        forSigning shouldStartWith "53545800"
    }

    test("golden: encodeForSigning excludes TxnSignature — body equals signing of tx without signature") {
        val jsonNoSig =
            """{"TransactionType":0,"Flags":0,"Sequence":1,"Fee":"12",""" +
                """"Account":"$accountA","Destination":"$accountB","Amount":"1000000"}"""
        val jsonWithSig =
            """{"TransactionType":0,"Flags":0,"Sequence":1,"Fee":"12",""" +
                """"Account":"$accountA","Destination":"$accountB","Amount":"1000000","TxnSignature":"DEADBEEF"}"""
        BinaryCodec.encodeForSigning(jsonNoSig) shouldBe BinaryCodec.encodeForSigning(jsonWithSig)
    }

    test("golden: encodeForMultiSigning starts with 534d5400 and ends with signer account ID") {
        val json =
            """{"TransactionType":0,"Flags":0,"Sequence":1,"Fee":"12",""" +
                """"Account":"$accountA","Destination":"$accountB","Amount":"1000000"}"""
        val signerHex = "0000000000000000000000000000000000000099"
        val result = BinaryCodec.encodeForMultiSigning(json, signerHex)
        result shouldStartWith "534d5400"
        result.takeLast(40) shouldBe signerHex
    }

    test("golden: encodeQuality / decodeQuality roundtrip — integer quality") {
        val q = "1000000"
        BinaryCodec.decodeQuality(BinaryCodec.encodeQuality(q)) shouldBe q
    }

    test("golden: encodeQuality / decodeQuality roundtrip — fractional quality") {
        val q = "195796912.5171664"
        BinaryCodec.decodeQuality(BinaryCodec.encodeQuality(q)) shouldBe q
    }

    test("golden: encodeQuality zero") {
        BinaryCodec.encodeQuality("0") shouldBe "0000000000000000"
    }
})

private data class GoldenVector(
    val name: String,
    val json: String,
)
