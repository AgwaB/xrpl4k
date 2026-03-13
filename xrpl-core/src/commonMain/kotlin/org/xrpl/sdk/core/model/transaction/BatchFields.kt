package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.XrplDsl

// ── Batch ─────────────────────────────────────────────────────────────────────

/**
 * Fields specific to a Batch transaction.
 *
 * @property rawTransactions The list of raw transaction maps to execute atomically.
 * @property flags Flags controlling batch behavior.
 */
@ExperimentalXrplApi
public class BatchFields(
    public val rawTransactions: List<Map<String, Any?>>,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BatchFields) return false
        return rawTransactions == other.rawTransactions &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = rawTransactions.hashCode()
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "BatchFields(rawTransactions=$rawTransactions, flags=$flags)"
}

/**
 * DSL builder for [BatchFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class BatchBuilder internal constructor() {
    /** The list of raw transaction maps to execute atomically. Required. */
    public var rawTransactions: List<Map<String, Any?>> = emptyList()

    /** Flags controlling batch behavior. */
    public var flags: UInt? = null

    internal fun build(): BatchFields {
        require(rawTransactions.isNotEmpty()) { "rawTransactions must not be empty" }
        return BatchFields(rawTransactions = rawTransactions, flags = flags)
    }
}
