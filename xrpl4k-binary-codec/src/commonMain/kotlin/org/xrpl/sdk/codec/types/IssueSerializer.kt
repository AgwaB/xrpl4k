package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL Issue type.
 *
 * An Issue is a currency + optional issuer (required for non-XRP currencies).
 * Represented as a [Map] with `"currency"` and optionally `"issuer"` keys.
 */
internal object IssueSerializer : TypeSerializer<Map<String, String>> {
    /** AccountID byte size. */
    private const val ACCOUNT_ID_LENGTH: Int = 20

    @Suppress("UNCHECKED_CAST")
    override fun write(
        writer: BinaryWriter,
        value: Map<String, String>,
    ) {
        val currency =
            value["currency"]
                ?: throw IllegalArgumentException("Issue requires 'currency' field")

        CurrencySerializer.write(writer, currency)

        // Non-XRP currencies require an issuer
        if (currency != "XRP") {
            val issuer =
                value["issuer"]
                    ?: throw IllegalArgumentException("Non-XRP Issue requires 'issuer' field")
            val issuerBytes = issuer.hexToByteArray()
            require(issuerBytes.size == ACCOUNT_ID_LENGTH) {
                "Issuer must be $ACCOUNT_ID_LENGTH bytes. Got ${issuerBytes.size}"
            }
            writer.writeBytes(issuerBytes)
        }
    }

    override fun read(reader: BinaryReader): Map<String, String> {
        val currency = CurrencySerializer.read(reader)

        return if (currency == "XRP") {
            mapOf("currency" to currency)
        } else {
            val issuer = reader.readBytes(ACCOUNT_ID_LENGTH).toHexString()
            mapOf(
                "currency" to currency,
                "issuer" to issuer,
            )
        }
    }
}
