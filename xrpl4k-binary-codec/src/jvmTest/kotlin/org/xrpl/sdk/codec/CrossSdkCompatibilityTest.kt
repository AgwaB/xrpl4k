package org.xrpl.sdk.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Cross-SDK compatibility tests that verify our binary codec produces output
 * consistent with xrpl4j's codec-fixtures.json.
 *
 * Strategy:
 *  - The fixture binary values are canonical (from the XRPL network).
 *  - For every fixture we:
 *      1. Decode the binary hex and verify key structural fields match expected.
 *      2. Re-encode the decoded output and verify the bytes are identical (roundtrip).
 *  - "TransactionType" and "LedgerEntryType" fields decode as integer codes in our
 *    codec, not string names. We compare against the integer code from definitions.
 *  - All transaction types including XChain are fully tested (VL-encoding bug fixed).
 *  - Encoding tests (JSON → binary) are not performed for xrpl4j fixtures because
 *    those fixtures use base58 r-addresses; our codec expects raw 20-byte hex account IDs.
 *
 * Fixture source: xrpl4j/xrpl4j-core/src/test/resources/codec-fixtures.json
 * (a representative subset is stored under fixtures/cross-sdk/).
 */
@Suppress("LargeClass", "TooManyFunctions")
class CrossSdkCompatibilityTest : FunSpec({

    // ── fixture loading ──────────────────────────────────────────────────────

    val fixtureJson: JsonObject by lazy {
        val text =
            CrossSdkCompatibilityTest::class.java
                .getResourceAsStream("/fixtures/cross-sdk/xrpl4j-codec-fixtures.json")
                ?.bufferedReader()
                ?.readText()
                ?: error("Could not load cross-sdk fixture file")
        Json.parseToJsonElement(text).jsonObject
    }

    val transactions: List<JsonObject> by lazy {
        fixtureJson["transactions"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
    }

    val accountStateEntries: List<JsonObject> by lazy {
        fixtureJson["accountState"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
    }

    // ── transaction type integer codes (from definitions.json) ───────────────
    //
    // Our codec decodes TransactionType as an integer code, not a string name.
    // These constants mirror the TRANSACTION_TYPES section of definitions.json.
    val txTypePayment = 0
    val txTypeAccountSet = 3
    val txTypeTrustSet = 20
    val txTypeOfferCreate = 7
    val txTypeOfferCancel = 8
    val txTypeEscrowCreate = 1
    val txTypeCheckCreate = 16
    val txTypeAmmCreate = 35
    val txTypeAmmDeposit = 36
    val txTypeAmmWithdraw = 37
    val txTypeAmmBid = 39
    val txTypeAmmVote = 38
    val txTypeDidSet = 49
    val txTypeDidDelete = 50
    val txTypeXchainCreateBridge = 48
    val txTypeXchainModifyBridge = 47
    val txTypeXchainCreateClaimId = 41
    val txTypeXchainCommit = 42
    val txTypeXchainClaim = 43
    val txTypeXchainAccountCreateCommit = 44
    val txTypeXchainAddAccountCreateAttestation = 46
    val txTypeXchainAddClaimAttestation = 45

    // ── ledger entry type integer codes ──────────────────────────────────────
    val letAccountRoot = 97
    val letRippleState = 114
    val letDirectoryNode = 100
    val letOffer = 111
    val letLedgerHashes = 104

    // ── transaction type name → integer code lookup ──────────────────────────
    val txTypeNameToCode =
        mapOf(
            "Payment" to txTypePayment,
            "AccountSet" to txTypeAccountSet,
            "TrustSet" to txTypeTrustSet,
            "OfferCreate" to txTypeOfferCreate,
            "OfferCancel" to txTypeOfferCancel,
            "EscrowCreate" to txTypeEscrowCreate,
            "CheckCreate" to txTypeCheckCreate,
            "AMMCreate" to txTypeAmmCreate,
            "AMMDeposit" to txTypeAmmDeposit,
            "AMMWithdraw" to txTypeAmmWithdraw,
            "AMMBid" to txTypeAmmBid,
            "AMMVote" to txTypeAmmVote,
            "DIDSet" to txTypeDidSet,
            "DIDDelete" to txTypeDidDelete,
            "XChainCreateBridge" to txTypeXchainCreateBridge,
            "XChainModifyBridge" to txTypeXchainModifyBridge,
            "XChainCreateClaimID" to txTypeXchainCreateClaimId,
            "XChainCommit" to txTypeXchainCommit,
            "XChainClaim" to txTypeXchainClaim,
            "XChainAccountCreateCommit" to txTypeXchainAccountCreateCommit,
            "XChainAddAccountCreateAttestation" to txTypeXchainAddAccountCreateAttestation,
            "XChainAddClaimAttestation" to txTypeXchainAddClaimAttestation,
        )

    // ── helpers ──────────────────────────────────────────────────────────────

    fun JsonObject.binary(): String = this["binary"]!!.jsonPrimitive.content.lowercase()

    fun JsonObject.expectedJson(): JsonObject = this["json"]!!.jsonObject

    fun decodeFixture(hex: String): JsonObject {
        val decoded = BinaryCodec.decode(hex)
        return Json.parseToJsonElement(decoded).jsonObject
    }

    // ── fixture count sanity ─────────────────────────────────────────────────

    test("fixture file loaded — transactions count is 24") {
        transactions.size shouldBe 24
    }

    test("fixture file loaded — accountState count is 10") {
        accountStateEntries.size shouldBe 10
    }

    // ── per-transaction tests ─────────────────────────────────────────────────
    //
    // For each of the 24 transaction fixtures we generate:
    //   - decode succeeds
    //   - TransactionType integer code matches
    //   - Flags match
    //   - Sequence matches
    //   - Fee matches
    //   - roundtrip encode(decode(binary)) == binary

    for ((index, fixture) in transactions.withIndex()) {
        val binary = fixture.binary()
        val expected = fixture.expectedJson()
        val txTypeName = expected["TransactionType"]?.jsonPrimitive?.content ?: "tx[$index]"
        val testLabel = "tx[$index] $txTypeName"

        test("$testLabel — decode produces valid JSON") {
            val decoded = decodeFixture(binary)
            decoded.size shouldNotBe 0
        }

        test("$testLabel — TransactionType code matches") {
            val decoded = decodeFixture(binary)
            val expectedCode = txTypeNameToCode[txTypeName]
            if (expectedCode != null) {
                // Decoded as Long (UInt16 → Long in some paths) or Int — normalise
                val decodedCode = decoded["TransactionType"]?.jsonPrimitive?.longOrNull
                decodedCode shouldBe expectedCode.toLong()
            }
        }

        test("$testLabel — Flags match") {
            val decoded = decodeFixture(binary)
            val expectedFlags = expected["Flags"]?.jsonPrimitive?.longOrNull
            if (expectedFlags != null) {
                decoded["Flags"]?.jsonPrimitive?.longOrNull shouldBe expectedFlags
            }
        }

        test("$testLabel — Sequence matches") {
            val decoded = decodeFixture(binary)
            val expectedSeq = expected["Sequence"]?.jsonPrimitive?.longOrNull
            if (expectedSeq != null) {
                decoded["Sequence"]?.jsonPrimitive?.longOrNull shouldBe expectedSeq
            }
        }

        test("$testLabel — Fee matches") {
            val decoded = decodeFixture(binary)
            val expectedFee = expected["Fee"]?.jsonPrimitive?.content
            if (expectedFee != null) {
                decoded["Fee"]?.jsonPrimitive?.content shouldBe expectedFee
            }
        }

        test("$testLabel — roundtrip encode(decode(binary)) == binary") {
            val decoded = BinaryCodec.decode(binary)
            val reEncoded = BinaryCodec.encode(decoded)
            reEncoded shouldBe binary
        }
    }

    // ── transaction-specific field tests ─────────────────────────────────────

    test("Payment — Amount is XRP drop string (all digits)") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "Payment"
            }
        fixture shouldNotBe null
        val decoded = decodeFixture(fixture!!.binary())
        val amount = decoded["Amount"]?.jsonPrimitive?.content
        amount shouldNotBe null
        amount!!.all { it.isDigit() } shouldBe true
    }

    test("Payment — Amount matches expected value") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "Payment"
            }
        fixture shouldNotBe null
        val decoded = decodeFixture(fixture!!.binary())
        val expectedAmount = fixture.expectedJson()["Amount"]?.jsonPrimitive?.content
        decoded["Amount"]?.jsonPrimitive?.content shouldBe expectedAmount
    }

    test("AMMCreate — TradingFee is present") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMCreate"
            }
        fixture shouldNotBe null
        val decoded = decodeFixture(fixture!!.binary())
        decoded["TradingFee"] shouldNotBe null
    }

    test("AMMCreate — TradingFee matches expected value") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMCreate"
            }
        fixture shouldNotBe null
        val decoded = decodeFixture(fixture!!.binary())
        val expectedFee = fixture.expectedJson()["TradingFee"]?.jsonPrimitive?.longOrNull
        decoded["TradingFee"]?.jsonPrimitive?.longOrNull shouldBe expectedFee
    }

    test("AMMCreate — Amount2 is IOU object with currency ETH") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMCreate"
            }
        fixture shouldNotBe null
        val decoded = decodeFixture(fixture!!.binary())
        val amount2 = decoded["Amount2"] as? JsonObject
        amount2 shouldNotBe null
        amount2!!["currency"]?.jsonPrimitive?.content shouldBe "ETH"
    }

    test("AMMDeposit fixtures (5 variants) — all roundtrip correctly") {
        val depositFixtures =
            transactions.filter {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMDeposit"
            }
        depositFixtures.size shouldBe 5
        for (fixture in depositFixtures) {
            val binary = fixture.binary()
            val reEncoded = BinaryCodec.encode(BinaryCodec.decode(binary))
            reEncoded shouldBe binary
        }
    }

    test("AMMWithdraw fixtures (5 variants) — all roundtrip correctly") {
        val withdrawFixtures =
            transactions.filter {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMWithdraw"
            }
        withdrawFixtures.size shouldBe 5
        for (fixture in withdrawFixtures) {
            val binary = fixture.binary()
            val reEncoded = BinaryCodec.encode(BinaryCodec.decode(binary))
            reEncoded shouldBe binary
        }
    }

    test("AMMVote — TradingFee is present") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMVote"
            }
        fixture shouldNotBe null
        val decoded = decodeFixture(fixture!!.binary())
        decoded["TradingFee"] shouldNotBe null
    }

    test("AMMBid — roundtrip is stable") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "AMMBid"
            }
        fixture shouldNotBe null
        val binary = fixture!!.binary()
        BinaryCodec.encode(BinaryCodec.decode(binary)) shouldBe binary
    }

    test("DIDSet — roundtrip is stable") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "DIDSet"
            }
        fixture shouldNotBe null
        val binary = fixture!!.binary()
        BinaryCodec.encode(BinaryCodec.decode(binary)) shouldBe binary
    }

    test("DIDDelete — roundtrip is stable") {
        val fixture =
            transactions.find {
                it.expectedJson()["TransactionType"]?.jsonPrimitive?.content == "DIDDelete"
            }
        fixture shouldNotBe null
        val binary = fixture!!.binary()
        BinaryCodec.encode(BinaryCodec.decode(binary)) shouldBe binary
    }

    test("all transactions decode without throwing") {
        val failures = mutableListOf<String>()
        for (fixture in transactions) {
            val txType = fixture.expectedJson()["TransactionType"]?.jsonPrimitive?.content ?: "unknown"
            try {
                BinaryCodec.decode(fixture.binary())
            } catch (e: Exception) {
                failures.add("$txType: ${e.message}")
            }
        }
        failures shouldBe emptyList()
    }

    // ── per-accountState tests ────────────────────────────────────────────────
    //
    // For each of the 10 accountState fixtures we generate:
    //   - decode succeeds
    //   - LedgerEntryType integer code matches
    //   - Flags match
    //   - roundtrip is stable

    val letNameToCode =
        mapOf(
            "AccountRoot" to letAccountRoot,
            "RippleState" to letRippleState,
            "DirectoryNode" to letDirectoryNode,
            "Offer" to letOffer,
            "LedgerHashes" to letLedgerHashes,
        )

    for ((index, fixture) in accountStateEntries.withIndex()) {
        val binary = fixture.binary()
        val expected = fixture.expectedJson()
        val entryTypeName = expected["LedgerEntryType"]?.jsonPrimitive?.content ?: "entry[$index]"
        val label = "accountState[$index] $entryTypeName"

        test("$label — decode produces valid JSON") {
            val decoded = decodeFixture(binary)
            decoded.size shouldNotBe 0
        }

        test("$label — LedgerEntryType code matches") {
            val decoded = decodeFixture(binary)
            val expectedCode = letNameToCode[entryTypeName]
            if (expectedCode != null) {
                decoded["LedgerEntryType"]?.jsonPrimitive?.longOrNull shouldBe expectedCode.toLong()
            }
        }

        test("$label — Flags match") {
            val decoded = decodeFixture(binary)
            val expectedFlags = expected["Flags"]?.jsonPrimitive?.longOrNull
            if (expectedFlags != null) {
                decoded["Flags"]?.jsonPrimitive?.longOrNull shouldBe expectedFlags
            }
        }

        test("$label — roundtrip encode(decode(binary)) == binary") {
            val decoded = BinaryCodec.decode(binary)
            val reEncoded = BinaryCodec.encode(decoded)
            reEncoded shouldBe binary
        }
    }

    // ── accountState type-specific field tests ────────────────────────────────

    test("AccountRoot fixtures — Balance is all-digit XRP drop string") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "AccountRoot"
            }
        fixtures.size shouldNotBe 0
        for (fixture in fixtures) {
            val decoded = decodeFixture(fixture.binary())
            val balance = decoded["Balance"]?.jsonPrimitive?.content
            balance shouldNotBe null
            balance!!.all { it.isDigit() } shouldBe true
        }
    }

    test("AccountRoot fixtures — Balance values match expected") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "AccountRoot"
            }
        for (fixture in fixtures) {
            val expected = fixture.expectedJson()["Balance"]?.jsonPrimitive?.content ?: continue
            val decoded = decodeFixture(fixture.binary())
            decoded["Balance"]?.jsonPrimitive?.content shouldBe expected
        }
    }

    test("AccountRoot fixtures — Sequence matches expected") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "AccountRoot"
            }
        for (fixture in fixtures) {
            val expectedSeq = fixture.expectedJson()["Sequence"]?.jsonPrimitive?.longOrNull ?: continue
            val decoded = decodeFixture(fixture.binary())
            decoded["Sequence"]?.jsonPrimitive?.longOrNull shouldBe expectedSeq
        }
    }

    test("RippleState fixtures — Balance is IOU object with a currency field") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "RippleState"
            }
        fixtures.size shouldNotBe 0
        for (fixture in fixtures) {
            val decoded = decodeFixture(fixture.binary())
            val balance = decoded["Balance"] as? JsonObject
            balance shouldNotBe null
            balance!!["currency"]?.jsonPrimitive?.content shouldNotBe null
        }
    }

    test("RippleState fixtures — LowLimit and HighLimit are IOU objects") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "RippleState"
            }
        fixtures.size shouldNotBe 0
        for (fixture in fixtures) {
            val decoded = decodeFixture(fixture.binary())
            (decoded["LowLimit"] as? JsonObject)?.get("currency")?.jsonPrimitive?.content shouldNotBe null
            (decoded["HighLimit"] as? JsonObject)?.get("currency")?.jsonPrimitive?.content shouldNotBe null
        }
    }

    test("RippleState fixtures — currency codes match expected LowLimit and HighLimit") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "RippleState"
            }
        for (fixture in fixtures) {
            val expectedLow =
                (fixture.expectedJson()["LowLimit"] as? JsonObject)
                    ?.get("currency")?.jsonPrimitive?.content ?: continue
            val expectedHigh =
                (fixture.expectedJson()["HighLimit"] as? JsonObject)
                    ?.get("currency")?.jsonPrimitive?.content ?: continue
            val decoded = decodeFixture(fixture.binary())
            (decoded["LowLimit"] as? JsonObject)?.get("currency")?.jsonPrimitive?.content shouldBe expectedLow
            (decoded["HighLimit"] as? JsonObject)?.get("currency")?.jsonPrimitive?.content shouldBe expectedHigh
        }
    }

    test("RippleState fixtures — Flags match expected values") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "RippleState"
            }
        for (fixture in fixtures) {
            val expectedFlags = fixture.expectedJson()["Flags"]?.jsonPrimitive?.longOrNull ?: continue
            val decoded = decodeFixture(fixture.binary())
            decoded["Flags"]?.jsonPrimitive?.longOrNull shouldBe expectedFlags
        }
    }

    test("DirectoryNode fixtures — Indexes is a non-empty array") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "DirectoryNode"
            }
        fixtures.size shouldNotBe 0
        for (fixture in fixtures) {
            val decoded = decodeFixture(fixture.binary())
            val indexes = decoded["Indexes"] as? JsonArray
            indexes shouldNotBe null
            indexes!!.size shouldNotBe 0
        }
    }

    test("DirectoryNode fixtures — Indexes count matches expected") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "DirectoryNode"
            }
        for (fixture in fixtures) {
            val expectedCount = (fixture.expectedJson()["Indexes"] as? JsonArray)?.size ?: continue
            val decoded = decodeFixture(fixture.binary())
            (decoded["Indexes"] as? JsonArray)?.size shouldBe expectedCount
        }
    }

    test("Offer fixtures — TakerPays and TakerGets are present") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "Offer"
            }
        fixtures.size shouldNotBe 0
        for (fixture in fixtures) {
            val decoded = decodeFixture(fixture.binary())
            decoded["TakerPays"] shouldNotBe null
            decoded["TakerGets"] shouldNotBe null
        }
    }

    test("Offer fixtures — Sequence matches expected") {
        val fixtures =
            accountStateEntries.filter {
                it.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content == "Offer"
            }
        for (fixture in fixtures) {
            val expectedSeq = fixture.expectedJson()["Sequence"]?.jsonPrimitive?.longOrNull ?: continue
            val decoded = decodeFixture(fixture.binary())
            decoded["Sequence"]?.jsonPrimitive?.longOrNull shouldBe expectedSeq
        }
    }

    test("all accountState entries decode without throwing") {
        val failures = mutableListOf<String>()
        for (fixture in accountStateEntries) {
            val entryType = fixture.expectedJson()["LedgerEntryType"]?.jsonPrimitive?.content ?: "unknown"
            try {
                BinaryCodec.decode(fixture.binary())
            } catch (e: Exception) {
                failures.add("$entryType: ${e.message}")
            }
        }
        failures shouldBe emptyList()
    }

    // ── ledgerData tests ─────────────────────────────────────────────────────

    test("ledgerData — decodeLedgerData produces expected header fields") {
        val ledgerEntries = fixtureJson["ledgerData"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
        if (ledgerEntries.isNotEmpty()) {
            val binary = ledgerEntries.first().binary()
            val decoded = BinaryCodec.decodeLedgerData(binary)
            val obj = Json.parseToJsonElement(decoded).jsonObject
            obj["ledger_index"] shouldNotBe null
            obj["total_coins"] shouldNotBe null
            obj["parent_hash"] shouldNotBe null
            obj["transaction_hash"] shouldNotBe null
            obj["account_hash"] shouldNotBe null
            obj["close_time"] shouldNotBe null
            obj["close_flags"] shouldNotBe null
        }
    }
})
