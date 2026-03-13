package org.xrpl.sdk.core.model.amount

import org.xrpl.sdk.core.type.XrpDrops

/** Creates an [XrpAmount] from this integer as XRP (multiplied by 1,000,000 for drops). */
public val Int.xrp: XrpAmount get() = XrpAmount(XrpDrops(this.toLong() * 1_000_000L))

/** Creates an [XrpAmount] from this Long as drops. */
public val Long.drops: XrpAmount get() = XrpAmount(XrpDrops(this))

// NO Double.xrp — floating-point money literals introduce silent rounding errors
