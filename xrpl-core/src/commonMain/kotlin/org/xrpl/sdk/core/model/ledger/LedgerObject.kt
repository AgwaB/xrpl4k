package org.xrpl.sdk.core.model.ledger

import org.xrpl.sdk.core.type.Hash256

/**
 * Base interface for all XRPL ledger objects.
 *
 * Each ledger object has a [ledgerObjectType] identifying its kind and an [index] that uniquely
 * locates it in the ledger state tree.
 */
public sealed interface LedgerObject {
    /** The type of this ledger object. */
    public val ledgerObjectType: LedgerObjectType

    /** The unique index (hash) of this object in the ledger state tree. */
    public val index: Hash256
}
