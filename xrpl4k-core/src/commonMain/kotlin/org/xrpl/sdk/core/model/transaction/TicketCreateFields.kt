package org.xrpl.sdk.core.model.transaction

import org.xrpl.sdk.core.XrplDsl

// ── TicketCreate ──────────────────────────────────────────────────────────────

/**
 * Fields specific to a TicketCreate transaction.
 *
 * @property ticketCount The number of Tickets to create.
 */
public class TicketCreateFields(
    public val ticketCount: UInt,
) : TransactionFields {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TicketCreateFields) return false
        return ticketCount == other.ticketCount
    }

    override fun hashCode(): Int = ticketCount.hashCode()

    override fun toString(): String = "TicketCreateFields(ticketCount=$ticketCount)"
}

/**
 * DSL builder for [TicketCreateFields].
 */
@XrplDsl
public class TicketCreateBuilder internal constructor() {
    /** The number of Tickets to create. Required. */
    public var ticketCount: UInt = 0u

    internal fun build(): TicketCreateFields = TicketCreateFields(ticketCount = ticketCount)
}
