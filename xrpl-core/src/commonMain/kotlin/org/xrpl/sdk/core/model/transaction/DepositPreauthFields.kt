package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

// ── DepositPreauth ────────────────────────────────────────────────────────────

/**
 * Fields specific to a DepositPreauth transaction.
 *
 * @property authorize Address to preauthorize for deposits.
 * @property unauthorize Address to remove from preauthorized deposits.
 */
public class DepositPreauthFields(
    public val authorize: Address? = null,
    public val unauthorize: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DepositPreauthFields) return false
        return authorize == other.authorize &&
            unauthorize == other.unauthorize
    }

    override fun hashCode(): Int {
        var result = (authorize?.hashCode() ?: 0)
        result = 31 * result + (unauthorize?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "DepositPreauthFields(" +
            "authorize=$authorize, " +
            "unauthorize=$unauthorize" +
            ")"
}

/**
 * DSL builder for [DepositPreauthFields].
 */
@XrplDsl
public class DepositPreauthBuilder internal constructor() {
    /** Address to preauthorize for deposits. */
    public var authorize: Address? = null

    /** Address to remove from preauthorized deposits. */
    public var unauthorize: Address? = null

    internal fun build(): DepositPreauthFields =
        DepositPreauthFields(
            authorize = authorize,
            unauthorize = unauthorize,
        )
}
