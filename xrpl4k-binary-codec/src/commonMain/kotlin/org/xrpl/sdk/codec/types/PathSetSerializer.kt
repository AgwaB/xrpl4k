@file:Suppress("MagicNumber")

package org.xrpl.sdk.codec.types

import org.xrpl.sdk.codec.binary.BinaryReader
import org.xrpl.sdk.codec.binary.BinaryWriter
import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString

/**
 * Serializer for the XRPL PathSet type.
 *
 * A PathSet is a list of paths, where each path is a list of path steps.
 * Paths are separated by `0xFF` and the set is terminated by `0x00`.
 *
 * Each step has a 1-byte type flag indicating which optional components are present:
 * - Bit 0 (0x01): account (20 bytes)
 * - Bit 1 (0x10): currency (20 bytes)
 * - Bit 2 (0x20): issuer (20 bytes)
 *
 * Input format: `List<List<Map<String, String>>>` where each step map may contain
 * `"account"`, `"currency"`, and `"issuer"` keys (hex strings).
 */
internal object PathSetSerializer : TypeSerializer<List<List<Map<String, String>>>> {
    /** Path separator byte between paths within a set. */
    private const val PATH_SEPARATOR: Int = 0xFF

    /** PathSet terminator byte. */
    private const val PATH_SET_END: Int = 0x00

    /** Type flag for account component. */
    private const val TYPE_ACCOUNT: Int = 0x01

    /** Type flag for currency component. */
    private const val TYPE_CURRENCY: Int = 0x10

    /** Type flag for issuer component. */
    private const val TYPE_ISSUER: Int = 0x20

    /** Size of account, currency, and issuer components in bytes. */
    private const val COMPONENT_SIZE: Int = 20

    override fun write(
        writer: BinaryWriter,
        value: List<List<Map<String, String>>>,
    ) {
        for ((pathIndex, path) in value.withIndex()) {
            if (pathIndex > 0) {
                writer.writeUInt8(PATH_SEPARATOR)
            }
            for (step in path) {
                writeStep(writer, step)
            }
        }
        writer.writeUInt8(PATH_SET_END)
    }

    override fun read(reader: BinaryReader): List<List<Map<String, String>>> {
        val paths = mutableListOf<List<Map<String, String>>>()
        var currentPath = mutableListOf<Map<String, String>>()

        while (!reader.isExhausted()) {
            val nextByte = reader.peek()
            when (nextByte) {
                PATH_SET_END -> {
                    reader.readUInt8() // consume
                    if (currentPath.isNotEmpty()) {
                        paths.add(currentPath)
                    }
                    break
                }
                PATH_SEPARATOR -> {
                    reader.readUInt8() // consume
                    paths.add(currentPath)
                    currentPath = mutableListOf()
                }
                else -> {
                    currentPath.add(readStep(reader))
                }
            }
        }

        return paths
    }

    private fun writeStep(
        writer: BinaryWriter,
        step: Map<String, String>,
    ) {
        var typeFlag = 0
        if (step.containsKey("account")) typeFlag = typeFlag or TYPE_ACCOUNT
        if (step.containsKey("currency")) typeFlag = typeFlag or TYPE_CURRENCY
        if (step.containsKey("issuer")) typeFlag = typeFlag or TYPE_ISSUER

        writer.writeUInt8(typeFlag)

        if (step.containsKey("account")) {
            val bytes = step["account"]!!.hexToByteArray()
            require(bytes.size == COMPONENT_SIZE) {
                "Path step account must be $COMPONENT_SIZE bytes. Got ${bytes.size}"
            }
            writer.writeBytes(bytes)
        }
        if (step.containsKey("currency")) {
            CurrencySerializer.write(writer, step["currency"]!!)
        }
        if (step.containsKey("issuer")) {
            val bytes = step["issuer"]!!.hexToByteArray()
            require(bytes.size == COMPONENT_SIZE) {
                "Path step issuer must be $COMPONENT_SIZE bytes. Got ${bytes.size}"
            }
            writer.writeBytes(bytes)
        }
    }

    private fun readStep(reader: BinaryReader): Map<String, String> {
        val typeFlag = reader.readUInt8()
        val step = mutableMapOf<String, String>()

        if (typeFlag and TYPE_ACCOUNT != 0) {
            step["account"] = reader.readBytes(COMPONENT_SIZE).toHexString()
        }
        if (typeFlag and TYPE_CURRENCY != 0) {
            step["currency"] = CurrencySerializer.read(reader)
        }
        if (typeFlag and TYPE_ISSUER != 0) {
            step["issuer"] = reader.readBytes(COMPONENT_SIZE).toHexString()
        }

        return step
    }
}
