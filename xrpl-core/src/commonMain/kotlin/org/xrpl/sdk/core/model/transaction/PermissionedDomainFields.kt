package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl

// ── PermissionedDomainSet ─────────────────────────────────────────────────────

/**
 * Fields specific to a PermissionedDomainSet transaction.
 *
 * @property domainId The ID of the permissioned domain to set (null for creation).
 * @property acceptedCredentials The list of accepted credential definitions.
 */
public class PermissionedDomainSetFields(
    public val domainId: String? = null,
    public val acceptedCredentials: List<Map<String, Any?>>? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PermissionedDomainSetFields) return false
        return domainId == other.domainId &&
            acceptedCredentials == other.acceptedCredentials
    }

    override fun hashCode(): Int {
        var result = (domainId?.hashCode() ?: 0)
        result = 31 * result + (acceptedCredentials?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "PermissionedDomainSetFields(" +
            "domainId=$domainId, " +
            "acceptedCredentials=$acceptedCredentials" +
            ")"
}

/**
 * DSL builder for [PermissionedDomainSetFields].
 */
@XrplDsl
public class PermissionedDomainSetBuilder internal constructor() {
    /** The ID of the permissioned domain to set (null for creation). */
    public var domainId: String? = null

    /** The list of accepted credential definitions. */
    public var acceptedCredentials: List<Map<String, Any?>>? = null

    internal fun build(): PermissionedDomainSetFields =
        PermissionedDomainSetFields(
            domainId = domainId,
            acceptedCredentials = acceptedCredentials,
        )
}

// ── PermissionedDomainDelete ──────────────────────────────────────────────────

/**
 * Fields specific to a PermissionedDomainDelete transaction.
 *
 * @property domainId The ID of the permissioned domain to delete.
 */
public class PermissionedDomainDeleteFields(
    public val domainId: String,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PermissionedDomainDeleteFields) return false
        return domainId == other.domainId
    }

    override fun hashCode(): Int = domainId.hashCode()

    override fun toString(): String = "PermissionedDomainDeleteFields(domainId=$domainId)"
}

/**
 * DSL builder for [PermissionedDomainDeleteFields].
 */
@XrplDsl
public class PermissionedDomainDeleteBuilder internal constructor() {
    /** The ID of the permissioned domain to delete. Required. */
    public lateinit var domainId: String

    internal fun build(): PermissionedDomainDeleteFields {
        require(::domainId.isInitialized) { "domainId is required" }
        return PermissionedDomainDeleteFields(domainId = domainId)
    }
}
