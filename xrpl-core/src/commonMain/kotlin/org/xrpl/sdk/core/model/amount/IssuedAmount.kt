package org.xrpl.sdk.core.model.amount

import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode

/**
 * An amount of an IOU (issued currency) on the XRPL.
 *
 * The [value] is stored as a decimal string to preserve precision.
 * XRPL IOU amounts support up to 16 significant digits.
 */
public class IssuedAmount(
    /** The currency code. */
    public val currency: CurrencyCode,
    /** The issuer's classic address. */
    public val issuer: Address,
    /** The decimal string value (e.g., "1.5", "1000", "1.23e5"). */
    public val value: String,
) : CurrencyAmount {
    override fun equals(other: Any?): Boolean =
        other is IssuedAmount &&
            currency == other.currency &&
            issuer == other.issuer &&
            value == other.value

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String = "$value ${currency.value}(${issuer.value})"
}
