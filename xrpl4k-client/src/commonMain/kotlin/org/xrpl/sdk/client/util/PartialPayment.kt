@file:Suppress("MagicNumber")
@file:JvmName("PartialPaymentUtils")

package org.xrpl.sdk.client.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.xrpl.sdk.client.model.AccountTxEntry
import org.xrpl.sdk.client.model.ValidatedTransaction
import org.xrpl.sdk.core.model.transaction.TransactionFlags

/**
 * The `tfPartialPayment` flag value used to detect partial payments.
 */
private const val TF_PARTIAL_PAYMENT: UInt = TransactionFlags.Payment.tfPartialPayment

/**
 * Checks whether a validated transaction is a partial payment.
 *
 * A transaction is considered a partial payment when all of the following are true:
 * 1. The `TransactionType` is `"Payment"`.
 * 2. The `Flags` field includes the `tfPartialPayment` flag (`0x00020000`).
 * 3. The `delivered_amount` in metadata differs from the `Amount` in the transaction.
 *
 * This mirrors the partial-payment detection logic in xrpl.js.
 *
 * @param tx The validated transaction to check.
 * @return `true` if the transaction is a partial payment, `false` otherwise.
 */
public fun isPartialPayment(tx: ValidatedTransaction): Boolean {
    val txJson = tx.txJson?.asJsonObjectOrNull() ?: return false
    val meta = (tx.meta ?: tx.metadata)?.asJsonObjectOrNull() ?: return false
    return isPartialPaymentInternal(txJson, meta)
}

/**
 * Checks whether an account transaction entry represents a partial payment.
 *
 * @param entry The account transaction entry to check.
 * @return `true` if the transaction is a partial payment, `false` otherwise.
 */
public fun isPartialPayment(entry: AccountTxEntry): Boolean {
    val txJson = entry.tx?.asJsonObjectOrNull() ?: return false
    val meta = entry.meta?.asJsonObjectOrNull() ?: return false
    return isPartialPaymentInternal(txJson, meta)
}

/**
 * Extension property that checks if this [ValidatedTransaction] is a partial payment.
 */
@get:JvmName("isPartialPaymentProperty")
public val ValidatedTransaction.isPartialPayment: Boolean
    get() = isPartialPayment(this)

/**
 * Extension property that checks if this [AccountTxEntry] is a partial payment.
 */
@get:JvmName("isPartialPaymentEntryProperty")
public val AccountTxEntry.isPartialPayment: Boolean
    get() = isPartialPayment(this)

/**
 * Core detection logic shared by both overloads.
 */
private fun isPartialPaymentInternal(
    txJson: JsonObject,
    meta: JsonObject,
): Boolean {
    // 1. Must be a Payment
    val txType = txJson["TransactionType"]?.jsonPrimitive?.content ?: return false
    if (txType != "Payment") return false

    // 2. Must have tfPartialPayment flag set
    val flags = txJson["Flags"]?.jsonPrimitive?.longOrNull?.toUInt() ?: return false
    if (flags and TF_PARTIAL_PAYMENT == 0u) return false

    // 3. delivered_amount must differ from Amount
    val amount = txJson["Amount"] ?: return false
    val deliveredAmount =
        meta["delivered_amount"]
            ?: meta["DeliveredAmount"]
            ?: return true // If delivered_amount is missing, it's a partial payment with unknown delivery

    return amount != deliveredAmount
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
    try {
        jsonObject
    } catch (_: IllegalArgumentException) {
        null
    }
