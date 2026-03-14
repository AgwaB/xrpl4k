@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.XAddress
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for XAddressCodec — X-Address encoding and decoding.
 *
 * Reference vectors from xrpl.js/packages/ripple-address-codec/test/index.test.ts
 */
class XAddressCodecTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── Mainnet encoding/decoding ───────────────────────────────────────

    test("encode mainnet X-address without tag") {
        val xAddress =
            XAddressCodec.encode(
                Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
                tag = null,
                isTest = false,
                provider,
            )
        xAddress shouldBe XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ")
    }

    test("encode mainnet X-address with tag=1") {
        val xAddress =
            XAddressCodec.encode(
                Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
                tag = 1u,
                isTest = false,
                provider,
            )
        xAddress shouldBe XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaGZMhc9YTE92ehJ2Fu")
    }

    test("encode mainnet X-address with tag=14") {
        val xAddress =
            XAddressCodec.encode(
                Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
                tag = 14u,
                isTest = false,
                provider,
            )
        xAddress shouldBe XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaGo2K5VpXpmCqbV2gS")
    }

    test("encode mainnet X-address with tag=11747") {
        val xAddress =
            XAddressCodec.encode(
                Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
                tag = 11747u,
                isTest = false,
                provider,
            )
        xAddress shouldBe XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaLFuhLRuNXPrDeJd9A")
    }

    // ── Testnet encoding/decoding ───────────────────────────────────────

    test("encode testnet X-address without tag") {
        val xAddress =
            XAddressCodec.encode(
                Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
                tag = null,
                isTest = true,
                provider,
            )
        xAddress shouldBe XAddress("T719a5UwUCnEs54UsxG9CJYYDhwmFCqkr7wxCcNcfZ6p5GZ")
    }

    // ── Tag=null vs tag=0 produce different X-addresses ──────────────────

    test("tag=null and tag=0 produce different X-addresses") {
        val noTag =
            XAddressCodec.encode(
                Address("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
                tag = null,
                isTest = false,
                provider,
            )
        val zeroTag =
            XAddressCodec.encode(
                Address("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
                tag = 0u,
                isTest = false,
                provider,
            )
        noTag shouldBe XAddress("XVLhHMPHU98es4dbozjVtdWzVrDjtV5fdx1mHp98tDMoQXb")
        zeroTag shouldBe XAddress("XVLhHMPHU98es4dbozjVtdWzVrDjtV8AqEL4xcZj5whKbmc")
        (noTag != zeroTag) shouldBe true
    }

    // ── Max tag value ────────────────────────────────────────────────────

    test("encode mainnet X-address with max UInt32 tag") {
        val xAddress =
            XAddressCodec.encode(
                Address("rGWrZyQqhTp9Xu7G5Pkayo7bXjH4k4QYpf"),
                tag = 4294967295u,
                isTest = false,
                provider,
            )
        xAddress shouldBe XAddress("XVLhHMPHU98es4dbozjVtdWzVrDjtV18pX8yuPT7y4xaEHi")
    }

    // ── Decode roundtrip ─────────────────────────────────────────────────

    test("decode mainnet X-address without tag") {
        val components =
            XAddressCodec.decode(
                XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ"),
                provider,
            )
        components.classicAddress shouldBe Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")
        components.tag shouldBe null
        components.isTest shouldBe false
    }

    test("decode mainnet X-address with tag") {
        val components =
            XAddressCodec.decode(
                XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaGZMhc9YTE92ehJ2Fu"),
                provider,
            )
        components.classicAddress shouldBe Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")
        components.tag shouldBe 1u
        components.isTest shouldBe false
    }

    test("decode testnet X-address") {
        val components =
            XAddressCodec.decode(
                XAddress("T719a5UwUCnEs54UsxG9CJYYDhwmFCqkr7wxCcNcfZ6p5GZ"),
                provider,
            )
        components.classicAddress shouldBe Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")
        components.tag shouldBe null
        components.isTest shouldBe true
    }

    // ── Full roundtrip ──────────────────────────────────────────────────

    test("encode/decode roundtrip with tag") {
        val address = Address("rpZc4mVfWUif9CRoHRKKcmhu1nx2xktxBo")
        val tag = 58u
        val xAddress = XAddressCodec.encode(address, tag, isTest = false, provider)
        xAddress shouldBe XAddress("X7YenJqxv3L66CwhBSfd3N8RzGXxYqV56ZkTCa9UCzgaao1")
        val components = XAddressCodec.decode(xAddress, provider)
        components.classicAddress shouldBe address
        components.tag shouldBe tag
        components.isTest shouldBe false
    }

    // ── Extension functions ──────────────────────────────────────────────

    test("XAddress.classicAddress() extension works") {
        val xAddress = XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ")
        xAddress.classicAddress(provider) shouldBe Address("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")
    }

    test("XAddress.destinationTag() extension returns null for no-tag") {
        val xAddress = XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ")
        xAddress.destinationTag(provider) shouldBe null
    }

    test("XAddress.destinationTag() extension returns tag value") {
        val xAddress = XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaGZMhc9YTE92ehJ2Fu")
        xAddress.destinationTag(provider) shouldBe 1u
    }

    test("XAddress.isTest() extension returns false for mainnet") {
        val xAddress = XAddress("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ")
        xAddress.isTest(provider) shouldBe false
    }

    test("XAddress.isTest() extension returns true for testnet") {
        val xAddress = XAddress("T719a5UwUCnEs54UsxG9CJYYDhwmFCqkr7wxCcNcfZ6p5GZ")
        xAddress.isTest(provider) shouldBe true
    }
})
