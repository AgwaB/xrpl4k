package org.xrpl.sdk.core.type

import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import kotlin.jvm.JvmInline

/**
 * A 20-byte XRPL account identifier represented as a 40-character lowercase hex string.
 *
 * Account IDs are derived from public keys via SHA-256 then RIPEMD-160 hashing.
 * They are used for address encoding, multi-sign sorting, and internal ledger lookups.
 *
 * The [hex] property is always normalized to lowercase to ensure consistent equality
 * comparisons. This is critical for correct multi-sign Signer sorting (by Account ID
 * hex ascending).
 *
 * @property hex The 40-character lowercase hex string (20 bytes).
 */
@JvmInline
public value class AccountId private constructor(public val hex: String) {
    /** Returns the raw 20-byte representation. */
    public fun toByteArray(): ByteArray = hex.hexToByteArray()

    public companion object {
        /**
         * Creates an [AccountId] from a hex string, normalizing to lowercase.
         *
         * @param hex A 40-character hexadecimal string (case-insensitive).
         * @throws IllegalArgumentException if the string is not exactly 40 valid hex characters.
         */
        public operator fun invoke(hex: String): AccountId {
            val normalized = hex.lowercase()
            require(normalized.length == 40 && normalized.all { it in '0'..'9' || it in 'a'..'f' }) {
                "AccountId must be exactly 40 hex characters (20 bytes). " +
                    "Got: '${hex.take(10)}...' (length=${hex.length})."
            }
            return AccountId(normalized)
        }

        /**
         * Creates an [AccountId] from raw 20-byte array.
         *
         * @param bytes A 20-byte array.
         * @throws IllegalArgumentException if [bytes] is not exactly 20 bytes.
         */
        public fun fromBytes(bytes: ByteArray): AccountId {
            require(bytes.size == 20) { "AccountId must be 20 bytes. Got ${bytes.size}." }
            return AccountId(bytes.toHexString()) // toHexString() already returns lowercase
        }
    }
}
