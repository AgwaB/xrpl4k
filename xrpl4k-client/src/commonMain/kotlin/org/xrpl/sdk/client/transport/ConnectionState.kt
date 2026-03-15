package org.xrpl.sdk.client.transport

/**
 * Represents the connection state of a WebSocket transport.
 */
public sealed class ConnectionState {
    /** Not connected. Initial state. */
    public data object Disconnected : ConnectionState()

    /** Actively connecting to the server. */
    public data object Connecting : ConnectionState()

    /** Connected and ready for communication. */
    public data object Connected : ConnectionState()

    /** Auto-reconnecting after an unexpected disconnect. */
    public class Reconnecting(
        /** The attempt number (1-based). */
        public val attempt: Int,
        /** The cause of the disconnect that triggered reconnection. */
        public val cause: Throwable,
    ) : ConnectionState() {
        override fun equals(other: Any?): Boolean =
            other is Reconnecting && attempt == other.attempt && cause == other.cause

        override fun hashCode(): Int = 31 * attempt + cause.hashCode()

        override fun toString(): String = "ConnectionState.Reconnecting(attempt=$attempt, cause=$cause)"
    }

    /** Connection failed or was lost. Terminal when auto-reconnect is exhausted or disabled. */
    public class Failed(public val cause: Throwable) : ConnectionState() {
        override fun equals(other: Any?): Boolean = other is Failed && cause == other.cause

        override fun hashCode(): Int = cause.hashCode()

        override fun toString(): String = "ConnectionState.Failed(cause=$cause)"
    }
}
