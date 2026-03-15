@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.AccountId
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.type.PublicKey
import org.xrpl.sdk.crypto.Wallet
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Known-vector tests for AddressCodec.
 *
 * Covers functionality NOT already in AddressCodecTest:
 * - isValidClassicAddress with well-known hardcoded addresses
 * - accountIdFromPublicKey with exact known vectors
 * - decodeAddress exact output verification
 * - Address/AccountId roundtrip from known public keys
 * - encodeSeed/decodeSeed edge cases
 */
class AddressCodecKnownVectorTest : FunSpec({
    val provider = platformCryptoProvider()

    // ── isValidClassicAddress with specific known addresses ─────────────

    test("isValidClassicAddress accepts genesis address") {
        isValidClassicAddress("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", provider) shouldBe true
    }

    test("isValidClassicAddress accepts known Ripple address") {
        isValidClassicAddress("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN", provider) shouldBe true
    }

    test("isValidClassicAddress rejects address with invalid character '0' (not in Base58)") {
        // '0' is not in the XRP Base58 alphabet
        isValidClassicAddress("r0000000000000000000000000000000", provider) shouldBe false
    }

    test("isValidClassicAddress rejects address with 'O' (not in Base58)") {
        isValidClassicAddress("rOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO", provider) shouldBe false
    }

    test("isValidClassicAddress rejects address with 'I' (not in Base58)") {
        isValidClassicAddress("rIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII", provider) shouldBe false
    }

    test("isValidClassicAddress rejects address with 'l' (not in Base58)") {
        isValidClassicAddress("rlllllllllllllllllllllllllllllll", provider) shouldBe false
    }

    test("isValidClassicAddress rejects too-short address") {
        isValidClassicAddress("rShort", provider) shouldBe false
    }

    test("isValidClassicAddress rejects address not starting with 'r'") {
        // X-addresses start with X, not r
        isValidClassicAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ", provider) shouldBe false
    }

    // ── accountIdFromPublicKey with exact known vectors ──────────────────

    test("accountIdFromPublicKey for known Ed25519 public key") {
        // Known from xrpl.js fixtures: seed [1,2,3,...,16]
        // Ed25519 public key: ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63
        val publicKey = PublicKey("ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63")
        val accountId = AddressCodec.accountIdFromPublicKey(publicKey, provider)

        // Verify that the resulting address is valid
        val address = AddressCodec.encodeAddress(accountId, provider)
        isValidClassicAddress(address.value, provider) shouldBe true
        address.value shouldBe
            Wallet.fromEntropy(
                ByteArray(16) { (it + 1).toByte() },
                KeyAlgorithm.Ed25519,
                provider,
            ).use { it.address.value }
    }

    test("accountIdFromPublicKey for known Secp256k1 public key") {
        // Known from xrpl.js fixtures:
        // secp256k1 public key: 030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435
        val publicKey = PublicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
        val accountId = AddressCodec.accountIdFromPublicKey(publicKey, provider)

        // AccountId should be 20 bytes (40 hex chars)
        accountId.hex.length shouldBe 40

        // Roundtrip: encode to address, decode back to accountId
        val address = AddressCodec.encodeAddress(accountId, provider)
        val decoded = AddressCodec.decodeAddress(address, provider)
        decoded shouldBe accountId
    }

    // ── decodeAddress exact AccountId hex for known address ─────────────

    test("decodeAddress returns exact AccountId hex for rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN") {
        // Known from xrpl.js: BA8E78626EE42C41B46D46C3048DF3A1C3C87072
        val address = Address("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN")
        val accountId = AddressCodec.decodeAddress(address, provider)
        accountId.hex shouldBe "ba8e78626ee42c41b46d46c3048df3a1c3c87072"
    }

    // ── Address / AccountId full roundtrip from various AccountIds ──────

    test("roundtrip for all-zeros AccountId") {
        val accountId = AccountId("0000000000000000000000000000000000000000")
        val address = AddressCodec.encodeAddress(accountId, provider)
        address.value shouldBe "rrrrrrrrrrrrrrrrrrrrrhoLvTp"
        val decoded = AddressCodec.decodeAddress(address, provider)
        decoded shouldBe accountId
    }

    test("roundtrip for all-FF AccountId") {
        val accountId = AccountId("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")
        val address = AddressCodec.encodeAddress(accountId, provider)
        val decoded = AddressCodec.decodeAddress(address, provider)
        decoded shouldBe accountId
    }

    // ── encodeSeed / decodeSeed additional edge cases ────────────────────

    test("decodeSeed for sEdSJHS4oiAdz7w2X2ni1gFiqtbJHqE returns all-zeros Ed25519") {
        val (bytes, algorithm) = AddressCodec.decodeSeed("sEdSJHS4oiAdz7w2X2ni1gFiqtbJHqE", provider)
        bytes shouldBe ByteArray(16)
        algorithm shouldBe KeyAlgorithm.Ed25519
    }

    test("decodeSeed for sp6JS7f14BuwFY8Mw6bTtLKWauoUs returns all-zeros Secp256k1") {
        val (bytes, algorithm) = AddressCodec.decodeSeed("sp6JS7f14BuwFY8Mw6bTtLKWauoUs", provider)
        bytes shouldBe ByteArray(16)
        algorithm shouldBe KeyAlgorithm.Secp256k1
    }

    test("decodeSeed rejects completely invalid string") {
        val result = runCatching { AddressCodec.decodeSeed("notASeed", provider) }
        result.isFailure shouldBe true
    }

    test("decodeSeed rejects empty string") {
        val result = runCatching { AddressCodec.decodeSeed("", provider) }
        result.isFailure shouldBe true
    }

    // ── Seed encode/decode roundtrip with various byte patterns ─────────

    test("seed roundtrip with alternating byte pattern Ed25519") {
        val seedBytes = ByteArray(16) { if (it % 2 == 0) 0xAA.toByte() else 0x55.toByte() }
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Ed25519, provider)
        val (decoded, algorithm) = AddressCodec.decodeSeed(encoded, provider)
        decoded shouldBe seedBytes
        algorithm shouldBe KeyAlgorithm.Ed25519
    }

    test("seed roundtrip with alternating byte pattern Secp256k1") {
        val seedBytes = ByteArray(16) { if (it % 2 == 0) 0xAA.toByte() else 0x55.toByte() }
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Secp256k1, provider)
        val (decoded, algorithm) = AddressCodec.decodeSeed(encoded, provider)
        decoded shouldBe seedBytes
        algorithm shouldBe KeyAlgorithm.Secp256k1
    }
})
