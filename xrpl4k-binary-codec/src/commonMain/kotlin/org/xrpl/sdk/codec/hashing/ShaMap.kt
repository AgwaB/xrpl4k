package org.xrpl.sdk.codec.hashing

import org.xrpl.sdk.crypto.CryptoProvider

/**
 * SHAMap is the hash structure used to model XRPL ledgers.
 *
 * It is a 256-way radix tree (Merkle tree) where:
 * - Each key (tag) is a 64-character hex string (32 bytes = 256 bits).
 * - Each nibble of the tag determines which of the 16 branches to follow at each level.
 * - Leaf nodes hold the actual data.
 * - Inner nodes hold up to 16 children.
 *
 * If the root hash is equivalent between two SHAMaps, all nodes are equivalent.
 *
 * @property provider Cryptographic provider for SHA-512Half computation.
 */
public class ShaMap(private val provider: CryptoProvider) {
    private val root: ShaMapInnerNode = ShaMapInnerNode(depth = 0)

    /**
     * The hash of the root of this SHAMap tree.
     * Returns 64-char lowercase hex string.
     */
    public val hash: String
        get() = root.hash(provider)

    /**
     * Add an item to the SHAMap.
     *
     * @param tag 64-character hex string identifying the entry (e.g., transaction hash or ledger index).
     * @param data Hex-encoded data for the entry.
     * @param type The type of data being added.
     */
    public fun addItem(
        tag: String,
        data: String,
        type: ShaMapNodeType,
    ) {
        root.addItem(tag, ShaMapLeafNode(tag, data, type))
    }
}
