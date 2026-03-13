package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl

// ── DIDSet ────────────────────────────────────────────────────────────────────

/**
 * Fields specific to a DIDSet transaction.
 *
 * @property uri The URI associated with the DID.
 * @property data The DID document data.
 * @property didDocument The DID document.
 */
public class DIDSetFields(
    public val uri: String? = null,
    public val data: String? = null,
    public val didDocument: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DIDSetFields) return false
        return uri == other.uri &&
            data == other.data &&
            didDocument == other.didDocument
    }

    override fun hashCode(): Int {
        var result = (uri?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (didDocument?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "DIDSetFields(" +
            "uri=$uri, " +
            "data=$data, " +
            "didDocument=$didDocument" +
            ")"
}

/**
 * DSL builder for [DIDSetFields].
 */
@XrplDsl
public class DIDSetBuilder internal constructor() {
    /** The URI associated with the DID. */
    public var uri: String? = null

    /** The DID document data. */
    public var data: String? = null

    /** The DID document. */
    public var didDocument: String? = null

    internal fun build(): DIDSetFields =
        DIDSetFields(
            uri = uri,
            data = data,
            didDocument = didDocument,
        )
}

// ── DIDDelete ─────────────────────────────────────────────────────────────────

/**
 * Fields specific to a DIDDelete transaction.
 *
 * DIDDelete has no transaction-type-specific fields.
 */
public class DIDDeleteFields : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DIDDeleteFields
    }

    override fun hashCode(): Int = "DIDDeleteFields".hashCode()

    override fun toString(): String = "DIDDeleteFields()"
}

/**
 * DSL builder for [DIDDeleteFields].
 */
@XrplDsl
public class DIDDeleteBuilder internal constructor() {
    internal fun build(): DIDDeleteFields = DIDDeleteFields()
}
