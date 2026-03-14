package org.xrpl.sdk.codec.definitions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Singleton that loads and provides access to XRPL field definitions.
 *
 * Parses the embedded `definitions.json` (derived from the canonical
 * ripple-binary-codec definitions) and exposes typed lookups for fields,
 * type codes, transaction types, and ledger entry types.
 */
internal object Definitions {
    private val json: Json = Json { ignoreUnknownKeys = true }

    private val root: JsonObject by lazy {
        json.parseToJsonElement(DEFINITIONS_JSON).jsonObject
    }

    // ---- Type codes ----

    /** Maps type name (e.g. "UInt32") to its integer code. */
    val typesByName: Map<String, Int> by lazy { parseTypes() }

    /** Reverse mapping: integer code to type name. */
    val typesByCode: Map<Int, String> by lazy {
        typesByName.entries.associate { (name, code) -> code to name }
    }

    // ---- Fields ----

    /** Maps field name to its [FieldDefinition]. */
    val fields: Map<String, FieldDefinition> by lazy { parseFields() }

    /** Maps [FieldId] to its [FieldDefinition]. */
    val fieldsByFieldId: Map<FieldId, FieldDefinition> by lazy {
        fields.values.associateBy { it.fieldId }
    }

    // ---- Transaction types ----

    /** Maps transaction type name (e.g. "Payment") to its integer code. */
    val transactionTypesByName: Map<String, Int> by lazy { parseNamedMap("TRANSACTION_TYPES") }

    /** Reverse mapping: integer code to transaction type name. */
    val transactionTypesByCode: Map<Int, String> by lazy {
        transactionTypesByName.entries.associate { (name, code) -> code to name }
    }

    // ---- Ledger entry types ----

    /** Maps ledger entry type name (e.g. "AccountRoot") to its integer code. */
    val ledgerEntryTypesByName: Map<String, Int> by lazy { parseNamedMap("LEDGER_ENTRY_TYPES") }

    /** Reverse mapping: integer code to ledger entry type name. */
    val ledgerEntryTypesByCode: Map<Int, String> by lazy {
        ledgerEntryTypesByName.entries.associate { (name, code) -> code to name }
    }

    // ---- Canonical ordering ----

    /**
     * Returns the given [fieldNames] sorted in canonical serialization order
     * (by type code ascending, then by field code ascending).
     *
     * Unknown field names are silently dropped.
     */
    fun getCanonicalFieldOrder(fieldNames: Collection<String>): List<FieldDefinition> =
        fieldNames
            .mapNotNull { fields[it] }
            .sortedWith(compareBy({ it.typeCode.value }, { it.fieldCode }))

    // ---- Parsing ----

    private fun parseTypes(): Map<String, Int> {
        val typesObj = root["TYPES"]!!.jsonObject
        return typesObj.entries.associate { (name, element) ->
            name to element.jsonPrimitive.int
        }
    }

    private fun parseFields(): Map<String, FieldDefinition> {
        val fieldsArray = root["FIELDS"]!!.jsonArray
        val types = typesByName

        val result = linkedMapOf<String, FieldDefinition>()
        for (entry in fieldsArray) {
            val pair = entry.jsonArray
            val name = pair[0].jsonPrimitive.content
            val info = pair[1].jsonObject

            val typeName = info["type"]!!.jsonPrimitive.content
            val typeCodeValue = types[typeName] ?: error("Unknown type '$typeName' for field '$name'")
            val nth = info["nth"]!!.jsonPrimitive.int

            result[name] =
                FieldDefinition(
                    name = name,
                    nth = nth,
                    isVlEncoded = info["isVLEncoded"]!!.jsonPrimitive.boolean,
                    isSerialized = info["isSerialized"]!!.jsonPrimitive.boolean,
                    isSigningField = info["isSigningField"]!!.jsonPrimitive.boolean,
                    typeCode = TypeCode(typeCodeValue),
                    fieldCode = nth,
                )
        }
        return result
    }

    private fun parseNamedMap(key: String): Map<String, Int> {
        val obj = root[key]!!.jsonObject
        return obj.entries.associate { (name, element) ->
            name to element.jsonPrimitive.int
        }
    }
}
