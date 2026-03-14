package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

// ── DelegateSet ───────────────────────────────────────────────────────────────

/**
 * Fields specific to a DelegateSet transaction.
 *
 * @property authorize The account to delegate permissions to.
 * @property permissions The list of permission identifiers to delegate.
 * @property flags Flags controlling delegation behavior.
 */
@ExperimentalXrplApi
public class DelegateSetFields(
    public val authorize: Address,
    public val permissions: List<String>? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DelegateSetFields) return false
        return authorize == other.authorize &&
            permissions == other.permissions &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = authorize.hashCode()
        result = 31 * result + (permissions?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "DelegateSetFields(" +
            "authorize=$authorize, " +
            "permissions=$permissions, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [DelegateSetFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class DelegateSetBuilder internal constructor() {
    /** The account to delegate permissions to. Required. */
    public var authorize: Address? = null

    /** The list of permission identifiers to delegate. */
    public var permissions: List<String>? = null

    /** Flags controlling delegation behavior. */
    public var flags: UInt? = null

    internal fun build(): DelegateSetFields {
        val authorizeValue = requireNotNull(authorize) { "authorize is required" }
        return DelegateSetFields(
            authorize = authorizeValue,
            permissions = permissions,
            flags = flags,
        )
    }
}
