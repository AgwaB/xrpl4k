@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

/**
 * T19 — Property-based tests.
 *
 * Exercises the BinaryCodec public API with randomly generated inputs.
 * All numeric write/read roundtrips are validated through BinaryCodec helpers
 * or via the full encode/decode path to avoid depending on internal classes.
 *
 * Roundtrip property: encode(decode(encode(json))) == encode(json)
 *
 * The canonical hex account IDs (40 chars = 20 bytes) are fixed so every
 * generated transaction is structurally valid.
 */
class PropertyTest : FunSpec({

    val accountA = "0000000000000000000000000000000000000001"
    val accountB = "0000000000000000000000000000000000000002"

    // ── XRP Amount roundtrip ──────────────────────────────────────────

    test("property: XRP drops roundtrip for range 1..MAX_XRP_DROPS") {
        checkAll(1000, Arb.long(1L, 100_000_000_000_000_000L)) { drops ->
            val json = xrpPayment(accountA, accountB, drops.toString(), seq = 1)
            val hex = BinaryCodec.encode(json)
            val reEncoded = BinaryCodec.encode(BinaryCodec.decode(hex))
            reEncoded shouldBe hex
        }
    }

    test("property: XRP drops roundtrip — small values 1..1000") {
        checkAll(500, Arb.long(1L, 1_000L)) { drops ->
            val json = xrpPayment(accountA, accountB, drops.toString(), seq = 1)
            val hex = BinaryCodec.encode(json)
            BinaryCodec.encode(BinaryCodec.decode(hex)) shouldBe hex
        }
    }

    // ── Sequence number roundtrip ─────────────────────────────────────

    test("property: Sequence number roundtrip over full UInt32 range") {
        // Sequence is a UInt32 (0..4294967295); Long used here to cover the full range
        checkAll(1000, Arb.long(0L, 4_294_967_295L)) { seq ->
            val json = xrpPayment(accountA, accountB, "1000000", seq = seq)
            val hex = BinaryCodec.encode(json)
            BinaryCodec.encode(BinaryCodec.decode(hex)) shouldBe hex
        }
    }

    // ── Fee roundtrip ───────────────────────────────────────────────

    test("property: Fee (XRP drops string) roundtrip for valid range") {
        checkAll(1000, Arb.long(1L, 1_000_000L)) { fee ->
            val json = xrpPayment(accountA, accountB, "1000000", seq = 1, fee = fee.toString())
            val hex = BinaryCodec.encode(json)
            BinaryCodec.encode(BinaryCodec.decode(hex)) shouldBe hex
        }
    }

    // ── Flags roundtrip (UInt32) ──────────────────────────────────────

    test("property: Flags (UInt32) roundtrip") {
        checkAll(1000, Arb.long(0L, 4_294_967_295L)) { flags ->
            val json = xrpPayment(accountA, accountB, "1000000", seq = 1, flags = flags)
            val hex = BinaryCodec.encode(json)
            BinaryCodec.encode(BinaryCodec.decode(hex)) shouldBe hex
        }
    }

    // ── encodeQuality / decodeQuality roundtrip ───────────────────────

    test("property: encodeQuality output is always valid 16-char lowercase hex") {
        // encodeQuality normalises the mantissa/exponent so simple string roundtrip
        // is not guaranteed (e.g. "1000000000000000" -> decoded as "1", re-encodes
        // with a different exponent byte). The safe invariant is format correctness.
        checkAll(1000, Arb.long(1L, 1_000_000_000_000_000L)) { v ->
            val encoded = BinaryCodec.encodeQuality(v.toString())
            encoded.length shouldBe 16
            encoded.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
        }
    }

    test("property: decodeQuality output re-encodes to same hex (idempotent from decoded form)") {
        // For values already in decoded/normalised form (produced by decodeQuality),
        // a second encode/decode cycle must be stable.
        // We seed with small integers that have no trailing zeros.
        val noTrailingZeros = Arb.long(1L, 9_999_999L).filter { it % 10 != 0L }
        checkAll(500, noTrailingZeros) { v ->
            val decoded1 = BinaryCodec.decodeQuality(BinaryCodec.encodeQuality(v.toString()))
            val decoded2 = BinaryCodec.decodeQuality(BinaryCodec.encodeQuality(decoded1))
            decoded2 shouldBe decoded1
        }
    }

    test("property: encodeQuality output is always 16 hex chars (8 bytes)") {
        checkAll(500, Arb.long(1L, 999_999_999_999_999L)) { v ->
            val encoded = BinaryCodec.encodeQuality(v.toString())
            encoded.length shouldBe 16
        }
    }

    // ── Hash256 roundtrip via encodeForSigningClaim ───────────────────

    test("property: encodeForSigningClaim output length is always 88 hex chars") {
        // 4-byte prefix + 32-byte channel + 8-byte amount = 44 bytes = 88 hex chars
        checkAll(500, Arb.byteArray(Arb.constant(32), Arb.byte())) { channelBytes ->
            val channelHex = channelBytes.toHexString()
            val claimJson = """{"Channel":"$channelHex","Amount":"1000000"}"""
            val encoded = BinaryCodec.encodeForSigningClaim(claimJson)
            encoded.length shouldBe 88
        }
    }

    // ── OfferSequence roundtrip (UInt32 field) ────────────────────────

    test("property: OfferSequence roundtrip over full UInt32 range") {
        checkAll(1000, Arb.long(1L, 4_294_967_295L)) { offerSeq ->
            val json = offerCancelJson(accountA, offerSeq)
            val hex = BinaryCodec.encode(json)
            BinaryCodec.encode(BinaryCodec.decode(hex)) shouldBe hex
        }
    }

    // ── SetFlag roundtrip (UInt32 field) ──────────────────────────────

    test("property: SetFlag roundtrip for AccountSet") {
        checkAll(500, Arb.int(0, 16)) { flag ->
            val json = accountSetJson(accountA, flag)
            val hex = BinaryCodec.encode(json)
            BinaryCodec.encode(BinaryCodec.decode(hex)) shouldBe hex
        }
    }
})

// ── helpers ──────────────────────────────────────────────────────────────

private fun xrpPayment(
    account: String,
    destination: String,
    amount: String,
    seq: Long,
    fee: String = "12",
    flags: Long = 0L,
): String =
    """{"TransactionType":0,"Flags":$flags,""" +
        """"Sequence":$seq,"Fee":"$fee",""" +
        """"Account":"$account",""" +
        """"Destination":"$destination",""" +
        """"Amount":"$amount"}"""

private fun offerCancelJson(
    account: String,
    offerSeq: Long,
): String =
    """{"TransactionType":8,"Flags":0,""" +
        """"Sequence":1,"Fee":"12",""" +
        """"Account":"$account",""" +
        """"OfferSequence":$offerSeq}"""

private fun accountSetJson(
    account: String,
    flag: Int,
): String =
    """{"TransactionType":3,"Flags":0,""" +
        """"Sequence":1,"Fee":"12",""" +
        """"Account":"$account",""" +
        """"SetFlag":$flag}"""

private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
