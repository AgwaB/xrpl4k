package org.xrpl.sdk.crypto.internal

import org.xrpl.sdk.crypto.CryptoProvider

/**
 * Converts a BIP39 mnemonic to a 64-byte seed.
 *
 * @param mnemonic space-separated mnemonic words.
 * @param passphrase optional passphrase (default empty).
 * @param provider crypto provider for HMAC-SHA512.
 * @return 64-byte seed.
 */
internal fun mnemonicToSeed(
    mnemonic: String,
    passphrase: String = "",
    provider: CryptoProvider,
): ByteArray {
    val normalizedMnemonic = mnemonic.trim().split("\\s+".toRegex()).joinToString(" ")
    val password = normalizedMnemonic.encodeToByteArray()
    val salt = "mnemonic$passphrase".encodeToByteArray()
    return pbkdf2HmacSha512(password, salt, iterations = 2048, keyLength = 64, provider)
}

/**
 * Validates that a mnemonic string contains valid BIP39 words.
 * Only validates word presence and count, NOT checksum.
 */
internal fun validateMnemonic(mnemonic: String): Boolean {
    val words = mnemonic.trim().split("\\s+".toRegex())
    if (words.size !in listOf(12, 15, 18, 21, 24)) return false
    return words.all { it in BIP39_ENGLISH_WORDLIST }
}
