package org.xrpl.sdk.core.type

/**
 * Converts this string to an [Address].
 *
 * @throws IllegalArgumentException if the string is not a valid classic XRPL address.
 */
public fun String.toAddress(): Address = Address(this)

/**
 * Converts this [XrpDrops] amount to a human-readable XRP string.
 *
 * Alias for [XrpDrops.toXrp] provided for consistent naming with other SDK string conversions.
 */
public fun XrpDrops.toXrpString(): String = toXrp()

/**
 * Converts this string to a [TxHash].
 *
 * @throws IllegalArgumentException if the string is not exactly 64 valid hex characters.
 */
public fun String.toTxHash(): TxHash = TxHash(this)

/**
 * Converts this string to a [Hash256].
 *
 * @throws IllegalArgumentException if the string is not exactly 64 valid hex characters.
 */
public fun String.toHash256(): Hash256 = Hash256(this)
