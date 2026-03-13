@file:Suppress("MagicNumber")

package org.xrpl.sdk.crypto.codec

import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.platformCryptoProvider

/**
 * Base58Check encoding/decoding using the XRP alphabet.
 *
 * Appends a 4-byte double-SHA-256 checksum to detect transcription errors.
 */
public object Base58Check {
    /**
     * Encodes payload with a version prefix and double-SHA-256 checksum.
     *
     * @param prefix Version byte(s) prepended before checksumming.
     * @param payload The raw data to encode.
     * @param provider CryptoProvider for SHA-256 (checksum computation).
     * @return Base58-encoded string.
     */
    public fun encode(
        prefix: ByteArray,
        payload: ByteArray,
        provider: CryptoProvider = platformCryptoProvider(),
    ): String {
        val data = prefix + payload
        val checksum = checksum(data, provider)
        return Base58.encode(data + checksum)
    }

    /**
     * Decodes a Base58Check string, verifying the checksum.
     * Returns the full decoded bytes (prefix + payload, checksum stripped).
     *
     * @param input Base58-encoded string.
     * @param provider CryptoProvider for SHA-256 (checksum verification).
     * @return The decoded bytes (prefix + payload) after stripping the 4-byte checksum.
     * @throws IllegalArgumentException if checksum is invalid or input is malformed.
     */
    public fun decodeRaw(
        input: String,
        provider: CryptoProvider = platformCryptoProvider(),
    ): ByteArray {
        val decoded = Base58.decode(input)
        require(decoded.size >= 5) {
            "Base58Check input too short. Decoded to ${decoded.size} bytes, need at least 5 " +
                "(1 prefix + 4 checksum). Check the input string."
        }
        val data = decoded.copyOfRange(0, decoded.size - 4)
        val actual = decoded.copyOfRange(decoded.size - 4, decoded.size)
        val expected = checksum(data, provider)
        require(actual.contentEquals(expected)) {
            "Base58Check checksum mismatch. The input may be corrupted or truncated."
        }
        return data
    }

    /**
     * Decodes and verifies the prefix matches one of the expected values.
     * Handles variable-length prefixes by trying each candidate.
     *
     * @param input Base58-encoded string.
     * @param expectedPrefixes List of candidate version prefixes to try.
     * @param expectedPayloadLength Expected payload length (used to determine prefix length).
     * @param provider CryptoProvider for SHA-256.
     * @return Pair of (matched prefix, payload bytes).
     * @throws IllegalArgumentException if no prefix matches or checksum fails.
     */
    public fun decodeChecked(
        input: String,
        expectedPrefixes: List<ByteArray>,
        expectedPayloadLength: Int,
        provider: CryptoProvider = platformCryptoProvider(),
    ): Pair<ByteArray, ByteArray> {
        val data = decodeRaw(input, provider)

        for (prefix in expectedPrefixes) {
            if (data.size == prefix.size + expectedPayloadLength &&
                data.copyOfRange(0, prefix.size).contentEquals(prefix)
            ) {
                val payload = data.copyOfRange(prefix.size, data.size)
                return prefix to payload
            }
        }

        throw IllegalArgumentException(
            "No matching prefix found. Decoded ${data.size} bytes, " +
                "expected payload length $expectedPayloadLength with one of " +
                "${expectedPrefixes.size} candidate prefix(es).",
        )
    }

    private fun checksum(
        data: ByteArray,
        provider: CryptoProvider,
    ): ByteArray {
        val hash = provider.sha256(provider.sha256(data))
        return hash.copyOfRange(0, 4)
    }
}
