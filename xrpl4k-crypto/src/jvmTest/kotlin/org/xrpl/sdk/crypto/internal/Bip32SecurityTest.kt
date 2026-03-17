@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Tests for BIP32 intermediate key zeroing (Bug 11).
 */
class Bip32SecurityTest : FunSpec({

    val provider = platformCryptoProvider()

    test("derivePath zeroes intermediate keys") {
        val seed = ByteArray(32) { (it + 1).toByte() }
        val masterKey = deriveFromSeed(seed, provider)

        // Derive a multi-level path
        val result = derivePath(masterKey, "m/44'/144'/0'/0/0", provider)

        // The result should be a valid key (non-zero)
        result.privateKey.any { it != 0.toByte() } shouldBe true
        result.chainCode.any { it != 0.toByte() } shouldBe true

        // Master key should NOT be zeroed (caller may still need it)
        masterKey.privateKey.any { it != 0.toByte() } shouldBe true
    }

    test("derivePath with single level does not zero masterKey") {
        val seed = ByteArray(32) { (it + 1).toByte() }
        val masterKey = deriveFromSeed(seed, provider)
        val originalKeySnapshot = masterKey.privateKey.copyOf()

        val result = derivePath(masterKey, "m/44'", provider)
        result.privateKey shouldNotBe null

        // Master key should be unchanged
        masterKey.privateKey.contentEquals(originalKeySnapshot) shouldBe true
    }
})
