package org.xrpl.sdk.codec.definitions

/**
 * Describes a single field in the XRPL binary serialization format.
 *
 * Each field has a name, a position index ([nth]), serialization flags,
 * and type/field codes that determine its binary header encoding.
 */
internal data class FieldDefinition(
    val name: String,
    val nth: Int,
    val isVlEncoded: Boolean,
    val isSerialized: Boolean,
    val isSigningField: Boolean,
    val typeCode: TypeCode,
    val fieldCode: Int,
) {
    /** The [FieldId] derived from this definition's type and field codes. */
    val fieldId: FieldId
        get() = FieldId(typeCode = typeCode.value, fieldCode = fieldCode)
}
