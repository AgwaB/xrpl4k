package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.ExperimentalXrplApi
import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.model.amount.CurrencyAmount
import org.xrpl.sdk.core.type.Address

// ── VaultCreate ───────────────────────────────────────────────────────────────

/**
 * Fields specific to a VaultCreate transaction.
 *
 * @property asset The asset type for the vault.
 * @property mptIssuanceId Optional MPT issuance ID.
 * @property flags Flags controlling vault behavior.
 */
@ExperimentalXrplApi
public class VaultCreateFields(
    public val asset: String,
    public val mptIssuanceId: String? = null,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultCreateFields) return false
        return asset == other.asset &&
            mptIssuanceId == other.mptIssuanceId &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = asset.hashCode()
        result = 31 * result + (mptIssuanceId?.hashCode() ?: 0)
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "VaultCreateFields(" +
            "asset=$asset, " +
            "mptIssuanceId=$mptIssuanceId, " +
            "flags=$flags" +
            ")"
}

/**
 * DSL builder for [VaultCreateFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class VaultCreateBuilder internal constructor() {
    /** The asset type for the vault. Required. */
    public lateinit var asset: String

    /** Optional MPT issuance ID. */
    public var mptIssuanceId: String? = null

    /** Flags controlling vault behavior. */
    public var flags: UInt? = null

    internal fun build(): VaultCreateFields {
        require(::asset.isInitialized) { "asset is required" }
        return VaultCreateFields(
            asset = asset,
            mptIssuanceId = mptIssuanceId,
            flags = flags,
        )
    }
}

// ── VaultSet ──────────────────────────────────────────────────────────────────

/**
 * Fields specific to a VaultSet transaction.
 *
 * @property vaultId The ID of the vault to modify.
 * @property flags Flags controlling the operation.
 */
@ExperimentalXrplApi
public class VaultSetFields(
    public val vaultId: String,
    public val flags: UInt? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultSetFields) return false
        return vaultId == other.vaultId &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = vaultId.hashCode()
        result = 31 * result + (flags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "VaultSetFields(vaultId=$vaultId, flags=$flags)"
}

/**
 * DSL builder for [VaultSetFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class VaultSetBuilder internal constructor() {
    /** The ID of the vault to modify. Required. */
    public lateinit var vaultId: String

    /** Flags controlling the operation. */
    public var flags: UInt? = null

    internal fun build(): VaultSetFields {
        require(::vaultId.isInitialized) { "vaultId is required" }
        return VaultSetFields(vaultId = vaultId, flags = flags)
    }
}

// ── VaultDelete ───────────────────────────────────────────────────────────────

/**
 * Fields specific to a VaultDelete transaction.
 *
 * @property vaultId The ID of the vault to delete.
 */
@ExperimentalXrplApi
public class VaultDeleteFields(
    public val vaultId: String,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultDeleteFields) return false
        return vaultId == other.vaultId
    }

    override fun hashCode(): Int = vaultId.hashCode()

    override fun toString(): String = "VaultDeleteFields(vaultId=$vaultId)"
}

/**
 * DSL builder for [VaultDeleteFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class VaultDeleteBuilder internal constructor() {
    /** The ID of the vault to delete. Required. */
    public lateinit var vaultId: String

    internal fun build(): VaultDeleteFields {
        require(::vaultId.isInitialized) { "vaultId is required" }
        return VaultDeleteFields(vaultId = vaultId)
    }
}

// ── VaultDeposit ──────────────────────────────────────────────────────────────

/**
 * Fields specific to a VaultDeposit transaction.
 *
 * @property vaultId The ID of the vault to deposit into.
 * @property amount The amount to deposit.
 */
@ExperimentalXrplApi
public class VaultDepositFields(
    public val vaultId: String,
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultDepositFields) return false
        return vaultId == other.vaultId &&
            amount == other.amount
    }

    override fun hashCode(): Int {
        var result = vaultId.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun toString(): String = "VaultDepositFields(vaultId=$vaultId, amount=$amount)"
}

/**
 * DSL builder for [VaultDepositFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class VaultDepositBuilder internal constructor() {
    /** The ID of the vault to deposit into. Required. */
    public lateinit var vaultId: String

    /** The amount to deposit. Required. */
    public lateinit var amount: CurrencyAmount

    internal fun build(): VaultDepositFields {
        require(::vaultId.isInitialized) { "vaultId is required" }
        require(::amount.isInitialized) { "amount is required" }
        return VaultDepositFields(vaultId = vaultId, amount = amount)
    }
}

// ── VaultWithdraw ─────────────────────────────────────────────────────────────

/**
 * Fields specific to a VaultWithdraw transaction.
 *
 * @property vaultId The ID of the vault to withdraw from.
 * @property amount The amount to withdraw.
 */
@ExperimentalXrplApi
public class VaultWithdrawFields(
    public val vaultId: String,
    public val amount: CurrencyAmount,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultWithdrawFields) return false
        return vaultId == other.vaultId &&
            amount == other.amount
    }

    override fun hashCode(): Int {
        var result = vaultId.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun toString(): String = "VaultWithdrawFields(vaultId=$vaultId, amount=$amount)"
}

/**
 * DSL builder for [VaultWithdrawFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class VaultWithdrawBuilder internal constructor() {
    /** The ID of the vault to withdraw from. Required. */
    public lateinit var vaultId: String

    /** The amount to withdraw. Required. */
    public lateinit var amount: CurrencyAmount

    internal fun build(): VaultWithdrawFields {
        require(::vaultId.isInitialized) { "vaultId is required" }
        require(::amount.isInitialized) { "amount is required" }
        return VaultWithdrawFields(vaultId = vaultId, amount = amount)
    }
}

// ── VaultClawback ─────────────────────────────────────────────────────────────

/**
 * Fields specific to a VaultClawback transaction.
 *
 * @property vaultId The ID of the vault.
 * @property amount The amount to claw back.
 * @property holder The holder account to claw back from.
 */
@ExperimentalXrplApi
public class VaultClawbackFields(
    public val vaultId: String,
    public val amount: CurrencyAmount,
    public val holder: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultClawbackFields) return false
        return vaultId == other.vaultId &&
            amount == other.amount &&
            holder == other.holder
    }

    override fun hashCode(): Int {
        var result = vaultId.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (holder?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "VaultClawbackFields(vaultId=$vaultId, amount=$amount, holder=$holder)"
}

/**
 * DSL builder for [VaultClawbackFields].
 */
@ExperimentalXrplApi
@XrplDsl
public class VaultClawbackBuilder internal constructor() {
    /** The ID of the vault. Required. */
    public lateinit var vaultId: String

    /** The amount to claw back. Required. */
    public lateinit var amount: CurrencyAmount

    /** The holder account to claw back from. */
    public var holder: Address? = null

    internal fun build(): VaultClawbackFields {
        require(::vaultId.isInitialized) { "vaultId is required" }
        require(::amount.isInitialized) { "amount is required" }
        return VaultClawbackFields(
            vaultId = vaultId,
            amount = amount,
            holder = holder,
        )
    }
}
