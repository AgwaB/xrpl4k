package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Hash256

/**
 * Handles future ledger object types not yet known to this SDK version.
 *
 * When the ledger returns an object whose [LedgerObjectType] does not match any concrete
 * implementation, an `UnknownLedgerObject` is created to preserve the raw field data for
 * forward compatibility.
 *
 * @property ledgerObjectType The type identifier from the ledger response.
 * @property index The unique index (hash) of this object in the ledger state tree.
 * @property fields Raw field data preserved for forward compatibility.
 */
public class UnknownLedgerObject(
    override val ledgerObjectType: LedgerObjectType,
    override val index: Hash256,
    public val fields: Map<String, Any?>,
) : LedgerObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownLedgerObject) return false
        return ledgerObjectType == other.ledgerObjectType &&
            index == other.index &&
            fields == other.fields
    }

    override fun hashCode(): Int {
        var result = ledgerObjectType.hashCode()
        result = 31 * result + index.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    override fun toString(): String =
        "UnknownLedgerObject(ledgerObjectType=$ledgerObjectType, index=$index, fields=$fields)"
}
