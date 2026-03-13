package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl
import org.xrpl.sdk.core.type.Address

// ── CredentialCreate ──────────────────────────────────────────────────────────

/**
 * Fields specific to a CredentialCreate transaction.
 *
 * @property subject The subject account of the credential.
 * @property credentialType The type identifier of the credential.
 * @property issuer The issuer account (if different from the transaction account).
 * @property expiration The expiration time of the credential (Ripple epoch).
 * @property uri Optional URI pointing to credential metadata.
 */
public class CredentialCreateFields(
    public val subject: Address,
    public val credentialType: String,
    public val issuer: Address? = null,
    public val expiration: UInt? = null,
    public val uri: String? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CredentialCreateFields) return false
        return subject == other.subject &&
            credentialType == other.credentialType &&
            issuer == other.issuer &&
            expiration == other.expiration &&
            uri == other.uri
    }

    override fun hashCode(): Int {
        var result = subject.hashCode()
        result = 31 * result + credentialType.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + (expiration?.hashCode() ?: 0)
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "CredentialCreateFields(" +
            "subject=$subject, " +
            "credentialType=$credentialType, " +
            "issuer=$issuer, " +
            "expiration=$expiration, " +
            "uri=$uri" +
            ")"
}

/**
 * DSL builder for [CredentialCreateFields].
 */
@XrplDsl
public class CredentialCreateBuilder internal constructor() {
    /** The subject account of the credential. Required. */
    public var subject: Address? = null

    /** The type identifier of the credential. Required. */
    public lateinit var credentialType: String

    /** The issuer account (if different from the transaction account). */
    public var issuer: Address? = null

    /** The expiration time of the credential (Ripple epoch). */
    public var expiration: UInt? = null

    /** Optional URI pointing to credential metadata. */
    public var uri: String? = null

    internal fun build(): CredentialCreateFields {
        val subjectValue = requireNotNull(subject) { "subject is required" }
        require(::credentialType.isInitialized) { "credentialType is required" }
        return CredentialCreateFields(
            subject = subjectValue,
            credentialType = credentialType,
            issuer = issuer,
            expiration = expiration,
            uri = uri,
        )
    }
}

// ── CredentialAccept ──────────────────────────────────────────────────────────

/**
 * Fields specific to a CredentialAccept transaction.
 *
 * @property subject The subject account of the credential.
 * @property credentialType The type identifier of the credential.
 * @property issuer The issuer account.
 */
public class CredentialAcceptFields(
    public val subject: Address,
    public val credentialType: String,
    public val issuer: Address,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CredentialAcceptFields) return false
        return subject == other.subject &&
            credentialType == other.credentialType &&
            issuer == other.issuer
    }

    override fun hashCode(): Int {
        var result = subject.hashCode()
        result = 31 * result + credentialType.hashCode()
        result = 31 * result + issuer.hashCode()
        return result
    }

    override fun toString(): String =
        "CredentialAcceptFields(" +
            "subject=$subject, " +
            "credentialType=$credentialType, " +
            "issuer=$issuer" +
            ")"
}

/**
 * DSL builder for [CredentialAcceptFields].
 */
@XrplDsl
public class CredentialAcceptBuilder internal constructor() {
    /** The subject account of the credential. Required. */
    public var subject: Address? = null

    /** The type identifier of the credential. Required. */
    public lateinit var credentialType: String

    /** The issuer account. Required. */
    public var issuer: Address? = null

    internal fun build(): CredentialAcceptFields {
        val subjectValue = requireNotNull(subject) { "subject is required" }
        require(::credentialType.isInitialized) { "credentialType is required" }
        val issuerValue = requireNotNull(issuer) { "issuer is required" }
        return CredentialAcceptFields(
            subject = subjectValue,
            credentialType = credentialType,
            issuer = issuerValue,
        )
    }
}

// ── CredentialDelete ──────────────────────────────────────────────────────────

/**
 * Fields specific to a CredentialDelete transaction.
 *
 * @property subject The subject account of the credential.
 * @property credentialType The type identifier of the credential.
 * @property issuer The issuer account (if different from the transaction account).
 */
public class CredentialDeleteFields(
    public val subject: Address,
    public val credentialType: String,
    public val issuer: Address? = null,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CredentialDeleteFields) return false
        return subject == other.subject &&
            credentialType == other.credentialType &&
            issuer == other.issuer
    }

    override fun hashCode(): Int {
        var result = subject.hashCode()
        result = 31 * result + credentialType.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "CredentialDeleteFields(" +
            "subject=$subject, " +
            "credentialType=$credentialType, " +
            "issuer=$issuer" +
            ")"
}

/**
 * DSL builder for [CredentialDeleteFields].
 */
@XrplDsl
public class CredentialDeleteBuilder internal constructor() {
    /** The subject account of the credential. Required. */
    public var subject: Address? = null

    /** The type identifier of the credential. Required. */
    public lateinit var credentialType: String

    /** The issuer account (if different from the transaction account). */
    public var issuer: Address? = null

    internal fun build(): CredentialDeleteFields {
        val subjectValue = requireNotNull(subject) { "subject is required" }
        require(::credentialType.isInitialized) { "credentialType is required" }
        return CredentialDeleteFields(
            subject = subjectValue,
            credentialType = credentialType,
            issuer = issuer,
        )
    }
}
