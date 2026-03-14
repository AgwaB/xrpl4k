package org.xrpl.sdk.crypto.signing

import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.AccountId

/**
 * Signs XRPL transactions.
 *
 * This interface lives in `xrpl-crypto` as part of the crypto API surface.
 * It does NOT depend on `BinaryCodec`. Concrete implementations that need
 * encoding (e.g., [InMemorySigner]) live in downstream modules like `xrpl-client`.
 *
 * @param K The type of private key material.
 */
public interface TransactionSigner<K : PrivateKeyable> {
    /**
     * Signs a filled transaction, producing a signed transaction.
     */
    public fun sign(
        key: K,
        transaction: XrplTransaction.Filled,
    ): XrplTransaction.Signed

    /**
     * Multi-signs a filled transaction, producing a single signer entry.
     */
    public fun multiSign(
        key: K,
        transaction: XrplTransaction.Filled,
        signerAccountId: AccountId,
    ): SingleSignature
}
