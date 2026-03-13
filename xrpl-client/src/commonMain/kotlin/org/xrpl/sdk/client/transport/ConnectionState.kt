package org.xrpl.sdk.client.transport

/**
 * Represents the connection state of a WebSocket transport.
 *
 * Phase 3 treats disconnect as terminal — there is no `Reconnecting` state.
 * Auto-reconnect will be added in a future phase alongside auto-resubscribe.
 */
public sealed class ConnectionState {
    /** Not connected. Initial state. */
    public data object Disconnected : ConnectionState()

    /** Actively connecting to the server. */
    public data object Connecting : ConnectionState()

    /** Connected and ready for communication. */
    public data object Connected : ConnectionState()

    /** Connection failed or was lost. Terminal state in Phase 3. */
    public class Failed(public val cause: Throwable) : ConnectionState() {
        override fun equals(other: Any?): Boolean = other is Failed && cause == other.cause

        override fun hashCode(): Int = cause.hashCode()

        override fun toString(): String = "ConnectionState.Failed(cause=$cause)"
    }
}
