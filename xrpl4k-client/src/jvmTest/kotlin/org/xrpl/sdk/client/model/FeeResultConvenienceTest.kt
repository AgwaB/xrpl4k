@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.XrpDrops

class FeeResultConvenienceTest : FunSpec({

    val drops =
        FeeDrops(
            baseFee = XrpDrops(10),
            medianFee = XrpDrops(5000),
            minimumFee = XrpDrops(10),
            openLedgerFee = XrpDrops(12),
        )

    val feeResult =
        FeeResult(
            currentLedgerSize = "56",
            currentQueueSize = "0",
            drops = drops,
            expectedLedgerSize = "55",
            ledgerCurrentIndex = null,
            maxQueueSize = "1120",
        )

    // ── baseFee convenience property ─────────────────────────────────────────

    test("baseFee delegates to drops.baseFee") {
        feeResult.baseFee shouldBe drops.baseFee
        feeResult.baseFee shouldBe XrpDrops(10)
    }

    // ── medianFee convenience property ───────────────────────────────────────

    test("medianFee delegates to drops.medianFee") {
        feeResult.medianFee shouldBe drops.medianFee
        feeResult.medianFee shouldBe XrpDrops(5000)
    }

    // ── minimumFee convenience property ──────────────────────────────────────

    test("minimumFee delegates to drops.minimumFee") {
        feeResult.minimumFee shouldBe drops.minimumFee
        feeResult.minimumFee shouldBe XrpDrops(10)
    }

    // ── openLedgerFee convenience property ───────────────────────────────────

    test("openLedgerFee delegates to drops.openLedgerFee") {
        feeResult.openLedgerFee shouldBe XrpDrops(12)
    }

    // ── null drops fields ────────────────────────────────────────────────────

    context("null fee drops") {
        val nullDrops =
            FeeDrops(
                baseFee = null,
                medianFee = null,
                minimumFee = null,
                openLedgerFee = null,
            )

        val nullResult =
            FeeResult(
                currentLedgerSize = null,
                currentQueueSize = null,
                drops = nullDrops,
                expectedLedgerSize = null,
                ledgerCurrentIndex = null,
                maxQueueSize = null,
            )

        test("baseFee is null when drops.baseFee is null") {
            nullResult.baseFee shouldBe null
        }

        test("medianFee is null when drops.medianFee is null") {
            nullResult.medianFee shouldBe null
        }

        test("minimumFee is null when drops.minimumFee is null") {
            nullResult.minimumFee shouldBe null
        }

        test("openLedgerFee defaults to 0 drops when drops.openLedgerFee is null") {
            nullResult.openLedgerFee shouldBe XrpDrops(0)
        }
    }
})
