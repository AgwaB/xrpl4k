package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.type.Address

/**
 * A transaction signer for multi-signing.
 *
 * @property account The signer's account address.
 * @property txnSignature The signer's transaction signature (hex).
 * @property signingPubKey The signer's public key (hex).
 */
public class Signer(
    public val account: Address,
    public val txnSignature: String,
    public val signingPubKey: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Signer) return false
        return account == other.account &&
            txnSignature == other.txnSignature &&
            signingPubKey == other.signingPubKey
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + txnSignature.hashCode()
        result = 31 * result + signingPubKey.hashCode()
        return result
    }

    override fun toString(): String =
        "Signer(account=$account, txnSignature=$txnSignature, signingPubKey=$signingPubKey)"
}
