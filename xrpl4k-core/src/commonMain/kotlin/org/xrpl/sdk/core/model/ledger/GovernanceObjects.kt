package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Hash256

/**
 * A pending amendment majority entry.
 *
 * @property amendment The hash of the amendment.
 * @property closeTime The ledger close time when this amendment gained a majority.
 */
public class AmendmentMajority(
    public val amendment: Hash256,
    public val closeTime: UInt,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AmendmentMajority) return false
        return amendment == other.amendment &&
            closeTime == other.closeTime
    }

    override fun hashCode(): Int {
        var result = amendment.hashCode()
        result = 31 * result + closeTime.hashCode()
        return result
    }

    override fun toString(): String = "AmendmentMajority(amendment=$amendment, closeTime=$closeTime)"
}

/**
 * Tracks which amendments are enabled or pending on the network.
 *
 * There is at most one `Amendments` object in the ledger.
 *
 * @property amendments The list of currently enabled amendment hashes.
 * @property majorities Amendments that have achieved a majority but are not yet enabled.
 * @property flags Bit-flags (reserved).
 */
public class Amendments(
    override val index: Hash256,
    public val amendments: List<Hash256> = emptyList(),
    public val majorities: List<AmendmentMajority> = emptyList(),
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.Amendments

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Amendments) return false
        return index == other.index &&
            amendments == other.amendments &&
            majorities == other.majorities &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + amendments.hashCode()
        result = 31 * result + majorities.hashCode()
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String =
        "Amendments(amendments=${amendments.size} enabled, majorities=${majorities.size} pending)"
}

/**
 * Current transaction fee settings used by the network.
 *
 * There is exactly one `FeeSettings` object in the ledger.
 *
 * @property baseFee The base fee for a reference transaction in drops (as string for large values).
 * @property referenceFeeUnits The cost of a reference transaction in fee units.
 * @property reserveBase The base reserve requirement in drops.
 * @property reserveIncrement The per-object reserve increment in drops.
 * @property flags Bit-flags (reserved).
 */
public class FeeSettings(
    override val index: Hash256,
    public val baseFee: String? = null,
    public val referenceFeeUnits: UInt? = null,
    public val reserveBase: UInt? = null,
    public val reserveIncrement: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.FeeSettings

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeeSettings) return false
        return index == other.index &&
            baseFee == other.baseFee &&
            referenceFeeUnits == other.referenceFeeUnits &&
            reserveBase == other.reserveBase &&
            reserveIncrement == other.reserveIncrement &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + (baseFee?.hashCode() ?: 0)
        result = 31 * result + (referenceFeeUnits?.hashCode() ?: 0)
        result = 31 * result + (reserveBase?.hashCode() ?: 0)
        result = 31 * result + (reserveIncrement?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String =
        "FeeSettings(baseFee=$baseFee, reserveBase=$reserveBase, reserveIncrement=$reserveIncrement)"
}

/**
 * A list of prior ledger hashes used for validation.
 *
 * @property hashes The list of ledger hashes (most recent first).
 * @property lastLedgerSequence The sequence number of the last ledger whose hash is in this list.
 * @property flags Bit-flags (reserved).
 */
public class LedgerHashes(
    override val index: Hash256,
    public val hashes: List<Hash256> = emptyList(),
    public val lastLedgerSequence: UInt? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.LedgerHashes

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerHashes) return false
        return index == other.index &&
            hashes == other.hashes &&
            lastLedgerSequence == other.lastLedgerSequence &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + hashes.hashCode()
        result = 31 * result + (lastLedgerSequence?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "LedgerHashes(hashes=${hashes.size} entries)"
}

/**
 * A disabled validator entry in the [NegativeUnl].
 *
 * @property publicKey The validator's public key (hex-encoded).
 * @property firstLedgerSequence The first ledger sequence in which this validator was disabled.
 */
public class DisabledValidator(
    public val publicKey: String,
    public val firstLedgerSequence: UInt,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisabledValidator) return false
        return publicKey == other.publicKey &&
            firstLedgerSequence == other.firstLedgerSequence
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + firstLedgerSequence.hashCode()
        return result
    }

    override fun toString(): String = "DisabledValidator(publicKey=$publicKey)"
}

/**
 * Tracks validators temporarily removed from the UNL (Unique Node List).
 *
 * There is at most one `NegativeUnl` object in the ledger.
 *
 * @property disabledValidators The list of currently disabled validators.
 * @property validatorToDisable The public key of the next validator to disable (hex).
 * @property validatorToReEnable The public key of the next validator to re-enable (hex).
 * @property flags Bit-flags (reserved).
 */
public class NegativeUnl(
    override val index: Hash256,
    public val disabledValidators: List<DisabledValidator> = emptyList(),
    public val validatorToDisable: String? = null,
    public val validatorToReEnable: String? = null,
    public val flags: UInt = 0u,
) : LedgerObject {
    override val ledgerObjectType: LedgerObjectType get() = LedgerObjectType.NegativeUNL

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NegativeUnl) return false
        return index == other.index &&
            disabledValidators == other.disabledValidators &&
            validatorToDisable == other.validatorToDisable &&
            validatorToReEnable == other.validatorToReEnable &&
            flags == other.flags
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + disabledValidators.hashCode()
        result = 31 * result + (validatorToDisable?.hashCode() ?: 0)
        result = 31 * result + (validatorToReEnable?.hashCode() ?: 0)
        result = 31 * result + flags.hashCode()
        return result
    }

    override fun toString(): String = "NegativeUnl(disabledValidators=${disabledValidators.size})"
}
