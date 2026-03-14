@file:Suppress("MagicNumber")

package org.xrpl.sdk.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Difference in seconds between Unix epoch (1970-01-01) and Ripple epoch (2000-01-01).
 *
 * Equivalent to `0x386D4380`.
 */
public const val RIPPLE_EPOCH_DIFF: Long = 946_684_800L

/**
 * Converts a Ripple epoch timestamp (seconds since 2000-01-01T00:00:00Z) to a
 * Unix epoch timestamp in **milliseconds**.
 *
 * @param rippleTime seconds since Ripple epoch.
 * @return milliseconds since Unix epoch.
 */
public fun rippleTimeToUnixTime(rippleTime: Long): Long = (rippleTime + RIPPLE_EPOCH_DIFF) * 1000

/**
 * Converts a Ripple epoch timestamp (seconds since 2000-01-01T00:00:00Z) to a
 * Unix epoch timestamp in **milliseconds**.
 *
 * This overload accepts [UInt], which matches the type used by escrow and check
 * fields such as `CancelAfter` and `FinishAfter`.
 *
 * @param rippleTime seconds since Ripple epoch.
 * @return milliseconds since Unix epoch.
 */
public fun rippleTimeToUnixTime(rippleTime: UInt): Long = rippleTimeToUnixTime(rippleTime.toLong())

/**
 * Converts a Unix epoch timestamp in **milliseconds** to a Ripple epoch timestamp
 * in **seconds**.
 *
 * @param unixTimeMs milliseconds since Unix epoch.
 * @return seconds since Ripple epoch.
 */
public fun unixTimeToRippleTime(unixTimeMs: Long): Long = (unixTimeMs / 1000) - RIPPLE_EPOCH_DIFF

/**
 * Converts a Ripple epoch timestamp to a [kotlinx.datetime.Instant].
 *
 * This is the **recommended primary API** for time conversion in Kotlin code.
 *
 * @param rippleTime seconds since Ripple epoch.
 * @return the corresponding [Instant].
 */
public fun rippleTimeToInstant(rippleTime: Long): Instant =
    Instant.fromEpochMilliseconds(rippleTimeToUnixTime(rippleTime))

/**
 * Converts a [kotlinx.datetime.Instant] to a Ripple epoch timestamp in seconds.
 *
 * This is the **recommended primary API** for time conversion in Kotlin code.
 *
 * @param instant the instant to convert.
 * @return seconds since Ripple epoch.
 */
public fun instantToRippleTime(instant: Instant): Long = instant.epochSeconds - RIPPLE_EPOCH_DIFF

/**
 * Converts a Ripple epoch timestamp to an ISO 8601 string (UTC).
 *
 * @param rippleTime seconds since Ripple epoch.
 * @return ISO 8601 date-time string (e.g. `"2000-01-01T00:00:00Z"`).
 */
public fun rippleTimeToISOTime(rippleTime: Long): String = rippleTimeToInstant(rippleTime).toString()

/**
 * Converts an ISO 8601 date-time string to a Ripple epoch timestamp in seconds.
 *
 * @param iso8601 ISO 8601 date-time string.
 * @return seconds since Ripple epoch.
 */
public fun isoTimeToRippleTime(iso8601: String): Long = instantToRippleTime(Instant.parse(iso8601))

/**
 * Returns the current time as Ripple epoch seconds.
 *
 * The result is returned as [UInt] to match the type used by on-ledger time
 * fields such as `CancelAfter` and `FinishAfter`.
 */
public fun currentRippleTime(): UInt = instantToRippleTime(Clock.System.now()).toUInt()
