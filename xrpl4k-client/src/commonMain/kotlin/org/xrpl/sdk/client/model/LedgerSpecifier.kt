package org.xrpl.sdk.client.model

import org.xrpl.sdk.core.type.Hash256

/**
 * Specifies which ledger version to use for an RPC query.
 */
public sealed class LedgerSpecifier {
    /** The most recent validated ledger. Default for most queries. */
    public data object Validated : LedgerSpecifier()

    /** The current open (in-progress) ledger. */
    public data object Current : LedgerSpecifier()

    /** The most recently closed ledger. */
    public data object Closed : LedgerSpecifier()

    /** A specific ledger by its sequence number. */
    public class Index(public val index: UInt) : LedgerSpecifier() {
        override fun equals(other: Any?): Boolean = other is Index && index == other.index

        override fun hashCode(): Int = index.hashCode()

        override fun toString(): String = "LedgerSpecifier.Index($index)"
    }

    /** A specific ledger by its hash. */
    public class Hash(public val hash: Hash256) : LedgerSpecifier() {
        override fun equals(other: Any?): Boolean = other is Hash && hash == other.hash

        override fun hashCode(): Int = hash.hashCode()

        override fun toString(): String = "LedgerSpecifier.Hash($hash)"
    }

    internal fun toParamPair(): Pair<String, String> =
        when (this) {
            is Validated -> "ledger_index" to "validated"
            is Current -> "ledger_index" to "current"
            is Closed -> "ledger_index" to "closed"
            is Index -> "ledger_index" to index.toString()
            is Hash -> "ledger_hash" to hash.value
        }
}
