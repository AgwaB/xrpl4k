package org.xrpl.sdk.core.util

/**
 * Converts a UTF-8 string to its uppercase hexadecimal representation.
 *
 * Useful for encoding Memo fields on the XRPL ledger.
 *
 * @param str the string to convert.
 * @return the hex-encoded string (uppercase).
 */
public fun convertStringToHex(str: String): String = str.encodeToByteArray().toHexString().uppercase()

/**
 * Converts a hexadecimal string back to a UTF-8 string.
 *
 * Useful for decoding Memo fields and the Domain field from the XRPL ledger.
 *
 * @param hex the hex string to decode.
 * @return the decoded UTF-8 string.
 * @throws IllegalArgumentException if the hex string has odd length or contains non-hex characters.
 */
public fun convertHexToString(hex: String): String = hex.hexToByteArray().decodeToString()
