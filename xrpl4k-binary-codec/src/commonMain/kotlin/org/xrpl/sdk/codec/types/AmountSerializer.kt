@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL Amount type.
 *
 * Handles three distinct formats:
 * - **XRP amounts**: 8-byte encoded drops value.
 * - **IOU amounts**: 48-byte encoding (8-byte amount + 20-byte currency + 20-byte issuer).
 * - **MPT amounts**: 33-byte encoding (1-byte flags + 8-byte amount + 24-byte MPT issuance ID).
 *
 * Input values are represented as [Any] -- either a [String] for XRP drops
 * or a [Map] with `"value"`, `"currency"`, and `"issuer"` keys for IOU,
 * or a [Map] with `"value"` and `"mpt_issuance_id"` keys for MPT.
 */
internal object AmountSerializer : TypeSerializer<Any> {
    // ---- XRP constants ----

    /** Bit 63 clear = XRP (not IOU). Bit 62 set = positive. */
    private const val XRP_POSITIVE_BIT: Long = 0x4000_0000_0000_0000L

    /** Maximum XRP drops value (10^17). */
    private const val MAX_XRP_DROPS: Long = 100_000_000_000_000_000L

    // ---- IOU constants ----

    /** Bit 63 set = not XRP (IOU). */
    private const val NOT_XRP_BIT: Long = 1L shl 63 // 0x8000000000000000

    /** Bit 62 = sign bit for IOU (1 = positive). */
    private const val IOU_SIGN_BIT: Long = 1L shl 62

    /** IOU zero amount: only the notXRP bit is set. */
    private const val IOU_ZERO: Long = NOT_XRP_BIT

    /** Exponent bias for IOU encoding. */
    private const val EXPONENT_BIAS: Int = 97

    /** Minimum mantissa for normalized IOU (10^15). */
    private const val MIN_MANTISSA: Long = 1_000_000_000_000_000L

    /** Maximum mantissa for normalized IOU (10^16 - 1). */
    private const val MAX_MANTISSA: Long = 9_999_999_999_999_999L

    // ---- MPT constants ----

    /** MPT amount type indicator in the flags byte. */
    private const val MPT_TYPE_INDICATOR: Int = 0x60

    /** MPT positive flag. */
    private const val MPT_POSITIVE_FLAG: Int = 0x40

    // ---- Serialization ----

    @Suppress("UNCHECKED_CAST")
    override fun write(
        writer: BinaryWriter,
        value: Any,
    ) {
        when (value) {
            is String -> writeXrpAmount(writer, value)
            is Map<*, *> -> {
                val map = value as Map<String, Any?>
                if (map.containsKey("mpt_issuance_id")) {
                    writeMptAmount(writer, map)
                } else {
                    writeIouAmount(writer, map)
                }
            }
            else -> throw IllegalArgumentException(
                "Amount value must be a String (XRP drops) or Map (IOU/MPT). Got: ${value::class}",
            )
        }
    }

    override fun read(reader: BinaryReader): Any {
        // Peek at the first byte to determine the format
        val firstByte = reader.peek()

        // MPT: flags byte starts with 0x60 pattern (bits 6-5 set, bit 7 clear)
        if (firstByte and 0xE0 == 0x60) {
            return readMptAmount(reader)
        }

        // Read the 8-byte amount
        val amount = reader.readUInt64()

        // Bit 63: 0 = XRP, 1 = IOU
        return if (amount and NOT_XRP_BIT == 0L) {
            readXrpAmount(amount)
        } else {
            readIouAmount(amount, reader)
        }
    }

    // ---- XRP ----

    private fun writeXrpAmount(
        writer: BinaryWriter,
        drops: String,
    ) {
        val value = drops.toLong()
        require(value >= 0L) { "XRP drops must be non-negative: $value" }
        require(value <= MAX_XRP_DROPS) { "XRP drops exceeds maximum: $value" }

        // Bit 63 = 0 (XRP), Bit 62 = 1 (positive)
        val encoded = value or XRP_POSITIVE_BIT
        writer.writeUInt64(encoded)
    }

    private fun readXrpAmount(encoded: Long): String {
        val drops = encoded and XRP_POSITIVE_BIT.inv()
        return drops.toString()
    }

    // ---- IOU ----

    private fun writeIouAmount(
        writer: BinaryWriter,
        map: Map<String, Any?>,
    ) {
        val valueStr =
            map["value"] as? String
                ?: throw IllegalArgumentException("IOU amount requires 'value' field")
        val currency =
            map["currency"] as? String
                ?: throw IllegalArgumentException("IOU amount requires 'currency' field")
        val issuer =
            map["issuer"] as? String
                ?: throw IllegalArgumentException("IOU amount requires 'issuer' field")

        val parsed = DecimalStringParser.parse(valueStr)

        val amountBits: Long =
            if (parsed.isZero) {
                IOU_ZERO
            } else {
                val signBit = if (!parsed.isNegative) IOU_SIGN_BIT else 0L
                val biasedExponent = (parsed.exponent + EXPONENT_BIAS).toLong()
                NOT_XRP_BIT or signBit or (biasedExponent shl 54) or parsed.mantissa
            }

        writer.writeUInt64(amountBits)

        // Write 20-byte currency code
        CurrencySerializer.write(writer, currency)

        // Write 20-byte issuer account ID
        val issuerBytes = issuer.hexToByteArray()
        require(issuerBytes.size == 20) {
            "Issuer account ID must be 20 bytes (40 hex chars). Got ${issuerBytes.size} bytes."
        }
        writer.writeBytes(issuerBytes)
    }

    private fun readIouAmount(
        amountBits: Long,
        reader: BinaryReader,
    ): Map<String, String> {
        // Read 20-byte currency
        val currency = CurrencySerializer.read(reader)

        // Read 20-byte issuer
        val issuer = reader.readBytes(20).toHexString()

        // Decode amount bits
        if (amountBits == IOU_ZERO) {
            return mapOf(
                "value" to "0",
                "currency" to currency,
                "issuer" to issuer,
            )
        }

        val isPositive = (amountBits and IOU_SIGN_BIT) != 0L
        val biasedExponent = ((amountBits ushr 54) and 0xFFL).toInt()
        val mantissa = amountBits and 0x003F_FFFF_FFFF_FFFFL
        val exponent = biasedExponent - EXPONENT_BIAS

        val valueStr = formatIouValue(mantissa, exponent, isPositive)
        return mapOf(
            "value" to valueStr,
            "currency" to currency,
            "issuer" to issuer,
        )
    }

    /**
     * Formats an IOU mantissa and exponent back to a decimal string.
     */
    private fun formatIouValue(
        mantissa: Long,
        exponent: Int,
        isPositive: Boolean,
    ): String {
        val sign = if (isPositive) "" else "-"
        // Mantissa has 16 significant digits (normalized form)
        val mantissaStr = mantissa.toString()
        // The effective decimal is: mantissa * 10^exponent
        // We output in scientific notation-like form or plain decimal
        return if (exponent == 0) {
            "$sign$mantissaStr"
        } else {
            "${sign}${mantissaStr}e$exponent"
        }
    }

    // ---- MPT ----

    private fun writeMptAmount(
        writer: BinaryWriter,
        map: Map<String, Any?>,
    ) {
        val valueStr =
            map["value"] as? String
                ?: throw IllegalArgumentException("MPT amount requires 'value' field")
        val mptIssuanceId =
            map["mpt_issuance_id"] as? String
                ?: throw IllegalArgumentException("MPT amount requires 'mpt_issuance_id' field")

        val amount = valueStr.toLong()
        require(amount >= 0) { "Negative MPT amounts not allowed: $amount" }
        val absAmount = amount

        // Flags byte: type indicator (0x60) with positive flag set when amount >= 0
        val flagsByte = MPT_TYPE_INDICATOR or MPT_POSITIVE_FLAG
        writer.writeUInt8(flagsByte)

        // 8-byte absolute amount
        writer.writeUInt64(absAmount)

        // 24-byte MPT issuance ID
        val idBytes = mptIssuanceId.hexToByteArray()
        require(idBytes.size == 24) {
            "MPT issuance ID must be 24 bytes (48 hex chars). Got ${idBytes.size} bytes."
        }
        writer.writeBytes(idBytes)
    }

    private fun readMptAmount(reader: BinaryReader): Map<String, String> {
        val flagsByte = reader.readUInt8()
        val isPositive = (flagsByte and MPT_POSITIVE_FLAG) != 0

        val absAmount = reader.readUInt64()
        val mptIssuanceId = reader.readBytes(24).toHexString()

        val value = if (isPositive) absAmount.toString() else "-$absAmount"
        return mapOf(
            "value" to value,
            "mpt_issuance_id" to mptIssuanceId,
        )
    }
}
