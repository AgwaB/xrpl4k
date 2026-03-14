package org.xrpl.sdk.core.model.amount

import org.xrpl.sdk.core.type.XrpDrops

/**
 * An amount of native XRP, represented in drops.
 */
public class XrpAmount(
    /** The amount in drops (1 XRP = 1,000,000 drops). */
    public val drops: XrpDrops,
) : CurrencyAmount {
    /** Adds two XRP amounts. */
    public operator fun plus(other: XrpAmount): XrpAmount = XrpAmount(drops + other.drops)

    /** Subtracts an XRP amount. */
    public operator fun minus(other: XrpAmount): XrpAmount = XrpAmount(drops - other.drops)

    /** Compares two XRP amounts by their drop values. */
    public operator fun compareTo(other: XrpAmount): Int = drops.compareTo(other.drops)

    override fun equals(other: Any?): Boolean = other is XrpAmount && drops == other.drops

    override fun hashCode(): Int = drops.hashCode()

    override fun toString(): String = "${drops.toXrp()} XRP"
}
