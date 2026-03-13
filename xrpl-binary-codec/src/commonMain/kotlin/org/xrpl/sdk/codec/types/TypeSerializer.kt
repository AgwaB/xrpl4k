package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter

/**
 * Interface for serializing and deserializing XRPL binary format types.
 *
 * Each implementation handles one serializable type (e.g. UInt32, Amount, Blob).
 */
internal interface TypeSerializer<T> {
    /**
     * Writes [value] to the [writer] in XRPL binary format.
     *
     * @param writer Destination binary writer.
     * @param value Value to serialize.
     */
    public fun write(
        writer: BinaryWriter,
        value: T,
    )

    /**
     * Reads a value of this type from [reader].
     *
     * @param reader Source binary reader.
     * @return Deserialized value.
     */
    public fun read(reader: BinaryReader): T
}
