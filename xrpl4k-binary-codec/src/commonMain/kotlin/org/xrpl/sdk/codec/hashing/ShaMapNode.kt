package org.xrpl.sdk.codec.hashing

import org.xrpl.sdk.core.util.hexToByteArray
import org.xrpl.sdk.core.util.toHexString
import org.xrpl.sdk.crypto.CryptoProvider
import org.xrpl.sdk.crypto.hashing.sha512HalfWithPrefix

/**
 * Abstract base class for nodes in a SHAMap tree.
 */
internal sealed class ShaMapNode {
    abstract fun hash(provider: CryptoProvider): String
}

/**
 * Leaf node in a SHAMap tree.
 *
 * @property tag 64-character hex string (32 bytes) — the key/index of this entry.
 * @property data Hex-encoded data for the node (transaction blob, state entry, etc.).
 * @property type The type of data this node holds.
 */
internal class ShaMapLeafNode(
    val tag: String,
    val data: String,
    val type: ShaMapNodeType,
) : ShaMapNode() {
    override fun hash(provider: CryptoProvider): String {
        return when (type) {
            ShaMapNodeType.ACCOUNT_STATE -> {
                // sha512Half(LEAF_NODE + data + tag)
                val payload = data.hexToByteArray() + tag.hexToByteArray()
                sha512HalfWithPrefix(HashPrefix.LEAF_NODE.bytes, payload, provider)
                    .toHexString()
            }
            ShaMapNodeType.TRANSACTION_NO_META -> {
                // sha512Half(TRANSACTION_ID + data)
                val payload = data.hexToByteArray()
                sha512HalfWithPrefix(HashPrefix.TRANSACTION_ID.bytes, payload, provider)
                    .toHexString()
            }
            ShaMapNodeType.TRANSACTION_METADATA -> {
                // sha512Half(TRANSACTION_NODE + data + tag)
                val payload = data.hexToByteArray() + tag.hexToByteArray()
                sha512HalfWithPrefix(HashPrefix.TRANSACTION_NODE.bytes, payload, provider)
                    .toHexString()
            }
        }
    }
}

private const val SLOT_MAX = 15
private const val HEX_RADIX = 16
private const val HASH_HEX_LENGTH = 64

/** 32 bytes of zeros as hex. */
private val HEX_ZERO = "0".repeat(HASH_HEX_LENGTH)

/**
 * Inner (non-leaf) node in a SHAMap tree.
 * Has 16 child branches indexed by hex nibble (0-15).
 *
 * @property depth How many parent inner nodes above this one (root = 0).
 */
internal class ShaMapInnerNode(
    val depth: Int = 0,
) : ShaMapNode() {
    private val branches: Array<ShaMapNode?> = arrayOfNulls(SLOT_MAX + 1)
    private var empty: Boolean = true

    override fun hash(provider: CryptoProvider): String {
        if (empty) return HEX_ZERO

        // Concatenate all 16 child hashes (each 32 bytes = 64 hex chars)
        val childHashes =
            buildString(HASH_HEX_LENGTH * (SLOT_MAX + 1)) {
                for (i in 0..SLOT_MAX) {
                    val child = branches[i]
                    append(child?.hash(provider) ?: HEX_ZERO)
                }
            }

        val payload = childHashes.hexToByteArray()
        return sha512HalfWithPrefix(HashPrefix.INNER_NODE.bytes, payload, provider)
            .toHexString()
    }

    /**
     * Adds a leaf node at the correct branch determined by the tag nibble at [depth].
     */
    fun addItem(
        tag: String,
        node: ShaMapLeafNode,
    ) {
        val nibble = tag[depth].digitToInt(HEX_RADIX)
        val existing = branches[nibble]

        when {
            existing == null -> {
                branches[nibble] = node
                empty = false
            }
            existing is ShaMapInnerNode -> {
                existing.addItem(tag, node)
            }
            existing is ShaMapLeafNode -> {
                if (existing.tag == tag) {
                    error("Tried to add a node to a SHAMap that was already in there.")
                }
                // Create a new inner node and push both existing and new nodes down
                val newInner = ShaMapInnerNode(depth + 1)
                newInner.addItem(existing.tag, existing)
                newInner.addItem(tag, node)
                branches[nibble] = newInner
                // empty is already false
            }
        }
    }
}
