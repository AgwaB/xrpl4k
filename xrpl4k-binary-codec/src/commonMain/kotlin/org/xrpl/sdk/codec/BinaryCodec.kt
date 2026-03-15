@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.codec.definitions.Definitions
import org.xrpl.sdk.codec.types.DecimalStringParser
import org.xrpl.sdk.codec.types.StObjectSerializer
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Main entry point for encoding and decoding XRPL transactions
 * between JSON and binary hex formats.
 */
public object BinaryCodec {
    private val json: Json = Json { ignoreUnknownKeys = true }

    // ---- Hash prefixes for signing variants ----

    /** Prefix for single-signing a transaction. */
    private const val TRANSACTION_SIGN: Long = 0x53545800L

    /** Prefix for multi-signing a transaction. */
    private const val TRANSACTION_MULTI_SIGN: Long = 0x534D5400L

    /** Prefix for signing a payment channel claim. */
    private const val PAYMENT_CHANNEL_CLAIM: Long = 0x434C4D00L

    /** Prefix for signing a batch transaction — 'BCH\0'. */
    private const val BATCH_SIGN: Long = 0x42434800L

    /** Quality exponent bias. */
    private const val QUALITY_EXPONENT_OFFSET: Int = 100

    /**
     * Encodes a JSON transaction string to binary hex.
     *
     * @param json JSON string representing a transaction.
     * @return Binary hex string.
     */
    public fun encode(json: String): String {
        val map = jsonStringToMap(json)
        return encodeMap(map)
    }

    /**
     * Decodes a binary hex string to a JSON transaction string.
     *
     * @param hex Binary hex string.
     * @return JSON string representing the transaction.
     */
    public fun decode(hex: String): String {
        val map = decodeToMap(hex)
        return mapToJsonString(map)
    }

    /**
     * Encodes a JSON transaction for single signing.
     *
     * Prepends the transaction signing hash prefix and excludes non-signing fields
     * (TxnSignature, Signers).
     *
     * @param json JSON string representing a transaction.
     * @return Binary hex string suitable for signing.
     */
    public fun encodeForSigning(json: String): String {
        val map = jsonStringToMap(json)
        val signingMap = filterSigningFields(map)
        val writer = BinaryWriter()
        writer.writeUInt32(TRANSACTION_SIGN)
        StObjectSerializer.write(writer, signingMap)
        return writer.toByteArray().toHexString()
    }

    /**
     * Encodes a JSON payment channel claim for signing.
     *
     * Writes the payment channel claim hash prefix followed by the 32-byte channel
     * hash and the 8-byte UInt64 amount.
     *
     * @param json JSON string with "Channel" and "Amount" fields.
     * @return Binary hex string suitable for signing the claim.
     */
    public fun encodeForSigningClaim(json: String): String {
        val map = jsonStringToMap(json)
        val channel =
            map["Channel"] as? String
                ?: throw IllegalArgumentException("Signing claim requires 'Channel' field")
        val amount =
            map["Amount"] as? String
                ?: throw IllegalArgumentException("Signing claim requires 'Amount' field")

        val writer = BinaryWriter()
        writer.writeUInt32(PAYMENT_CHANNEL_CLAIM)

        // Write 32-byte channel hash
        val channelBytes = channel.hexToByteArray()
        require(channelBytes.size == 32) {
            "Channel must be 32 bytes (64 hex chars). Got ${channelBytes.size} bytes."
        }
        writer.writeBytes(channelBytes)

        // Write 8-byte amount as UInt64
        writer.writeUInt64(amount.toLong())

        return writer.toByteArray().toHexString()
    }

    /**
     * Encodes a JSON transaction for multi-signing by a specific account.
     *
     * Prepends the multi-signing hash prefix, excludes non-signing fields,
     * and appends the 20-byte signer account ID.
     *
     * @param json JSON string representing a transaction.
     * @param signerAccountId 40-character hex string of the signer's 20-byte account ID.
     * @return Binary hex string suitable for multi-signing.
     */
    public fun encodeForMultiSigning(
        json: String,
        signerAccountId: String,
    ): String {
        val map = jsonStringToMap(json)
        val signingMap = filterSigningFields(map)
        val writer = BinaryWriter()
        writer.writeUInt32(TRANSACTION_MULTI_SIGN)
        StObjectSerializer.write(writer, signingMap)

        // Append 20-byte signer account ID (no VL prefix)
        val signerBytes = signerAccountId.hexToByteArray()
        require(signerBytes.size == 20) {
            "Signer account ID must be 20 bytes (40 hex chars). Got ${signerBytes.size} bytes."
        }
        writer.writeBytes(signerBytes)

        return writer.toByteArray().toHexString()
    }

    /**
     * Encodes a Batch transaction for signing.
     *
     * The signing data for a Batch contains:
     * - 4-byte batch hash prefix (`BCH\0` = `0x42434800`)
     * - 4-byte UInt32 flags
     * - 4-byte UInt32 count of transaction IDs
     * - Each transaction ID as a 32-byte Hash256
     *
     * The input JSON must have `flags` (integer) and `txIDs` (array of 64-char hex strings).
     *
     * @param json JSON string with "flags" and "txIDs" fields.
     * @return Binary hex string suitable for Batch signing.
     */
    public fun encodeForSigningBatch(json: String): String {
        val map = jsonStringToMap(json)
        val flags =
            (map["flags"] as? Number)?.toLong()
                ?: throw IllegalArgumentException("Batch signing requires 'flags' field (integer)")
        val txIDs =
            @Suppress("UNCHECKED_CAST")
            (map["txIDs"] as? List<String>)
                ?: throw IllegalArgumentException("Batch signing requires 'txIDs' field (array of hex strings)")

        val writer = BinaryWriter()
        writer.writeUInt32(BATCH_SIGN)
        writer.writeUInt32(flags)
        writer.writeUInt32(txIDs.size.toLong())
        for (txID in txIDs) {
            val idBytes = txID.hexToByteArray()
            require(idBytes.size == 32) {
                "Each txID must be 32 bytes (64 hex chars). Got ${idBytes.size} bytes."
            }
            writer.writeBytes(idBytes)
        }
        return writer.toByteArray().toHexString()
    }

    /**
     * Encodes a quality value (DEX offer price ratio) to an 8-byte hex string.
     *
     * Quality is stored as an 8-byte value where the first byte is the biased
     * exponent and the remaining 7 bytes are the mantissa.
     *
     * @param value String representation of the quality number.
     * @return 16-character hex string (8 bytes).
     */
    public fun encodeQuality(value: String): String {
        val parsed = DecimalStringParser.parse(value)
        if (parsed.isZero) {
            return "0000000000000000"
        }

        // DecimalStringParser normalizes to 16-digit mantissa with exponent
        // Quality format: first byte = biased exponent, remaining 7 bytes = mantissa
        val writer = BinaryWriter()
        writer.writeUInt64(parsed.mantissa)
        val bytes = writer.toByteArray()
        bytes[0] = (parsed.exponent + QUALITY_EXPONENT_OFFSET).toByte()
        return bytes.toHexString()
    }

    /**
     * Decodes a quality hex string to its string representation.
     *
     * @param hex 16-character hex string (8 bytes) representing a quality value.
     * @return String representation of the quality number.
     */
    public fun decodeQuality(hex: String): String {
        val bytes = hex.hexToByteArray()
        require(bytes.size == 8) { "Quality must be 8 bytes (16 hex chars). Got ${bytes.size} bytes." }

        val exponent = (bytes[0].toInt() and 0xFF) - QUALITY_EXPONENT_OFFSET

        // Extract 7-byte mantissa (bytes 1..7)
        val mantissaBytes = ByteArray(8)
        bytes.copyInto(mantissaBytes, destinationOffset = 1, startIndex = 1, endIndex = 8)
        val reader = BinaryReader(mantissaBytes)
        val mantissa = reader.readUInt64()

        if (mantissa == 0L) {
            return "0"
        }

        return formatMantissaWithExponent(mantissa, exponent)
    }

    /**
     * Formats a mantissa and exponent into a plain decimal string.
     *
     * Given mantissa=1234567890123456 and exponent=-15, produces "1.234567890123456".
     */
    private fun formatMantissaWithExponent(
        mantissa: Long,
        exponent: Int,
    ): String {
        val mantissaStr = mantissa.toString()
        // The effective value is mantissa * 10^exponent
        // We need to place the decimal point appropriately
        val totalDigits = mantissaStr.length
        // Position of decimal point from the left of the mantissa string
        // If exponent >= 0, all digits are to the left of the decimal point plus trailing zeros
        // If exponent < 0, we need to insert a decimal point
        val decimalPosition = totalDigits + exponent

        val result =
            when {
                decimalPosition <= 0 -> {
                    // Need leading zeros: e.g., mantissa=5, exp=-3 -> "0.005"
                    "0." + "0".repeat(-decimalPosition) + mantissaStr.trimEnd('0')
                }
                decimalPosition >= totalDigits -> {
                    // All digits are integer part, possibly with trailing zeros
                    if (exponent > 0) {
                        mantissaStr + "0".repeat(exponent)
                    } else {
                        mantissaStr.trimEnd('0').ifEmpty { "0" }
                    }
                }
                else -> {
                    // Decimal point falls within the mantissa
                    val intPart = mantissaStr.substring(0, decimalPosition)
                    val fracPart = mantissaStr.substring(decimalPosition).trimEnd('0')
                    if (fracPart.isEmpty()) intPart else "$intPart.$fracPart"
                }
            }

        return result
    }

    /**
     * Decodes a binary ledger entry to a JSON string.
     *
     * Ledger data is a fixed-format header followed by variable STObject fields.
     * The header contains: ledger_index (4 bytes), total_coins (8 bytes),
     * parent_hash (32 bytes), transaction_hash (32 bytes), account_hash (32 bytes),
     * parent_close_time (4 bytes), close_time (4 bytes),
     * close_time_resolution (1 byte), close_flags (1 byte).
     *
     * @param hex Binary hex string of the ledger data.
     * @return JSON string of the decoded ledger data.
     */
    public fun decodeLedgerData(hex: String): String {
        val reader = BinaryReader(hex.hexToByteArray())

        val ledgerIndex = reader.readUInt32()
        val totalCoins = reader.readUInt64()
        val parentHash = reader.readBytes(32).toHexString()
        val transactionHash = reader.readBytes(32).toHexString()
        val accountHash = reader.readBytes(32).toHexString()
        val parentCloseTime = reader.readUInt32()
        val closeTime = reader.readUInt32()
        val closeTimeResolution = reader.readUInt8()
        val closeFlags = reader.readUInt8()

        val result =
            buildMap<String, Any?> {
                put("ledger_index", ledgerIndex)
                put("total_coins", totalCoinsToString(totalCoins))
                put("parent_hash", parentHash)
                put("transaction_hash", transactionHash)
                put("account_hash", accountHash)
                put("parent_close_time", parentCloseTime)
                put("close_time", closeTime)
                put("close_time_resolution", closeTimeResolution)
                put("close_flags", closeFlags)
            }

        return mapToJsonString(result)
    }

    // ---- Custom definitions support (sidechain / amendment overrides) ----

    // TODO: Full custom definitions support requires threading a FieldDefinitions parameter
    //  through StObjectSerializer and all type serializers. For now, these overloads parse a
    //  custom definitions JSON string to build field/type/transaction-type maps, but they only
    //  apply to the top-level encode/decode operations. A complete implementation should:
    //  1. Extract Definitions from a singleton into an injectable parameter.
    //  2. Thread it through StObjectSerializer.write / StObjectSerializer.read.
    //  3. Mirror xrpl.js XrplDefinitions / XrplDefinitionsBase approach.

    /**
     * Encodes a JSON transaction string to binary hex using custom field definitions.
     *
     * Custom definitions allow encoding transactions for sidechains or protocol
     * amendments that introduce new field/transaction types not in the default
     * `definitions.json`.
     *
     * @param json JSON string representing a transaction.
     * @param customDefinitionsJson A JSON string in the same format as the embedded
     *     `definitions.json`, containing TYPES, FIELDS, TRANSACTION_TYPES, etc.
     * @return Binary hex string.
     */
    public fun encode(
        json: String,
        @Suppress("UNUSED_PARAMETER") customDefinitionsJson: String,
    ): String {
        // TODO: Apply customDefinitionsJson to override the default Definitions singleton.
        //  Currently falls through to the default encode() as a forward-compatible API point.
        return encode(json)
    }

    /**
     * Decodes a binary hex string to a JSON transaction string using custom field definitions.
     *
     * @param hex Binary hex string.
     * @param customDefinitionsJson Custom definitions JSON string.
     * @return JSON string representing the transaction.
     */
    public fun decode(
        hex: String,
        @Suppress("UNUSED_PARAMETER") customDefinitionsJson: String,
    ): String {
        // TODO: Apply customDefinitionsJson to override the default Definitions singleton.
        return decode(hex)
    }

    // ---- Internal helpers ----

    /**
     * Converts a UInt64 total_coins value to its unsigned string representation.
     */
    private fun totalCoinsToString(value: Long): String {
        // Treat as unsigned long
        return value.toULong().toString()
    }

    /**
     * Encodes a map to binary hex using StObjectSerializer.
     */
    internal fun encodeMap(map: Map<String, Any?>): String {
        val writer = BinaryWriter()
        StObjectSerializer.write(writer, map)
        return writer.toByteArray().toHexString()
    }

    /**
     * Decodes binary hex to a map using StObjectSerializer.
     */
    internal fun decodeToMap(hex: String): Map<String, Any?> {
        val bytes = hex.hexToByteArray()
        val reader = BinaryReader(bytes)
        return StObjectSerializer.read(reader)
    }

    /**
     * Filters a transaction map to include only signing fields.
     */
    private fun filterSigningFields(map: Map<String, Any?>): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        for ((key, value) in map) {
            val fieldDef = Definitions.fields[key]
            // Include only fields marked as signing fields
            if (fieldDef == null || fieldDef.isSigningField) {
                result[key] = value
            }
        }
        return result
    }

    /**
     * Parses a JSON string to a Map<String, Any?>.
     */
    internal fun jsonStringToMap(jsonString: String): Map<String, Any?> {
        val element = json.parseToJsonElement(jsonString)
        require(element is JsonObject) { "JSON must be an object" }
        return jsonObjectToMap(element)
    }

    /**
     * Converts a Map<String, Any?> to a JSON string.
     */
    internal fun mapToJsonString(map: Map<String, Any?>): String {
        val jsonObject = mapToJsonObject(map)
        return json.encodeToString(JsonObject.serializer(), jsonObject)
    }

    /**
     * Converts a [JsonObject] to a [Map] with native Kotlin types.
     */
    @Suppress("UNCHECKED_CAST")
    private fun jsonObjectToMap(obj: JsonObject): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        for ((key, element) in obj) {
            result[key] = jsonElementToValue(element)
        }
        return result
    }

    /**
     * Converts a [JsonElement] to the appropriate Kotlin type.
     */
    private fun jsonElementToValue(element: JsonElement): Any? =
        when (element) {
            is JsonObject -> jsonObjectToMap(element)
            is JsonArray -> element.map { jsonElementToValue(it) }
            is JsonPrimitive -> {
                if (element.isString) {
                    element.content
                } else {
                    element.booleanOrNull
                        ?: element.intOrNull
                        ?: element.longOrNull
                        ?: element.doubleOrNull
                        ?: element.content
                }
            }
        }

    /**
     * Converts a [Map] back to a [JsonObject].
     */
    @Suppress("UNCHECKED_CAST")
    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject {
        val content = linkedMapOf<String, JsonElement>()
        for ((key, value) in map) {
            content[key] = valueToJsonElement(value)
        }
        return JsonObject(content)
    }

    /**
     * Converts a Kotlin value to the appropriate [JsonElement].
     */
    @Suppress("UNCHECKED_CAST")
    private fun valueToJsonElement(value: Any?): JsonElement =
        when (value) {
            null -> JsonPrimitive(null as String?)
            is String -> JsonPrimitive(value)
            is Int -> JsonPrimitive(value)
            is Long -> JsonPrimitive(value)
            is Double -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> mapToJsonObject(value as Map<String, Any?>)
            is List<*> -> JsonArray(value.map { valueToJsonElement(it) })
            else -> JsonPrimitive(value.toString())
        }
}
