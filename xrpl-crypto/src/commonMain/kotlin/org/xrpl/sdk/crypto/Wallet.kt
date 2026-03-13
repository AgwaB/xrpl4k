package org.xrpl.sdk.crypto

import org.xrpl.sdk.core.model.transaction.XrplTransaction
import org.xrpl.sdk.core.type.AccountId
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.PublicKey
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.keys.KeyDerivation
import org.xrpl.sdk.crypto.keys.KeyPair
import org.xrpl.sdk.crypto.signing.PrivateKey
import org.xrpl.sdk.crypto.signing.SingleSignature
import org.xrpl.sdk.crypto.signing.TransactionSigner

/**
 * An XRPL wallet holding a key pair and address.
 *
 * Use factory methods on the companion object to create instances.
 * Implements [AutoCloseable]: call [close] (or use `wallet.use {}`) to zero
 * the private key bytes and seed bytes from memory when done.
 */
public class Wallet internal constructor(
    public val address: Address,
    public val publicKey: PublicKey,
    public val algorithm: KeyAlgorithm,
    internal val keyPair: KeyPair,
    private val seedBytes: ByteArray?,
    private val provider: CryptoProvider,
) : AutoCloseable {
    /**
     * Zeroes private key and seed bytes in memory and releases associated resources.
     *
     * Use [AutoCloseable.use] or call this explicitly when the wallet is no longer needed.
     */
    override fun close() {
        keyPair.close()
        seedBytes?.fill(0)
    }

    /**
     * Sign [message] with this wallet's private key.
     *
     * - Ed25519: signs the raw message (RFC 8032 — no pre-hash).
     * - secp256k1: signs the message as-is; the caller is responsible for pre-hashing.
     */
    public fun sign(message: ByteArray): ByteArray =
        when (algorithm) {
            KeyAlgorithm.Ed25519 -> provider.ed25519Sign(message, keyPair.privateKeyBytes)
            KeyAlgorithm.Secp256k1 -> provider.secp256k1Sign(message, keyPair.privateKeyBytes)
        }

    /**
     * Verify [signature] against [message] using this wallet's public key.
     *
     * - Ed25519: verifies against raw message.
     * - secp256k1: verifies against message hash (caller must supply the same pre-hashed value used in [sign]).
     */
    public fun verify(
        message: ByteArray,
        signature: ByteArray,
    ): Boolean {
        return when (algorithm) {
            KeyAlgorithm.Ed25519 -> {
                // Ed25519 public key in XRPL is prefixed with 0xED (1 byte); drop it for raw verify.
                val publicKeyBytes = publicKey.value.hexToByteArray().drop(1).toByteArray()
                provider.ed25519Verify(message, signature, publicKeyBytes)
            }
            KeyAlgorithm.Secp256k1 -> {
                val publicKeyBytes = publicKey.value.hexToByteArray()
                provider.secp256k1Verify(message, signature, publicKeyBytes)
            }
        }
    }

    /**
     * Sign [transaction] using [signer], delegating private key material via a temporary [PrivateKey].
     */
    public fun signWith(
        signer: TransactionSigner<PrivateKey>,
        transaction: XrplTransaction.Filled,
    ): XrplTransaction.Signed {
        return PrivateKey(keyPair.privateKeyBytes.copyOf(), algorithm).use { key ->
            signer.sign(key, transaction)
        }
    }

    /**
     * Multi-sign [transaction] using [signer] for the given [signerAccountId].
     */
    public fun multiSignWith(
        signer: TransactionSigner<PrivateKey>,
        transaction: XrplTransaction.Filled,
        signerAccountId: AccountId,
    ): SingleSignature {
        return PrivateKey(keyPair.privateKeyBytes.copyOf(), algorithm).use { key ->
            signer.multiSign(key, transaction, signerAccountId)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wallet) return false
        return address == other.address && publicKey == other.publicKey
    }

    override fun hashCode(): Int = 31 * address.hashCode() + publicKey.hashCode()

    /** Does NOT include private key or seed bytes. */
    override fun toString(): String = "Wallet(address=$address, publicKey=$publicKey, algorithm=$algorithm)"

    public companion object {
        /**
         * Generate a new random wallet.
         *
         * @param algorithm The key algorithm to use (default: Ed25519).
         * @param provider The crypto provider to use.
         * @return A [GeneratedWallet] containing the wallet and a seed string for backup.
         */
        public fun generate(
            algorithm: KeyAlgorithm = KeyAlgorithm.Ed25519,
            provider: CryptoProvider = platformCryptoProvider(),
        ): GeneratedWallet {
            val seedBytes = KeyDerivation.generateSeed(provider)
            val keyPair = KeyDerivation.deriveKeyPair(seedBytes, algorithm, provider)
            val accountId = AddressCodec.accountIdFromPublicKey(keyPair.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)
            val seedString = AddressCodec.encodeSeed(seedBytes, algorithm, provider)
            return GeneratedWallet(
                Wallet(address, keyPair.publicKey, algorithm, keyPair, seedBytes.copyOf(), provider),
                seedString,
            )
        }

        /**
         * Restore a wallet from a Base58-encoded seed string.
         *
         * @param seed The Base58-encoded seed string (starts with `"s"` or `"sEd"`).
         * @param provider The crypto provider to use.
         * @return The restored [Wallet].
         */
        public fun fromSeed(
            seed: String,
            provider: CryptoProvider = platformCryptoProvider(),
        ): Wallet {
            val (seedBytes, algorithm) = AddressCodec.decodeSeed(seed, provider)
            val keyPair = KeyDerivation.deriveKeyPair(seedBytes, algorithm, provider)
            val accountId = AddressCodec.accountIdFromPublicKey(keyPair.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)
            return Wallet(address, keyPair.publicKey, algorithm, keyPair, seedBytes.copyOf(), provider)
        }

        /**
         * Derive a wallet from raw entropy bytes.
         *
         * The entropy is used directly as the seed (must be exactly 16 bytes).
         *
         * @param entropy 16 bytes of entropy to use as the seed.
         * @param algorithm The key algorithm to use (default: Ed25519).
         * @param provider The crypto provider to use.
         * @return The derived [Wallet].
         */
        public fun fromEntropy(
            entropy: ByteArray,
            algorithm: KeyAlgorithm = KeyAlgorithm.Ed25519,
            provider: CryptoProvider = platformCryptoProvider(),
        ): Wallet {
            val keyPair = KeyDerivation.deriveKeyPair(entropy, algorithm, provider)
            val accountId = AddressCodec.accountIdFromPublicKey(keyPair.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)
            return Wallet(address, keyPair.publicKey, algorithm, keyPair, entropy.copyOf(), provider)
        }
    }
}
