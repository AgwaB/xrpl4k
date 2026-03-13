package org.xrpl.sdk.crypto.signing

import org.xrpl.sdk.core.model.transaction.Signer

/** Result of a multi-signing operation by one signer. */
public class SingleSignature(
    public val signer: Signer,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SingleSignature) return false
        return signer == other.signer
    }

    override fun hashCode(): Int = signer.hashCode()

    override fun toString(): String = "SingleSignature(signer=$signer)"
}
