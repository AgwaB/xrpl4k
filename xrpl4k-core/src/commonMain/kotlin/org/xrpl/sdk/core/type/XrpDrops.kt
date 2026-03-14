package org.xrpl.sdk.core.type

import kotlin.jvm.JvmInline

/**
 * An amount of XRP expressed in drops (1 XRP = 1,000,000 drops).
 *
 * Drops are the smallest indivisible unit of XRP. This value class enforces non-negativity
 * and provides arithmetic operators that preserve type safety.
 *
 * @property value The number of drops. Must be non-negative.
 */
@JvmInline
public value class XrpDrops(public val value: Long) : Comparable<XrpDrops> {
    init {
        require(value >= 0) {
            "XRP drops must be non-negative. Got $value. " +
                "Use a value >= 0."
        }
    }

    /**
     * Adds two [XrpDrops] amounts.
     */
    public operator fun plus(other: XrpDrops): XrpDrops = XrpDrops(value + other.value)

    /**
     * Subtracts another [XrpDrops] amount from this one.
     *
     * @throws IllegalArgumentException if the result would be negative.
     */
    public operator fun minus(other: XrpDrops): XrpDrops = XrpDrops(value - other.value)

    override fun compareTo(other: XrpDrops): Int = value.compareTo(other.value)

    /**
     * Converts this drops amount to a human-readable XRP string.
     *
     * Examples:
     * - `XrpDrops(0).toXrp()` returns `"0"`
     * - `XrpDrops(1).toXrp()` returns `"0.000001"`
     * - `XrpDrops(1_000_000).toXrp()` returns `"1"`
     * - `XrpDrops(1_500_000).toXrp()` returns `"1.5"`
     * - `XrpDrops(123_456_789).toXrp()` returns `"123.456789"`
     */
    public fun toXrp(): String {
        val wholePart = value / DROPS_PER_XRP
        val fractionalPart = value % DROPS_PER_XRP
        return if (fractionalPart == 0L) {
            wholePart.toString()
        } else {
            val fractionalStr = fractionalPart.toString().padStart(6, '0').trimEnd('0')
            "$wholePart.$fractionalStr"
        }
    }

    public companion object {
        /** Number of drops in one XRP. */
        private const val DROPS_PER_XRP: Long = 1_000_000L

        /** Maximum total XRP supply expressed in drops (100 billion XRP). */
        public const val MAX_DROPS: Long = 100_000_000_000_000_000L
    }
}
