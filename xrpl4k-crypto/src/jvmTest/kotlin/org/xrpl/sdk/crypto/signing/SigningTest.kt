@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.signing

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import org.xrpl.sdk.core.model.transaction.Signer
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm

/**
 * Tests for signing primitives: PrivateKey and SingleSignature.
 *
 * Covers:
 * - PrivateKey.close() zeros key bytes
 * - PrivateKey.useBytes() provides scoped access
 * - PrivateKey.toString() does not expose key material
 * - PrivateKey.equals() and hashCode()
 * - SingleSignature equality and toString
 */
class SigningTest : FunSpec({

    // ── PrivateKey.close() ──────────────────────────────────────────────

    test("PrivateKey.close() zeros key bytes") {
        val keyBytes = ByteArray(32) { (it + 1).toByte() }
        val pk = PrivateKey(keyBytes, KeyAlgorithm.Ed25519)

        // Before close: should have non-zero data
        keyBytes.any { it != 0.toByte() } shouldBe true

        pk.close()

        // After close: all bytes should be zero
        keyBytes.all { it == 0.toByte() } shouldBe true
    }

    test("PrivateKey.use {} closes on normal exit") {
        val keyBytes = ByteArray(32) { (it + 1).toByte() }
        val pk = PrivateKey(keyBytes, KeyAlgorithm.Ed25519)

        pk.use {
            it.algorithm shouldBe KeyAlgorithm.Ed25519
        }

        keyBytes.all { it == 0.toByte() } shouldBe true
    }

    test("PrivateKey.use {} closes on exception") {
        val keyBytes = ByteArray(32) { (it + 1).toByte() }
        val pk = PrivateKey(keyBytes, KeyAlgorithm.Ed25519)

        runCatching {
            pk.use { error("simulated") }
        }

        keyBytes.all { it == 0.toByte() } shouldBe true
    }

    // ── PrivateKey.useBytes() ───────────────────────────────────────────

    test("PrivateKey.useBytes() provides scoped byte access") {
        val keyBytes = ByteArray(32) { (it + 1).toByte() }
        val pk = PrivateKey(keyBytes.copyOf(), KeyAlgorithm.Ed25519)

        val result =
            pk.useBytes { bytes ->
                bytes.size
            }

        pk.close()
        result shouldBe 32
    }

    // ── PrivateKey.toString() ───────────────────────────────────────────

    test("PrivateKey.toString() does not contain key bytes") {
        val keyBytes = ByteArray(32) { (it + 1).toByte() }
        val pk = PrivateKey(keyBytes, KeyAlgorithm.Ed25519)
        pk.use {
            val str = it.toString()
            str shouldNotContain "01020304"
            str shouldNotContain "bytes"
            str shouldNotContain "privateKey"
        }
    }

    test("PrivateKey.toString() contains algorithm info") {
        val pk = PrivateKey(ByteArray(32), KeyAlgorithm.Secp256k1)
        pk.use {
            val str = it.toString()
            (str.contains("Secp256k1")) shouldBe true
        }
    }

    // ── PrivateKey.equals() ─────────────────────────────────────────────

    test("identical PrivateKeys are equal") {
        val bytes1 = ByteArray(32) { (it + 1).toByte() }
        val bytes2 = ByteArray(32) { (it + 1).toByte() }
        val pk1 = PrivateKey(bytes1, KeyAlgorithm.Ed25519)
        val pk2 = PrivateKey(bytes2, KeyAlgorithm.Ed25519)
        pk1.use { a ->
            pk2.use { b ->
                (a == b) shouldBe true
            }
        }
    }

    test("PrivateKeys with different bytes are not equal") {
        val pk1 = PrivateKey(ByteArray(32) { 1 }, KeyAlgorithm.Ed25519)
        val pk2 = PrivateKey(ByteArray(32) { 2 }, KeyAlgorithm.Ed25519)
        pk1.use { a ->
            pk2.use { b ->
                (a == b) shouldBe false
            }
        }
    }

    test("PrivateKeys with different algorithms are not equal") {
        val bytes = ByteArray(32) { (it + 1).toByte() }
        val pk1 = PrivateKey(bytes.copyOf(), KeyAlgorithm.Ed25519)
        val pk2 = PrivateKey(bytes.copyOf(), KeyAlgorithm.Secp256k1)
        pk1.use { a ->
            pk2.use { b ->
                (a == b) shouldBe false
            }
        }
    }

    test("PrivateKey is not equal to null or different type") {
        val pk = PrivateKey(ByteArray(32), KeyAlgorithm.Ed25519)
        pk.use {
            (it.equals(null)) shouldBe false
            (it.equals("string")) shouldBe false
        }
    }

    test("PrivateKey referential equality returns true") {
        val pk = PrivateKey(ByteArray(32), KeyAlgorithm.Ed25519)
        pk.use {
            (it == it) shouldBe true
        }
    }

    // ── PrivateKey.hashCode() ───────────────────────────────────────────

    test("equal PrivateKeys have same hashCode") {
        val pk1 = PrivateKey(ByteArray(32) { (it + 1).toByte() }, KeyAlgorithm.Ed25519)
        val pk2 = PrivateKey(ByteArray(32) { (it + 1).toByte() }, KeyAlgorithm.Ed25519)
        pk1.use { a ->
            pk2.use { b ->
                a.hashCode() shouldBe b.hashCode()
            }
        }
    }

    // ── SingleSignature ─────────────────────────────────────────────────

    test("SingleSignature equality for identical signers") {
        val signer1 =
            Signer(
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                txnSignature = "AABBCCDD",
                signingPubKey = "02DEADBEEF",
            )
        val signer2 =
            Signer(
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                txnSignature = "AABBCCDD",
                signingPubKey = "02DEADBEEF",
            )
        val sig1 = SingleSignature(signer1)
        val sig2 = SingleSignature(signer2)
        (sig1 == sig2) shouldBe true
        sig1.hashCode() shouldBe sig2.hashCode()
    }

    test("SingleSignature inequality for different txnSignature") {
        val signer1 =
            Signer(
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                txnSignature = "AABBCCDD",
                signingPubKey = "02DEADBEEF",
            )
        val signer2 =
            Signer(
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                txnSignature = "11223344",
                signingPubKey = "02DEADBEEF",
            )
        val sig1 = SingleSignature(signer1)
        val sig2 = SingleSignature(signer2)
        (sig1 == sig2) shouldBe false
    }

    test("SingleSignature inequality for different account") {
        val signer1 =
            Signer(
                account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                txnSignature = "AABBCCDD",
                signingPubKey = "02DEADBEEF",
            )
        val signer2 =
            Signer(
                account = Address("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN"),
                txnSignature = "AABBCCDD",
                signingPubKey = "02DEADBEEF",
            )
        val sig1 = SingleSignature(signer1)
        val sig2 = SingleSignature(signer2)
        (sig1 == sig2) shouldBe false
    }

    test("SingleSignature is not equal to null or different type") {
        val sig =
            SingleSignature(
                Signer(
                    account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    txnSignature = "AABB",
                    signingPubKey = "02DEAD",
                ),
            )
        (sig.equals(null)) shouldBe false
        (sig.equals("not a signature")) shouldBe false
    }

    test("SingleSignature referential equality") {
        val sig =
            SingleSignature(
                Signer(
                    account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    txnSignature = "AABB",
                    signingPubKey = "02DEAD",
                ),
            )
        (sig == sig) shouldBe true
    }

    test("SingleSignature.toString() contains signer info") {
        val sig =
            SingleSignature(
                Signer(
                    account = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
                    txnSignature = "AABB",
                    signingPubKey = "02DEAD",
                ),
            )
        val str = sig.toString()
        (str.contains("SingleSignature")) shouldBe true
        (str.contains("signer")) shouldBe true
    }
})
