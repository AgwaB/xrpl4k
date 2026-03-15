@file:Suppress("MagicNumber")

package org.xrpl.sdk.client.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.xrpl.sdk.client.model.AccountTxEntry
import org.xrpl.sdk.client.model.ValidatedTransaction

/** tfPartialPayment = 0x00020000 = 131072 */
private const val TF_PARTIAL_PAYMENT = 131072L

class PartialPaymentTest : FunSpec({

    // ── Non-payment transaction ───────────────────────────────────

    test("non-payment transaction is not partial") {
        val tx =
            ValidatedTransaction(
                txJson =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("OfferCreate"),
                            "Flags" to JsonPrimitive(TF_PARTIAL_PAYMENT),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("999")),
                    ),
            )
        isPartialPayment(tx) shouldBe false
    }

    // ── Payment without tfPartialPayment flag ─────────────────────

    test("payment without tfPartialPayment flag is not partial") {
        val tx =
            ValidatedTransaction(
                txJson =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(0L),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("500000")),
                    ),
            )
        isPartialPayment(tx) shouldBe false
    }

    // ── Payment with tfPartialPayment and delivered_amount != Amount ──

    test("payment with tfPartialPayment and delivered_amount != Amount is partial") {
        val tx =
            ValidatedTransaction(
                txJson =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(TF_PARTIAL_PAYMENT),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("500000")),
                    ),
            )
        isPartialPayment(tx) shouldBe true
    }

    // ── Payment with tfPartialPayment and delivered_amount == Amount ──

    test("payment with tfPartialPayment and delivered_amount == Amount is not partial") {
        val tx =
            ValidatedTransaction(
                txJson =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(TF_PARTIAL_PAYMENT),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("1000000")),
                    ),
            )
        isPartialPayment(tx) shouldBe false
    }

    // ── Extension property on ValidatedTransaction ────────────────

    test("isPartialPayment extension property works on ValidatedTransaction") {
        val partialTx =
            ValidatedTransaction(
                txJson =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(TF_PARTIAL_PAYMENT),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("200000")),
                    ),
            )
        partialTx.isPartialPayment shouldBe true

        val normalTx =
            ValidatedTransaction(
                txJson =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(0L),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("1000000")),
                    ),
            )
        normalTx.isPartialPayment shouldBe false
    }

    // ── Extension property on AccountTxEntry ──────────────────────

    test("isPartialPayment extension property works on AccountTxEntry") {
        val partialEntry =
            AccountTxEntry(
                tx =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(TF_PARTIAL_PAYMENT),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("300000")),
                    ),
                validated = true,
            )
        partialEntry.isPartialPayment shouldBe true

        val normalEntry =
            AccountTxEntry(
                tx =
                    JsonObject(
                        mapOf(
                            "TransactionType" to JsonPrimitive("Payment"),
                            "Amount" to JsonPrimitive("1000000"),
                            "Flags" to JsonPrimitive(TF_PARTIAL_PAYMENT),
                        ),
                    ),
                meta =
                    JsonObject(
                        mapOf("delivered_amount" to JsonPrimitive("1000000")),
                    ),
                validated = true,
            )
        normalEntry.isPartialPayment shouldBe false
    }
})
