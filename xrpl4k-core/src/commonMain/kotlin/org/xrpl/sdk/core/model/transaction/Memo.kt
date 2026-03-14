package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl

/**
 * A memo attached to an XRPL transaction.
 *
 * All fields are hex-encoded strings. At least one field must be set.
 *
 * @property memoData The memo data (hex-encoded).
 * @property memoType The memo type (hex-encoded, e.g., "text/plain" as hex).
 * @property memoFormat The memo format (hex-encoded).
 */
public class Memo(
    public val memoData: String? = null,
    public val memoType: String? = null,
    public val memoFormat: String? = null,
) {
    init {
        require(memoData != null || memoType != null || memoFormat != null) {
            "At least one memo field must be set."
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Memo) return false
        return memoData == other.memoData &&
            memoType == other.memoType &&
            memoFormat == other.memoFormat
    }

    override fun hashCode(): Int {
        var result = memoData?.hashCode() ?: 0
        result = 31 * result + (memoType?.hashCode() ?: 0)
        result = 31 * result + (memoFormat?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Memo(memoData=$memoData, memoType=$memoType, memoFormat=$memoFormat)"
}

/**
 * DSL builder for [Memo].
 */
@XrplDsl
public class MemoBuilder internal constructor() {
    /** The memo data (hex-encoded). */
    public var memoData: String? = null

    /** The memo type (hex-encoded). */
    public var memoType: String? = null

    /** The memo format (hex-encoded). */
    public var memoFormat: String? = null

    internal fun build(): Memo =
        Memo(
            memoData = memoData,
            memoType = memoType,
            memoFormat = memoFormat,
        )
}

/**
 * Creates a [Memo] using the DSL builder.
 */
public fun memo(block: MemoBuilder.() -> Unit): Memo = MemoBuilder().apply(block).build()
