package org.xrpl.sdk.codec.hashing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.crypto.platformCryptoProvider

class LedgerHashingTest : FunSpec({

    val provider = platformCryptoProvider()
    val zeroHash = "0".repeat(64)

    // ── hashLedgerHeader ─────────────────────────────────────────────

    context("hashLedgerHeader") {
        test("produces a 64-character lowercase hex string") {
            val hash = hashLedgerHeader(
                ledgerSequence = 1,
                totalCoins = 100_000_000_000_000_000L,
                parentHash = zeroHash,
                transactionHash = zeroHash,
                stateHash = zeroHash,
                parentCloseTime = 0,
                closeTime = 0,
                closeTimeResolution = 10,
                closeFlags = 0,
                provider = provider,
            )
            hash.length shouldBe 64
            hash.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
        }

        test("is deterministic for same inputs") {
            val hash1 = hashLedgerHeader(1, 100_000_000_000_000_000L, zeroHash, zeroHash, zeroHash, 0, 0, 10, 0, provider)
            val hash2 = hashLedgerHeader(1, 100_000_000_000_000_000L, zeroHash, zeroHash, zeroHash, 0, 0, 10, 0, provider)
            hash1 shouldBe hash2
        }

        test("different ledger sequence produces different hash") {
            val hash1 = hashLedgerHeader(
                ledgerSequence = 1,
                totalCoins = 100_000_000_000_000_000L,
                parentHash = zeroHash,
                transactionHash = zeroHash,
                stateHash = zeroHash,
                parentCloseTime = 0,
                closeTime = 0,
                closeTimeResolution = 10,
                closeFlags = 0,
                provider = provider,
            )
            val hash2 = hashLedgerHeader(
                ledgerSequence = 2,
                totalCoins = 100_000_000_000_000_000L,
                parentHash = zeroHash,
                transactionHash = zeroHash,
                stateHash = zeroHash,
                parentCloseTime = 0,
                closeTime = 0,
                closeTimeResolution = 10,
                closeFlags = 0,
                provider = provider,
            )
            hash1 shouldNotBe hash2
        }

        test("different totalCoins produces different hash") {
            val hash1 = hashLedgerHeader(1, 100L, zeroHash, zeroHash, zeroHash, 0, 0, 10, 0, provider)
            val hash2 = hashLedgerHeader(1, 200L, zeroHash, zeroHash, zeroHash, 0, 0, 10, 0, provider)
            hash1 shouldNotBe hash2
        }

        test("different closeFlags produces different hash") {
            val hash1 = hashLedgerHeader(1, 100L, zeroHash, zeroHash, zeroHash, 0, 0, 10, 0, provider)
            val hash2 = hashLedgerHeader(1, 100L, zeroHash, zeroHash, zeroHash, 0, 0, 10, 1, provider)
            hash1 shouldNotBe hash2
        }
    }

    // ── hashTxTree ───────────────────────────────────────────────────

    context("hashTxTree") {
        test("empty transaction list produces all-zeros hash") {
            hashTxTree(emptyList(), provider) shouldBe zeroHash
        }

        test("single transaction produces non-zero hash") {
            val txBlob = "1200002400000001"
            val metaBlob = "201C00000000"
            val hash = hashTxTree(listOf(txBlob to metaBlob), provider)
            hash shouldNotBe zeroHash
            hash.length shouldBe 64
        }

        test("is deterministic") {
            val txs = listOf("AABB" to "CCDD", "EEFF" to "0011")
            hashTxTree(txs, provider) shouldBe hashTxTree(txs, provider)
        }

        test("different transactions produce different hashes") {
            val hash1 = hashTxTree(listOf("AABB" to "CCDD"), provider)
            val hash2 = hashTxTree(listOf("EEFF" to "0011"), provider)
            hash1 shouldNotBe hash2
        }
    }

    // ── hashStateTree ────────────────────────────────────────────────

    context("hashStateTree") {
        test("empty state list produces all-zeros hash") {
            hashStateTree(emptyList(), provider) shouldBe zeroHash
        }

        test("single entry produces non-zero hash") {
            val index = "A".repeat(64)
            val hash = hashStateTree(listOf(index to "DEADBEEF"), provider)
            hash shouldNotBe zeroHash
            hash.length shouldBe 64
        }

        test("is deterministic") {
            val entries = listOf("A".repeat(64) to "AABB", "B".repeat(64) to "CCDD")
            hashStateTree(entries, provider) shouldBe hashStateTree(entries, provider)
        }

        test("order of entries does not affect hash") {
            val entry1 = "1".padEnd(64, '0') to "AABB"
            val entry2 = "2".padEnd(64, '0') to "CCDD"
            val hash1 = hashStateTree(listOf(entry1, entry2), provider)
            val hash2 = hashStateTree(listOf(entry2, entry1), provider)
            hash1 shouldBe hash2
        }
    }
})
