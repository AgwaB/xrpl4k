package org.xrpl.sdk.codec.definitions

import kotlin.jvm.JvmInline

/**
 * Type codes from the XRPL binary format.
 *
 * Each serializable type in the XRP Ledger protocol is assigned a unique
 * integer code used in field header encoding.
 */
@JvmInline
internal value class TypeCode(val value: Int) {
    internal companion object {
        val Unknown: TypeCode = TypeCode(-2)
        val Done: TypeCode = TypeCode(-1)
        val NotPresent: TypeCode = TypeCode(0)
        val UInt16: TypeCode = TypeCode(1)
        val UInt32: TypeCode = TypeCode(2)
        val UInt64: TypeCode = TypeCode(3)
        val Hash128: TypeCode = TypeCode(4)
        val Hash256: TypeCode = TypeCode(5)
        val Amount: TypeCode = TypeCode(6)
        val Blob: TypeCode = TypeCode(7)
        val AccountID: TypeCode = TypeCode(8)
        val Number: TypeCode = TypeCode(9)
        val Int32: TypeCode = TypeCode(10)
        val Int64: TypeCode = TypeCode(11)
        val STObject: TypeCode = TypeCode(14)
        val STArray: TypeCode = TypeCode(15)
        val UInt8: TypeCode = TypeCode(16)
        val Hash160: TypeCode = TypeCode(17)
        val PathSet: TypeCode = TypeCode(18)
        val Vector256: TypeCode = TypeCode(19)
        val UInt96: TypeCode = TypeCode(20)
        val Hash192: TypeCode = TypeCode(21)
        val UInt384: TypeCode = TypeCode(22)
        val UInt512: TypeCode = TypeCode(23)
        val Issue: TypeCode = TypeCode(24)
        val XChainBridge: TypeCode = TypeCode(25)
        val Currency: TypeCode = TypeCode(26)
        val Transaction: TypeCode = TypeCode(10001)
        val LedgerEntry: TypeCode = TypeCode(10002)
        val Validation: TypeCode = TypeCode(10003)
        val Metadata: TypeCode = TypeCode(10004)
    }
}
