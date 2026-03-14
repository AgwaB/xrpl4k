package org.xrpl.sdk.codec.hashing

/**
 * Types of nodes that can appear in a SHAMap tree.
 */
public enum class ShaMapNodeType {
    /** Transaction without metadata (used for computing transaction ID). */
    TRANSACTION_NO_META,

    /** Transaction with metadata (used in transaction tree). */
    TRANSACTION_METADATA,

    /** Account state entry (used in state tree). */
    ACCOUNT_STATE,
}
