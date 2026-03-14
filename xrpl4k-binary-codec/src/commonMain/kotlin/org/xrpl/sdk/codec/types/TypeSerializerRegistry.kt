@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.definitions.TypeCode

/**
 * Registry that maps XRPL type codes to their corresponding [TypeSerializer] instances.
 *
 * Includes both standard types and meta-types (Transaction, LedgerEntry, etc.)
 * which all delegate to [StObjectSerializer].
 */
internal object TypeSerializerRegistry {
    @Suppress("UNCHECKED_CAST")
    private val serializers: Map<Int, TypeSerializer<Any?>> =
        buildMap {
            // Standard integer types
            put(TypeCode.UInt8.value, UInt8Serializer as TypeSerializer<Any?>)
            put(TypeCode.UInt16.value, UInt16Serializer as TypeSerializer<Any?>)
            put(TypeCode.UInt32.value, UInt32Serializer as TypeSerializer<Any?>)
            put(TypeCode.UInt64.value, UInt64Serializer as TypeSerializer<Any?>)

            // Signed integer
            put(TypeCode.Int32.value, Int32Serializer as TypeSerializer<Any?>)

            // Hash types
            put(TypeCode.Hash128.value, Hash128Serializer as TypeSerializer<Any?>)
            put(TypeCode.Hash160.value, UInt160Serializer as TypeSerializer<Any?>)
            put(TypeCode.Hash256.value, Hash256Serializer as TypeSerializer<Any?>)
            put(TypeCode.Hash192.value, Hash192Serializer as TypeSerializer<Any?>)

            // Large unsigned integer types
            put(TypeCode.UInt96.value, UInt96Serializer as TypeSerializer<Any?>)
            put(TypeCode.UInt384.value, UInt384Serializer as TypeSerializer<Any?>)
            put(TypeCode.UInt512.value, UInt512Serializer as TypeSerializer<Any?>)

            // Variable-length types
            put(TypeCode.Blob.value, BlobSerializer as TypeSerializer<Any?>)
            put(TypeCode.AccountID.value, AccountIdSerializer as TypeSerializer<Any?>)

            // Amount
            put(TypeCode.Amount.value, AmountSerializer as TypeSerializer<Any?>)

            // Number (AMM fields)
            put(TypeCode.Number.value, StNumberSerializer as TypeSerializer<Any?>)

            // Structured types
            put(TypeCode.STObject.value, StObjectSerializer as TypeSerializer<Any?>)
            put(TypeCode.STArray.value, StArraySerializer as TypeSerializer<Any?>)
            put(TypeCode.PathSet.value, PathSetSerializer as TypeSerializer<Any?>)
            put(TypeCode.Vector256.value, Vector256Serializer as TypeSerializer<Any?>)

            // Composite types
            put(TypeCode.Currency.value, CurrencySerializer as TypeSerializer<Any?>)
            put(TypeCode.Issue.value, IssueSerializer as TypeSerializer<Any?>)
            put(TypeCode.XChainBridge.value, XChainBridgeSerializer as TypeSerializer<Any?>)

            // Meta-types: all delegate to StObjectSerializer
            put(TypeCode.Transaction.value, StObjectSerializer as TypeSerializer<Any?>)
            put(TypeCode.LedgerEntry.value, StObjectSerializer as TypeSerializer<Any?>)
            put(TypeCode.Validation.value, StObjectSerializer as TypeSerializer<Any?>)
            put(TypeCode.Metadata.value, StObjectSerializer as TypeSerializer<Any?>)
        }

    /**
     * Returns the serializer for the given [typeCode], or `null` if not registered.
     *
     * @param typeCode XRPL type code.
     * @return Corresponding serializer, or `null`.
     */
    public fun getSerializer(typeCode: Int): TypeSerializer<Any?>? = serializers[typeCode]
}
