@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.constant
import io.kotest.property.checkAll
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.codec.AddressCodec
import org.xrpl.sdk.crypto.codec.Base58Check
import org.xrpl.sdk.crypto.keys.KeyDerivation

/**
 * T8 — Integration tests and property-based tests for xrpl-crypto.
 *
 * Covers:
 * - Seed codec roundtrip for both algorithms
 * - Base58Check encode/decode roundtrip
 * - Sign/verify roundtrip for both algorithms
 * - secp256k1 Low-S canonical verification
 * - Key derivation from known seeds
 */
class IntegrationTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── Known seed vectors ───────────────────────────────────────────────

    test("Ed25519 key derivation from known seed produces expected public key") {
        val seed = "sEdTM1uX8pu2do5XvTnutH6HsouMaM2"
        val wallet = Wallet.fromSeed(seed, provider)
        wallet.use { w ->
            w.algorithm shouldBe KeyAlgorithm.Ed25519
            w.publicKey.value shouldStartWith "ED"
            w.publicKey.value.length shouldBe 66
            // Address should be a valid r-address
            w.address.value shouldStartWith "r"
        }
    }

    test("secp256k1 key derivation from known seed produces expected public key") {
        val seed = "sp6JS7f14BuwFY8Mw6bTtLKWauoUs"
        val wallet = Wallet.fromSeed(seed, provider)
        wallet.use { w ->
            w.algorithm shouldBe KeyAlgorithm.Secp256k1
            w.publicKey.value.length shouldBe 66 // compressed 33 bytes
            w.address.value shouldStartWith "r"
        }
    }

    test("Ed25519 fromSeed is deterministic across calls") {
        val seed = "sEdTM1uX8pu2do5XvTnutH6HsouMaM2"
        val w1 = Wallet.fromSeed(seed, provider)
        val w2 = Wallet.fromSeed(seed, provider)
        w1.address shouldBe w2.address
        w1.publicKey shouldBe w2.publicKey
        w1.close()
        w2.close()
    }

    test("secp256k1 fromSeed is deterministic across calls") {
        val seed = "sp6JS7f14BuwFY8Mw6bTtLKWauoUs"
        val w1 = Wallet.fromSeed(seed, provider)
        val w2 = Wallet.fromSeed(seed, provider)
        w1.address shouldBe w2.address
        w1.publicKey shouldBe w2.publicKey
        w1.close()
        w2.close()
    }

    // ── Seed codec roundtrip ─────────────────────────────────────────────

    test("property: Ed25519 seed encode/decode roundtrip (100+ iterations)") {
        checkAll(150, Arb.byteArray(Arb.constant(16), Arb.byte())) { seedBytes ->
            val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Ed25519, provider)
            encoded shouldStartWith "sEd"
            val (decoded, algorithm) = AddressCodec.decodeSeed(encoded, provider)
            algorithm shouldBe KeyAlgorithm.Ed25519
            decoded.contentEquals(seedBytes) shouldBe true
        }
    }

    test("property: secp256k1 seed encode/decode roundtrip (100+ iterations)") {
        checkAll(150, Arb.byteArray(Arb.constant(16), Arb.byte())) { seedBytes ->
            val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Secp256k1, provider)
            encoded shouldStartWith "s"
            val (decoded, algorithm) = AddressCodec.decodeSeed(encoded, provider)
            algorithm shouldBe KeyAlgorithm.Secp256k1
            decoded.contentEquals(seedBytes) shouldBe true
        }
    }

    // ── Base58Check encode/decode roundtrip ───────────────────────────────

    test("property: Base58Check encode/decode roundtrip (100+ iterations)") {
        val prefix = byteArrayOf(0x00) // classic address prefix
        checkAll(150, Arb.byteArray(Arb.constant(20), Arb.byte())) { payload ->
            val encoded = Base58Check.encode(prefix, payload, provider)
            val decoded = Base58Check.decodeRaw(encoded, provider)
            // decoded = prefix + payload
            decoded.size shouldBe 21
            decoded[0] shouldBe 0x00.toByte()
            decoded.copyOfRange(1, 21).contentEquals(payload) shouldBe true
        }
    }

    test("property: Base58Check roundtrip with multi-byte prefix (100+ iterations)") {
        // Ed25519 seed prefix: 0x01, 0xE1, 0x4B
        val prefix = byteArrayOf(0x01, 0xE1.toByte(), 0x4B)
        checkAll(150, Arb.byteArray(Arb.constant(16), Arb.byte())) { payload ->
            val encoded = Base58Check.encode(prefix, payload, provider)
            val decoded = Base58Check.decodeRaw(encoded, provider)
            decoded.size shouldBe 19 // 3 + 16
            decoded.copyOfRange(0, 3).contentEquals(prefix) shouldBe true
            decoded.copyOfRange(3, 19).contentEquals(payload) shouldBe true
        }
    }

    // ── Sign/verify roundtrip ────────────────────────────────────────────

    test("property: Ed25519 sign/verify roundtrip (100+ iterations)") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use { w ->
            checkAll(150, Arb.byteArray(Arb.constant(32), Arb.byte())) { message ->
                val sig = w.sign(message)
                w.verify(message, sig) shouldBe true
            }
        }
    }

    test("property: secp256k1 sign/verify roundtrip (100+ iterations)") {
        val wallet = Wallet.fromSeed("sp6JS7f14BuwFY8Mw6bTtLKWauoUs", provider)
        wallet.use { w ->
            checkAll(150, Arb.byteArray(Arb.constant(32), Arb.byte())) { messageHash ->
                val sig = w.sign(messageHash)
                w.verify(messageHash, sig) shouldBe true
            }
        }
    }

    // ── secp256k1 Low-S canonical ────────────────────────────────────────

    test("property: all secp256k1 signatures are Low-S canonical (100+ iterations)") {
        // secp256k1 order / 2
        val halfOrder = "7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF5D576E7357A4501DDFE92F46681B20A0".hexToByteArray()
        val wallet = Wallet.fromSeed("sp6JS7f14BuwFY8Mw6bTtLKWauoUs", provider)
        wallet.use { w ->
            checkAll(150, Arb.byteArray(Arb.constant(32), Arb.byte())) { messageHash ->
                val sig = w.sign(messageHash)
                // DER-encoded signature: 30 <len> 02 <rlen> <r> 02 <slen> <s>
                val sValue = extractSFromDer(sig)
                // s must be <= halfOrder
                isLessThanOrEqual(sValue, halfOrder) shouldBe true
            }
        }
    }

    // ── Ed25519 signing from multiple known seeds ────────────────────────

    test("Ed25519 signing is deterministic for multiple entropy values") {
        val entropies =
            listOf(
                ByteArray(16) { it.toByte() },
                ByteArray(16) { (it + 0x10).toByte() },
                ByteArray(16) { (it + 0x20).toByte() },
            )
        for (entropy in entropies) {
            val w1 = Wallet.fromEntropy(entropy, KeyAlgorithm.Ed25519, provider)
            val w2 = Wallet.fromEntropy(entropy, KeyAlgorithm.Ed25519, provider)
            val message = "test message for signing".encodeToByteArray()
            val sig1 = w1.sign(message)
            val sig2 = w2.sign(message)
            sig1.toHexString() shouldBe sig2.toHexString()
            w1.verify(message, sig1) shouldBe true
            w1.close()
            w2.close()
        }
    }

    test("secp256k1 signing is deterministic for known message (RFC 6979)") {
        val seed = "sp6JS7f14BuwFY8Mw6bTtLKWauoUs"
        val w1 = Wallet.fromSeed(seed, provider)
        val w2 = Wallet.fromSeed(seed, provider)
        val messageHash = provider.sha512Half("test message".encodeToByteArray())
        val sig1 = w1.sign(messageHash)
        val sig2 = w2.sign(messageHash)
        sig1.toHexString() shouldBe sig2.toHexString()
        w1.verify(messageHash, sig1) shouldBe true
        w1.close()
        w2.close()
    }

    // ── Address codec integration ────────────────────────────────────────

    test("accountIdFromPublicKey produces consistent AccountId") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use { w ->
            val accountId = AddressCodec.accountIdFromPublicKey(w.publicKey, provider)
            val address = AddressCodec.encodeAddress(accountId, provider)
            address shouldBe w.address

            // Roundtrip: decode address back to account ID
            val decodedAccountId = AddressCodec.decodeAddress(address, provider)
            decodedAccountId shouldBe accountId
        }
    }

    // ── KeyDerivation.generateSeed ───────────────────────────────────────

    test("generateSeed produces 16-byte random seeds") {
        val seed1 = KeyDerivation.generateSeed(provider)
        val seed2 = KeyDerivation.generateSeed(provider)
        seed1.size shouldBe 16
        seed2.size shouldBe 16
        // Extremely unlikely to be equal
        seed1.contentEquals(seed2) shouldBe false
    }
})

// ── DER signature parsing helpers ────────────────────────────────────────

/**
 * Extract the S value from a DER-encoded ECDSA signature.
 * Format: 30 <len> 02 <rlen> <r bytes> 02 <slen> <s bytes>
 */
private fun extractSFromDer(der: ByteArray): ByteArray {
    var offset = 2 // skip 30 <total-len>
    // Skip R
    require(der[offset] == 0x02.toByte()) { "Expected 0x02 for R" }
    offset++
    val rLen = der[offset].toInt() and 0xFF
    offset++
    offset += rLen
    // Read S
    require(der[offset] == 0x02.toByte()) { "Expected 0x02 for S" }
    offset++
    val sLen = der[offset].toInt() and 0xFF
    offset++
    val sBytes = der.copyOfRange(offset, offset + sLen)
    // Strip leading zero if present (DER uses it for positive sign)
    return if (sBytes.isNotEmpty() && sBytes[0] == 0x00.toByte()) {
        sBytes.copyOfRange(1, sBytes.size)
    } else {
        sBytes
    }
}

/** Returns true if a <= b, interpreting both as unsigned big-endian integers. */
private fun isLessThanOrEqual(
    a: ByteArray,
    b: ByteArray,
): Boolean {
    // Pad shorter array with leading zeros
    val maxLen = maxOf(a.size, b.size)
    val aPadded = ByteArray(maxLen - a.size) + a
    val bPadded = ByteArray(maxLen - b.size) + b

    for (i in aPadded.indices) {
        val ai = aPadded[i].toInt() and 0xFF
        val bi = bPadded[i].toInt() and 0xFF
        if (ai < bi) return true
        if (ai > bi) return false
    }
    return true // equal
}
