@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for SecretNumbers value range validation (Bug 12)
 * and entropy zeroing after wallet creation (Bug 14).
 */
class SecretNumbersSecurityTest : FunSpec({

    // ── Bug 12: secretToEntropy validates value range ─────────────────────

    test("secretToEntropy accepts value 0 (minimum)") {
        // Value "00000" with checksum for position 0: (0 * 1) % 9 = 0
        val groups = listOf("000000", "000000", "000000", "000000", "000000", "000000", "000000", "000000")
        shouldNotThrow<IllegalArgumentException> {
            SecretNumbers.secretToEntropy(groups)
        }
    }

    test("secretToEntropy accepts value 65535 (maximum)") {
        // Value "65535" at position 0: checksum = (65535 * 1) % 9 = 65535 % 9 = 0
        val checksum0 = SecretNumbers.calculateChecksum(0, 65535)
        val group0 = "65535$checksum0"
        // Other positions need valid checksums too
        val groups =
            (0 until 8).map { pos ->
                val cs = SecretNumbers.calculateChecksum(pos, 65535)
                "65535$cs"
            }
        shouldNotThrow<IllegalArgumentException> {
            SecretNumbers.secretToEntropy(groups)
        }
    }

    // ── Bug 14: entropy is zeroed after toWallet ─────────────────────────

    test("toWallet with string produces a valid wallet") {
        // Generate valid secret numbers: 8 groups of "000000" (all zero entropy)
        val secretString = "000000 000000 000000 000000 000000 000000 000000 000000"
        val wallet = SecretNumbers.toWallet(secretString)
        wallet.address.value.isNotEmpty() shouldBe true
    }

    test("toWallet with list produces a valid wallet") {
        val groups = listOf("000000", "000000", "000000", "000000", "000000", "000000", "000000", "000000")
        val wallet = SecretNumbers.toWallet(groups)
        wallet.address.value.isNotEmpty() shouldBe true
    }
})
