@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import java.math.BigInteger

class CryptoProviderTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── SHA-256 ─────────────────────────────────────────────────────────

    test("sha256 of 'abc' matches known hash") {
        val result = provider.sha256("abc".toByteArray())
        result.toHex() shouldBe "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    }

    // ── SHA-512 ─────────────────────────────────────────────────────────

    test("sha512 of 'abc' produces 64 bytes") {
        val result = provider.sha512("abc".toByteArray())
        result.size shouldBe 64
    }

    test("sha512 of 'abc' matches known hash") {
        val result = provider.sha512("abc".toByteArray())
        result.toHex() shouldBe (
            "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a" +
                "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f"
        )
    }

    // ── SHA-512Half ─────────────────────────────────────────────────────

    test("sha512Half of 'abc' is first 32 bytes of sha512") {
        val full = provider.sha512("abc".toByteArray())
        val half = provider.sha512Half("abc".toByteArray())
        half.size shouldBe 32
        half.toHex() shouldBe full.copyOfRange(0, 32).toHex()
    }

    // ── RIPEMD-160 ──────────────────────────────────────────────────────

    test("ripemd160 of 'abc' matches known hash") {
        val result = provider.ripemd160("abc".toByteArray())
        result.toHex() shouldBe "8eb208f7e05d987a9b044a8e98c6b087f15a0bfc"
    }

    // ── SecureRandom ────────────────────────────────────────────────────

    test("secureRandom produces requested number of bytes") {
        val result = provider.secureRandom(32)
        result.size shouldBe 32
    }

    test("secureRandom does not produce all zeros") {
        val result = provider.secureRandom(32)
        result shouldNotBe ByteArray(32)
    }

    // ── Ed25519 sign/verify roundtrip ───────────────────────────────────

    test("ed25519 sign and verify roundtrip") {
        // Known 32-byte test private key
        val privateKey = ByteArray(32) { (it + 1).toByte() }
        val publicKey = provider.ed25519PublicKey(privateKey)
        val message = "Hello, XRPL!".toByteArray()

        val signature = provider.ed25519Sign(message, privateKey)
        signature.size shouldBe 64

        provider.ed25519Verify(message, signature, publicKey) shouldBe true
    }

    test("ed25519 verify rejects tampered message") {
        val privateKey = ByteArray(32) { (it + 1).toByte() }
        val publicKey = provider.ed25519PublicKey(privateKey)
        val message = "Hello, XRPL!".toByteArray()
        val signature = provider.ed25519Sign(message, privateKey)

        provider.ed25519Verify("Tampered".toByteArray(), signature, publicKey) shouldBe false
    }

    // ── secp256k1 sign/verify roundtrip ─────────────────────────────────

    test("secp256k1 sign and verify roundtrip") {
        val privateKey = ByteArray(32) { (it + 1).toByte() }
        val publicKey = provider.secp256k1PublicKey(privateKey)
        val messageHash = provider.sha256("test message".toByteArray())

        val signature = provider.secp256k1Sign(messageHash, privateKey)
        provider.secp256k1Verify(messageHash, signature, publicKey) shouldBe true
    }

    test("secp256k1 verify rejects tampered hash") {
        val privateKey = ByteArray(32) { (it + 1).toByte() }
        val publicKey = provider.secp256k1PublicKey(privateKey)
        val messageHash = provider.sha256("test message".toByteArray())
        val signature = provider.secp256k1Sign(messageHash, privateKey)

        val tamperedHash = provider.sha256("tampered".toByteArray())
        provider.secp256k1Verify(tamperedHash, signature, publicKey) shouldBe false
    }

    // ── secp256k1 deterministic (RFC 6979) ──────────────────────────────

    test("secp256k1 signature is deterministic (RFC 6979)") {
        val privateKey = ByteArray(32) { (it + 1).toByte() }
        val messageHash = provider.sha256("deterministic test".toByteArray())

        val sig1 = provider.secp256k1Sign(messageHash, privateKey)
        val sig2 = provider.secp256k1Sign(messageHash, privateKey)

        sig1.toHex() shouldBe sig2.toHex()
    }

    // ── secp256k1 Low-S canonical ───────────────────────────────────────

    test("secp256k1 signature is Low-S canonical") {
        val halfOrder =
            BigInteger(
                "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF5D576E7357A4501DDFE92F46681B20A0",
                16,
            )

        // Test with multiple keys to increase confidence
        for (seed in 1..10) {
            val privateKey = ByteArray(32) { ((it + seed) % 256).toByte() }
            val messageHash = provider.sha256("canonical test $seed".toByteArray())
            val signature = provider.secp256k1Sign(messageHash, privateKey)

            val seq = ASN1Sequence.getInstance(signature)
            val s = ASN1Integer.getInstance(seq.getObjectAt(1)).value
            (s <= halfOrder) shouldBe true
        }
    }

    // ── secp256k1 public key format ─────────────────────────────────────

    test("secp256k1PublicKey returns compressed 33-byte key") {
        val privateKey = ByteArray(32) { (it + 1).toByte() }
        val publicKey = provider.secp256k1PublicKey(privateKey)
        publicKey.size shouldBe 33
        // Compressed keys start with 0x02 or 0x03
        (publicKey[0] == 0x02.toByte() || publicKey[0] == 0x03.toByte()) shouldBe true
    }

    // ── secp256k1AddPublicKeys ──────────────────────────────────────────

    test("secp256k1AddPublicKeys adds two public keys correctly") {
        val privateKey1 = ByteArray(32) { (it + 1).toByte() }
        val privateKey2 = ByteArray(32) { (it + 10).toByte() }
        val publicKey1 = provider.secp256k1PublicKey(privateKey1)
        val publicKey2 = provider.secp256k1PublicKey(privateKey2)

        val sumPublicKey = provider.secp256k1AddPublicKeys(publicKey1, publicKey2)
        sumPublicKey.size shouldBe 33

        // The sum of private keys should yield the same public key
        val sumPrivateKey = provider.secp256k1AddPrivateKeys(privateKey1, privateKey2)
        val expectedPublicKey = provider.secp256k1PublicKey(sumPrivateKey)
        sumPublicKey.toHex() shouldBe expectedPublicKey.toHex()
    }

    // ── secp256k1AddPrivateKeys ─────────────────────────────────────────

    test("secp256k1AddPrivateKeys produces 32 bytes") {
        val key1 = ByteArray(32) { (it + 1).toByte() }
        val key2 = ByteArray(32) { (it + 10).toByte() }
        val result = provider.secp256k1AddPrivateKeys(key1, key2)
        result.size shouldBe 32
    }

    test("secp256k1AddPrivateKeys is commutative") {
        val key1 = ByteArray(32) { (it + 1).toByte() }
        val key2 = ByteArray(32) { (it + 10).toByte() }
        val sum1 = provider.secp256k1AddPrivateKeys(key1, key2)
        val sum2 = provider.secp256k1AddPrivateKeys(key2, key1)
        sum1.toHex() shouldBe sum2.toHex()
    }
})

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
