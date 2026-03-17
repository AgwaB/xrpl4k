@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto

import org.xrpl.sdk.core.type.KeyAlgorithm

/**
 * Xaman (formerly Xumm) secret numbers format.
 *
 * 8 groups of 6-digit numbers that encode 16 bytes of entropy.
 * Each group encodes 2 bytes (5 value digits + 1 checksum digit).
 *
 * The checksum digit for group at position `i` with 5-digit value `v` is:
 *   `(v * (i * 2 + 1)) % 9`
 *
 * Reference: `@xrplf/secret-numbers` package in xrpl.js
 */
public object SecretNumbers {
    private const val GROUP_COUNT = 8
    private const val DIGITS_PER_GROUP = 6
    private const val TOTAL_DIGITS = GROUP_COUNT * DIGITS_PER_GROUP // 48

    /**
     * Parse a secret numbers string into 8 groups of 6-digit strings.
     *
     * Accepts whitespace-delimited or any non-digit-delimited format.
     * Also accepts a raw 48-digit string with no separators.
     */
    internal fun parseSecretString(secret: String): List<String> {
        val digitsOnly = secret.replace(Regex("[^0-9]"), "")
        require(digitsOnly.length == TOTAL_DIGITS) {
            "Invalid secret string: expected $TOTAL_DIGITS digits, got ${digitsOnly.length}."
        }
        return (0 until GROUP_COUNT).map { i ->
            digitsOnly.substring(i * DIGITS_PER_GROUP, (i + 1) * DIGITS_PER_GROUP)
        }
    }

    /**
     * Compute the checksum digit for a group value at the given position.
     */
    internal fun calculateChecksum(
        position: Int,
        value: Int,
    ): Int {
        return (value * (position * 2 + 1)) % 9
    }

    /**
     * Validate the checksum digit for a 6-digit group at the given position.
     */
    internal fun checkChecksum(
        position: Int,
        group: String,
    ): Boolean {
        require(group.length == DIGITS_PER_GROUP) {
            "Each secret number group must be $DIGITS_PER_GROUP digits."
        }
        val value = group.substring(0, 5).toInt()
        val checksum = group.substring(5).toInt()
        return calculateChecksum(position, value) == checksum
    }

    /**
     * Convert 8 groups of 6-digit secret numbers to 16 bytes of entropy.
     *
     * Each group encodes 2 bytes: the first 5 digits are the value (0..65535),
     * and the 6th digit is a checksum.
     */
    internal fun secretToEntropy(groups: List<String>): ByteArray {
        require(groups.size == GROUP_COUNT) {
            "Secret must have $GROUP_COUNT groups."
        }
        val entropy = ByteArray(16)
        for (i in groups.indices) {
            val group = groups[i]
            require(group.length == DIGITS_PER_GROUP) {
                "Each secret number must be $DIGITS_PER_GROUP digits."
            }
            require(checkChecksum(i, group)) {
                "Invalid checksum for group $i: '$group'."
            }
            val value = group.substring(0, 5).toInt()
            require(value in 0..65535) {
                "Secret number value $value for group $i is out of range (0..65535)."
            }
            entropy[i * 2] = (value shr 8).toByte()
            entropy[i * 2 + 1] = (value and 0xFF).toByte()
        }
        return entropy
    }

    /**
     * Derive a [Wallet] from Xaman secret numbers.
     *
     * The secret numbers are parsed into entropy, which is then used as a secp256k1 seed
     * (matching Xaman's default behavior). Use [algorithm] to override.
     *
     * @param secretNumbers A string of 8 groups of 6-digit numbers (whitespace or other delimited).
     * @param algorithm Key algorithm (default: secp256k1 to match Xaman).
     * @param provider The crypto provider to use.
     * @return The derived [Wallet].
     */
    public fun toWallet(
        secretNumbers: String,
        algorithm: KeyAlgorithm = KeyAlgorithm.Secp256k1,
        provider: CryptoProvider = platformCryptoProvider(),
    ): Wallet {
        val groups = parseSecretString(secretNumbers)
        val entropy = secretToEntropy(groups)
        return try {
            Wallet.fromEntropy(entropy, algorithm, provider)
        } finally {
            entropy.fill(0)
        }
    }

    /**
     * Derive a [Wallet] from Xaman secret numbers provided as a list of 8 groups.
     *
     * @param secretNumbers List of 8 six-digit number strings.
     * @param algorithm Key algorithm (default: secp256k1 to match Xaman).
     * @param provider The crypto provider to use.
     * @return The derived [Wallet].
     */
    public fun toWallet(
        secretNumbers: List<String>,
        algorithm: KeyAlgorithm = KeyAlgorithm.Secp256k1,
        provider: CryptoProvider = platformCryptoProvider(),
    ): Wallet {
        val entropy = secretToEntropy(secretNumbers)
        return try {
            Wallet.fromEntropy(entropy, algorithm, provider)
        } finally {
            entropy.fill(0)
        }
    }
}

/**
 * Derive a wallet from Xaman (Xumm) secret numbers.
 *
 * NOTE: Uses secp256k1 by default to match Xaman's behavior.
 *
 * @param secretNumbers A string of 8 groups of 6-digit numbers.
 * @param algorithm Key algorithm (default: secp256k1).
 * @param provider The crypto provider to use.
 * @return The derived [Wallet].
 */
public fun Wallet.Companion.fromSecretNumbers(
    secretNumbers: String,
    algorithm: KeyAlgorithm = KeyAlgorithm.Secp256k1,
    provider: CryptoProvider = platformCryptoProvider(),
): Wallet = SecretNumbers.toWallet(secretNumbers, algorithm, provider)
