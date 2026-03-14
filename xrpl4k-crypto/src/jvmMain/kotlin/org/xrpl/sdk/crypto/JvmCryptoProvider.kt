@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * JVM implementation of [CryptoProvider] backed by BouncyCastle and standard JCA.
 */
internal object JvmCryptoProvider : CryptoProvider {
    private val secureRandom = SecureRandom()

    private val curveParams = CustomNamedCurves.getByName("secp256k1")
    private val domainParams =
        ECDomainParameters(
            curveParams.curve,
            curveParams.g,
            curveParams.n,
            curveParams.h,
        )
    private val halfOrder: BigInteger = curveParams.n.shiftRight(1)

    override fun sha256(data: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(data)

    override fun sha512(data: ByteArray): ByteArray = MessageDigest.getInstance("SHA-512").digest(data)

    override fun sha512Half(data: ByteArray): ByteArray = sha512(data).copyOfRange(0, 32)

    override fun ripemd160(data: ByteArray): ByteArray {
        val digest = RIPEMD160Digest()
        digest.update(data, 0, data.size)
        val result = ByteArray(digest.digestSize)
        digest.doFinal(result, 0)
        return result
    }

    override fun secureRandom(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    // ── HMAC-SHA512 ───────────────────────────────────────────────────────

    override fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
        val hmac = HMac(SHA512Digest())
        hmac.init(KeyParameter(key))
        hmac.update(data, 0, data.size)
        val result = ByteArray(hmac.macSize)
        hmac.doFinal(result, 0)
        return result
    }

    // ── Ed25519 ─────────────────────────────────────────────────────────

    override fun ed25519Sign(
        message: ByteArray,
        privateKey: ByteArray,
    ): ByteArray {
        val keyParams = Ed25519PrivateKeyParameters(privateKey, 0)
        val signer = Ed25519Signer()
        signer.init(true, keyParams)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    override fun ed25519PublicKey(privateKey: ByteArray): ByteArray {
        val keyParams = Ed25519PrivateKeyParameters(privateKey, 0)
        return keyParams.generatePublicKey().encoded
    }

    override fun ed25519Verify(
        message: ByteArray,
        signature: ByteArray,
        publicKey: ByteArray,
    ): Boolean {
        val keyParams = Ed25519PublicKeyParameters(publicKey, 0)
        val verifier = Ed25519Signer()
        verifier.init(false, keyParams)
        verifier.update(message, 0, message.size)
        return verifier.verifySignature(signature)
    }

    // ── secp256k1 ───────────────────────────────────────────────────────

    override fun secp256k1Sign(
        messageHash: ByteArray,
        privateKey: ByteArray,
    ): ByteArray {
        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        val privKeyParams = ECPrivateKeyParameters(BigInteger(1, privateKey), domainParams)
        signer.init(true, privKeyParams)
        val components = signer.generateSignature(messageHash)
        var r = components[0]
        var s = components[1]

        // Enforce Low-S canonical form required by XRPL
        if (s > halfOrder) {
            s = curveParams.n.subtract(s)
        }

        return derEncode(r, s)
    }

    override fun secp256k1PublicKey(privateKey: ByteArray): ByteArray {
        val d = BigInteger(1, privateKey)
        val q: ECPoint = curveParams.g.multiply(d).normalize()
        return q.getEncoded(true)
    }

    override fun secp256k1Verify(
        messageHash: ByteArray,
        signature: ByteArray,
        publicKey: ByteArray,
    ): Boolean {
        val (r, s) = derDecode(signature)
        val point = curveParams.curve.decodePoint(publicKey)
        val pubKeyParams = ECPublicKeyParameters(point, domainParams)
        val verifier = ECDSASigner()
        verifier.init(false, pubKeyParams)
        return verifier.verifySignature(messageHash, r, s)
    }

    override fun secp256k1AddPublicKeys(
        key1: ByteArray,
        key2: ByteArray,
    ): ByteArray {
        val point1 = curveParams.curve.decodePoint(key1)
        val point2 = curveParams.curve.decodePoint(key2)
        val sum = point1.add(point2).normalize()
        return sum.getEncoded(true)
    }

    override fun secp256k1AddPrivateKeys(
        key1: ByteArray,
        key2: ByteArray,
    ): ByteArray {
        val a = BigInteger(1, key1)
        val b = BigInteger(1, key2)
        val sum = a.add(b).mod(curveParams.n)
        val bytes = sum.toByteArray()
        // Ensure exactly 32 bytes: strip leading zero or left-pad
        return when {
            bytes.size == 32 -> bytes
            bytes.size > 32 -> bytes.copyOfRange(bytes.size - 32, bytes.size)
            else -> ByteArray(32 - bytes.size) + bytes
        }
    }

    // ── DER encoding/decoding ───────────────────────────────────────────

    private fun derEncode(
        r: BigInteger,
        s: BigInteger,
    ): ByteArray {
        val seq = DERSequence(arrayOf(ASN1Integer(r), ASN1Integer(s)))
        return seq.encoded
    }

    private fun derDecode(derSignature: ByteArray): Pair<BigInteger, BigInteger> {
        val seq = ASN1Sequence.getInstance(derSignature)
        val r = ASN1Integer.getInstance(seq.getObjectAt(0)).value
        val s = ASN1Integer.getInstance(seq.getObjectAt(1)).value
        return Pair(r, s)
    }
}

public actual fun platformCryptoProvider(): CryptoProvider = JvmCryptoProvider
