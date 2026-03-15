@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.xrpl.sdk.core.type.KeyAlgorithm

/**
 * Tests for Wallet.fromSecret(), Wallet.getXAddress(), Wallet.fromSecretNumbers(),
 * and Wallet.fromRfc1751Mnemonic().
 *
 * Reference vectors from xrpl.js tests.
 */
class WalletExtensionsTest : FunSpec({

    val provider = platformCryptoProvider()

    // ── fromSecret alias ────────────────────────────────────────────────

    test("fromSecret produces same wallet as fromSeed") {
        val seed = "sEdTM1uX8pu2do5XvTnutH6HsouMaM2"
        val fromSecret = Wallet.fromSecret(seed, provider)
        val fromSeed = Wallet.fromSeed(seed, provider)
        fromSecret.use { s ->
            fromSeed.use { d ->
                s.address shouldBe d.address
                s.publicKey shouldBe d.publicKey
                s.algorithm shouldBe d.algorithm
            }
        }
    }

    test("fromSecret works with secp256k1 seed") {
        val seed = "sn259rEFXrQrWyx3Q7XneWcwV6dfL"
        val wallet = Wallet.fromSecret(seed, provider)
        wallet.use {
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
            it.address.value shouldStartWith "r"
        }
    }

    // ── getXAddress ─────────────────────────────────────────────────────

    test("getXAddress returns mainnet X-Address starting with X") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use {
            val xAddress = it.getXAddress()
            xAddress shouldStartWith "X"
        }
    }

    test("getXAddress returns testnet X-Address starting with T") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use {
            val xAddress = it.getXAddress(isTestnet = true)
            xAddress shouldStartWith "T"
        }
    }

    test("getXAddress with tag returns valid X-Address") {
        val wallet = Wallet.fromSeed("sEdTM1uX8pu2do5XvTnutH6HsouMaM2", provider)
        wallet.use {
            val xAddress = it.getXAddress(tag = 12345u)
            xAddress shouldStartWith "X"
            // Verify it round-trips through XAddressCodec
            val decoded =
                org.xrpl.sdk.crypto.codec.XAddressCodec.decode(
                    org.xrpl.sdk.core.type.XAddress(xAddress),
                    provider,
                )
            decoded.classicAddress shouldBe it.address
            decoded.tag shouldBe 12345u
            decoded.isTest shouldBe false
        }
    }

    // ── fromSecretNumbers ───────────────────────────────────────────────

    test("fromSecretNumbers derives wallet with default secp256k1 algorithm") {
        // Test vector from xrpl.js/packages/xrpl/test/wallet/index.test.ts
        val secretNumbers = "399150 474506 009147 088773 432160 282843 253738 605430"
        val expectedPublicKey = "03BFC2F7AE242C3493187FA0B72BE97B2DF71194FB772E507FF9DEA0AD13CA1625"

        val wallet = Wallet.fromSecretNumbers(secretNumbers, provider = provider)
        wallet.use {
            it.publicKey.value shouldBe expectedPublicKey
            it.algorithm shouldBe KeyAlgorithm.Secp256k1
        }
    }

    test("fromSecretNumbers derives same wallet with different delimiter formats") {
        val withSpaces = "399150 474506 009147 088773 432160 282843 253738 605430"
        val withDashes = "399150-474506-009147-088773-432160-282843-253738-605430"

        val w1 = Wallet.fromSecretNumbers(withSpaces, provider = provider)
        val w2 = Wallet.fromSecretNumbers(withDashes, provider = provider)
        w1.use { a ->
            w2.use { b ->
                a.address shouldBe b.address
                a.publicKey shouldBe b.publicKey
            }
        }
    }

    test("fromSecretNumbers with invalid checksum throws") {
        // Change last digit to invalidate checksum
        val invalidSecret = "399151 474506 009147 088773 432160 282843 253738 605430"
        val result =
            runCatching {
                Wallet.fromSecretNumbers(invalidSecret, provider = provider)
            }
        result.isFailure shouldBe true
    }

    test("fromSecretNumbers with wrong group count throws") {
        val tooFew = "399150 474506 009147"
        val result =
            runCatching {
                Wallet.fromSecretNumbers(tooFew, provider = provider)
            }
        result.isFailure shouldBe true
    }

    // ── fromRfc1751Mnemonic ─────────────────────────────────────────────

    test("fromRfc1751Mnemonic with secp256k1 produces known seed") {
        // Test vector from xrpl.js/packages/xrpl/test/wallet/index.test.ts
        val mnemonic = "CAB BETH HANK BIRD MEND SIGN GILD ANY KERN HYDE CHAT STUB"
        val expectedSeed = "snVB4iTWYqsWZaj1hkvAy1QzqNbAg"

        val wallet = Wallet.fromRfc1751Mnemonic(mnemonic, KeyAlgorithm.Secp256k1, provider)
        wallet.use {
            // Verify the seed matches by encoding the entropy
            val entropy = org.xrpl.sdk.crypto.internal.Rfc1751.mnemonicToKey(mnemonic)
            val encodedSeed =
                org.xrpl.sdk.crypto.codec.AddressCodec.encodeSeed(
                    entropy,
                    KeyAlgorithm.Secp256k1,
                    provider,
                )
            encodedSeed shouldBe expectedSeed
        }
    }

    test("fromRfc1751Mnemonic with ed25519 produces known seed") {
        val mnemonic = "CAB BETH HANK BIRD MEND SIGN GILD ANY KERN HYDE CHAT STUB"
        val expectedSeed = "sEdVaw4m9W3H3ou3VnyvDwvPAP5BEz1"

        val wallet = Wallet.fromRfc1751Mnemonic(mnemonic, KeyAlgorithm.Ed25519, provider)
        wallet.use {
            val entropy = org.xrpl.sdk.crypto.internal.Rfc1751.mnemonicToKey(mnemonic)
            val encodedSeed =
                org.xrpl.sdk.crypto.codec.AddressCodec.encodeSeed(
                    entropy,
                    KeyAlgorithm.Ed25519,
                    provider,
                )
            encodedSeed shouldBe expectedSeed
        }
    }

    test("fromRfc1751Mnemonic is case-insensitive") {
        val upper = "CAB BETH HANK BIRD MEND SIGN GILD ANY KERN HYDE CHAT STUB"
        val lower = "cab beth hank bird mend sign gild any kern hyde chat stub"

        val w1 = Wallet.fromRfc1751Mnemonic(upper, KeyAlgorithm.Ed25519, provider)
        val w2 = Wallet.fromRfc1751Mnemonic(lower, KeyAlgorithm.Ed25519, provider)
        w1.use { a ->
            w2.use { b ->
                a.address shouldBe b.address
                a.publicKey shouldBe b.publicKey
            }
        }
    }

    test("fromRfc1751Mnemonic with invalid word throws") {
        val mnemonic = "CAB BETH HANK BIRD MEND SIGN GILD ANY KERN HYDE CHAT INVALID"
        val result =
            runCatching {
                Wallet.fromRfc1751Mnemonic(mnemonic, provider = provider)
            }
        result.isFailure shouldBe true
    }
})
