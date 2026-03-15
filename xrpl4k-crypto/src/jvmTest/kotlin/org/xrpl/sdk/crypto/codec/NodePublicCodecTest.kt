@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for encodeNodePublic, decodeNodePublic, and deriveNodeAddress.
 *
 * Reference vectors from xrpl.js/packages/ripple-address-codec/test/xrp-codec.test.ts
 * and xrpl.js/packages/ripple-keypairs/test/api.test.ts.
 */
class NodePublicCodecTest : FunSpec({

    val provider = platformCryptoProvider()

    // Known vector from xrpl.js xrp-codec.test.ts
    val knownHex = "0388E5BA87A000CB807240DF8C848EB0B5FFA5C8E5A521BC8E105C0F0A44217828"
    val knownEncoded = "n9MXXueo837zYH36DvMc13BwHcqtfAWNJY5czWVbp7uYTj7x17TH"

    test("encodeNodePublic produces known encoded string") {
        val publicKeyBytes = knownHex.hexToByteArray()
        val encoded = AddressCodec.encodeNodePublic(publicKeyBytes, provider)
        encoded shouldBe knownEncoded
    }

    test("decodeNodePublic returns correct bytes") {
        val decoded = AddressCodec.decodeNodePublic(knownEncoded, provider)
        decoded.toHexString().uppercase() shouldBe knownHex
    }

    test("encodeNodePublic/decodeNodePublic roundtrip") {
        val publicKeyBytes = knownHex.hexToByteArray()
        val encoded = AddressCodec.encodeNodePublic(publicKeyBytes, provider)
        val decoded = AddressCodec.decodeNodePublic(encoded, provider)
        decoded shouldBe publicKeyBytes
    }

    test("encodeNodePublic result starts with n") {
        val publicKeyBytes = knownHex.hexToByteArray()
        val encoded = AddressCodec.encodeNodePublic(publicKeyBytes, provider)
        encoded shouldStartWith "n"
    }

    test("encodeNodePublic with wrong key length throws") {
        val result =
            runCatching {
                AddressCodec.encodeNodePublic(ByteArray(32), provider)
            }
        result.isFailure shouldBe true
    }

    // deriveNodeAddress test vector from xrpl.js/packages/ripple-keypairs/test/api.test.ts
    test("deriveNodeAddress produces known address") {
        val nodePublic = "n9KHn8NfbBsZV5q8bLfS72XyGqwFt5mgoPbcTV4c6qKiuPTAtXYk"
        val expectedAddress = Address("rU7bM9ENDkybaxNrefAVjdLTyNLuue1KaJ")
        val address = AddressCodec.deriveNodeAddress(nodePublic, provider)
        address shouldBe expectedAddress
    }

    test("deriveNodeAddress result starts with r") {
        val nodePublic = "n9KHn8NfbBsZV5q8bLfS72XyGqwFt5mgoPbcTV4c6qKiuPTAtXYk"
        val address = AddressCodec.deriveNodeAddress(nodePublic, provider)
        address.value shouldStartWith "r"
    }
})
