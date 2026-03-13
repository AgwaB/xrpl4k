@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.AccountId
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.KeyAlgorithm
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for AddressCodec — address encoding, seed encoding, and Account ID derivation.
 *
 * Reference vectors from xrpl.js/packages/ripple-address-codec/test/xrp-codec.test.ts
 */
class AddressCodecTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── Address encoding/decoding ───────────────────────────────────────

    test("encodeAddress produces known r-address for reference Account ID") {
        // xrpl.js: BA8E78626EE42C41B46D46C3048DF3A1C3C87072 -> rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN
        val accountId = AccountId("BA8E78626EE42C41B46D46C3048DF3A1C3C87072")
        val address = AddressCodec.encodeAddress(accountId, provider)
        address shouldBe Address("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN")
    }

    test("decodeAddress returns correct Account ID with lowercase hex") {
        val address = Address("rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN")
        val accountId = AddressCodec.decodeAddress(address, provider)
        accountId.hex shouldBe "ba8e78626ee42c41b46d46c3048df3a1c3c87072"
    }

    test("encodeAddress/decodeAddress roundtrip") {
        val accountId = AccountId("BA8E78626EE42C41B46D46C3048DF3A1C3C87072")
        val address = AddressCodec.encodeAddress(accountId, provider)
        val decoded = AddressCodec.decodeAddress(address, provider)
        decoded shouldBe accountId
    }

    // ── Seed encoding (secp256k1) ───────────────────────────────────────

    test("encodeSeed secp256k1 produces known seed string") {
        // xrpl.js: CF2DE378FBDD7E2EE87D486DFB5A7BFF -> sn259rEFXrQrWyx3Q7XneWcwV6dfL
        val seedBytes = "CF2DE378FBDD7E2EE87D486DFB5A7BFF".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Secp256k1, provider)
        encoded shouldBe "sn259rEFXrQrWyx3Q7XneWcwV6dfL"
    }

    test("encodeSeed secp256k1 low seed (all zeros)") {
        val seedBytes = "00000000000000000000000000000000".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Secp256k1, provider)
        encoded shouldBe "sp6JS7f14BuwFY8Mw6bTtLKWauoUs"
    }

    test("encodeSeed secp256k1 high seed (all FFs)") {
        val seedBytes = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Secp256k1, provider)
        encoded shouldBe "saGwBRReqUNKuWNLpUAq8i8NkXEPN"
    }

    // ── Seed encoding (Ed25519) ─────────────────────────────────────────

    test("encodeSeed Ed25519 produces known seed string starting with sEd") {
        // xrpl.js: 4C3A1D213FBDFB14C7C28D609469B341 -> sEdTM1uX8pu2do5XvTnutH6HsouMaM2
        val seedBytes = "4C3A1D213FBDFB14C7C28D609469B341".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Ed25519, provider)
        encoded shouldBe "sEdTM1uX8pu2do5XvTnutH6HsouMaM2"
    }

    test("encodeSeed Ed25519 low seed (all zeros)") {
        val seedBytes = "00000000000000000000000000000000".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Ed25519, provider)
        encoded shouldBe "sEdSJHS4oiAdz7w2X2ni1gFiqtbJHqE"
    }

    test("encodeSeed Ed25519 high seed (all FFs)") {
        val seedBytes = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(seedBytes, KeyAlgorithm.Ed25519, provider)
        encoded shouldBe "sEdV19BLfeQeKdEXyYA4NhjPJe6XBfG"
    }

    // ── Seed decoding ───────────────────────────────────────────────────

    test("decodeSeed Ed25519 returns correct bytes and algorithm") {
        val (bytes, algorithm) = AddressCodec.decodeSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        bytes.toHexString() shouldBe "4c3a1d213fbdfb14c7c28d609469b341"
        algorithm shouldBe KeyAlgorithm.Ed25519
    }

    test("decodeSeed secp256k1 returns correct bytes and algorithm") {
        val (bytes, algorithm) = AddressCodec.decodeSeed("sn259rEFXrQrWyx3Q7XneWcwV6dfL", provider)
        bytes.toHexString() shouldBe "cf2de378fbdd7e2ee87d486dfb5a7bff"
        algorithm shouldBe KeyAlgorithm.Secp256k1
    }

    // ── Seed roundtrip ──────────────────────────────────────────────────

    test("seed encode/decode roundtrip for Ed25519") {
        val original = "4C3A1D213FBDFB14C7C28D609469B341".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(original, KeyAlgorithm.Ed25519, provider)
        val (decoded, algorithm) = AddressCodec.decodeSeed(encoded, provider)
        decoded shouldBe original
        algorithm shouldBe KeyAlgorithm.Ed25519
    }

    test("seed encode/decode roundtrip for Secp256k1") {
        val original = "CF2DE378FBDD7E2EE87D486DFB5A7BFF".hexToByteArray()
        val encoded = AddressCodec.encodeSeed(original, KeyAlgorithm.Secp256k1, provider)
        val (decoded, algorithm) = AddressCodec.decodeSeed(encoded, provider)
        decoded shouldBe original
        algorithm shouldBe KeyAlgorithm.Secp256k1
    }

    // ── Account public key encoding ─────────────────────────────────────

    test("encodeAccountPublicKey produces known value") {
        // xrpl.js: 023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6
        //       -> aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3
        val pubKeyBytes = "023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6".hexToByteArray()
        val encoded = AddressCodec.encodeAccountPublicKey(pubKeyBytes, provider)
        encoded shouldBe "aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3"
    }

    test("decodeAccountPublicKey roundtrip") {
        val pubKeyBytes = "023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6".hexToByteArray()
        val encoded = AddressCodec.encodeAccountPublicKey(pubKeyBytes, provider)
        val decoded = AddressCodec.decodeAccountPublicKey(encoded, provider)
        decoded shouldBe pubKeyBytes
    }

    // ── Validation ──────────────────────────────────────────────────────

    test("encodeSeed with wrong seed length throws") {
        val result =
            runCatching {
                AddressCodec.encodeSeed(ByteArray(15), KeyAlgorithm.Ed25519, provider)
            }
        result.isFailure shouldBe true
    }

    test("encodeAccountPublicKey with wrong key length throws") {
        val result =
            runCatching {
                AddressCodec.encodeAccountPublicKey(ByteArray(32), provider)
            }
        result.isFailure shouldBe true
    }
})
