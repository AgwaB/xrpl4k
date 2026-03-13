package org.xrpl.sdk.crypto.codec

import org.xrpl.sdk.core.type.Address

/**
 * Components decoded from an X-Address.
 *
 * @property classicAddress The classic XRPL r-address.
 * @property tag The destination tag, or `null` if no tag is encoded.
 * @property isTest `true` if this is a testnet address.
 */
public class XAddressComponents(
    public val classicAddress: Address,
    public val tag: UInt?,
    public val isTest: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is XAddressComponents) return false
        return classicAddress == other.classicAddress &&
            tag == other.tag &&
            isTest == other.isTest
    }

    override fun hashCode(): Int {
        var result = classicAddress.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + isTest.hashCode()
        return result
    }

    override fun toString(): String = "XAddressComponents(classicAddress=$classicAddress, tag=$tag, isTest=$isTest)"
}
