package org.xrpl.sdk.crypto

/**
 * Platform-abstracted cryptographic primitives for XRPL.
 *
 * Implementations must be thread-safe. The JVM implementation uses BouncyCastle.
 */
public interface CryptoProvider {
    public fun sha256(data: ByteArray): ByteArray

    public fun sha512(data: ByteArray): ByteArray

    public fun sha512Half(data: ByteArray): ByteArray

    public fun ripemd160(data: ByteArray): ByteArray

    public fun secureRandom(size: Int): ByteArray

    /**
     * Sign a message with Ed25519 private key. Returns 64-byte signature.
     * Per RFC 8032, Ed25519 signs the RAW MESSAGE (not a pre-hash).
     */
    public fun ed25519Sign(
        message: ByteArray,
        privateKey: ByteArray,
    ): ByteArray

    public fun ed25519PublicKey(privateKey: ByteArray): ByteArray

    public fun ed25519Verify(
        message: ByteArray,
        signature: ByteArray,
        publicKey: ByteArray,
    ): Boolean

    /**
     * Sign a message hash with secp256k1 private key. Returns DER-encoded ECDSA signature.
     * Contract: RFC 6979 deterministic nonce + Low-S canonical form.
     * XRPL rejects non-canonical signatures.
     */
    public fun secp256k1Sign(
        messageHash: ByteArray,
        privateKey: ByteArray,
    ): ByteArray

    public fun secp256k1PublicKey(privateKey: ByteArray): ByteArray

    public fun secp256k1Verify(
        messageHash: ByteArray,
        signature: ByteArray,
        publicKey: ByteArray,
    ): Boolean

    public fun secp256k1AddPublicKeys(
        key1: ByteArray,
        key2: ByteArray,
    ): ByteArray

    public fun secp256k1AddPrivateKeys(
        key1: ByteArray,
        key2: ByteArray,
    ): ByteArray
}

public expect fun platformCryptoProvider(): CryptoProvider
