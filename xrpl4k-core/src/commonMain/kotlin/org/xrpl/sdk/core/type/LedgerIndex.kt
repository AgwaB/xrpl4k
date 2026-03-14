package org.xrpl.sdk.core.type

import kotlin.jvm.JvmInline

/**
 * A ledger sequence number in the XRPL.
 *
 * Wraps an unsigned 32-bit integer representing the position of a ledger in the chain.
 * No additional validation is needed since [UInt] is inherently non-negative.
 *
 * @property value The ledger sequence number.
 */
@JvmInline
public value class LedgerIndex(public val value: UInt)
