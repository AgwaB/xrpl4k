package org.xrpl.sdk.core.model.amount

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode

/**
 * Specifies a currency for order book queries without an amount.
 *
 * Use [Xrp] for native XRP or [Issued] for IOU tokens.
 *
 * ```kotlin
 * val xrp = CurrencySpec.Xrp
 * val usd = CurrencySpec.Issued(CurrencyCode("USD"), issuerAddress)
 * ```
 */
public sealed interface CurrencySpec {
    /** Native XRP currency. */
    public data object Xrp : CurrencySpec

    /**
     * An issued (IOU) currency identified by its code and issuer.
     *
     * @property currency The currency code.
     * @property issuer The issuer's classic address.
     */
    public data class Issued(
        val currency: CurrencyCode,
        val issuer: Address,
    ) : CurrencySpec
}

/**
 * Converts this [CurrencySpec] to the JSON representation expected by the XRPL JSON-RPC API.
 *
 * - [CurrencySpec.Xrp] produces `{"currency": "XRP"}`.
 * - [CurrencySpec.Issued] produces `{"currency": "<code>", "issuer": "<address>"}`.
 */
public fun CurrencySpec.toJson(): JsonElement =
    when (this) {
        is CurrencySpec.Xrp -> buildJsonObject { put("currency", "XRP") }
        is CurrencySpec.Issued ->
            buildJsonObject {
                put("currency", currency.value)
                put("issuer", issuer.value)
            }
    }
