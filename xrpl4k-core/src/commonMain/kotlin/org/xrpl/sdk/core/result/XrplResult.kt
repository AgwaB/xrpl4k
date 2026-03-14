package org.xrpl.sdk.core.result

/**
 * Represents the result of an XRPL operation.
 *
 * @param T the type of the success value.
 */
public sealed class XrplResult<out T> {
    /** A successful result containing [value]. */
    public data class Success<out T>(public val value: T) : XrplResult<T>()

    /** A failed result containing a [XrplFailure] describing the error. */
    public data class Failure(public val error: XrplFailure) : XrplResult<Nothing>()
}

/**
 * The types of failures that can occur in XRPL operations.
 */
public sealed class XrplFailure {
    /** An RPC error returned by the XRPL node. */
    public data class RpcError(
        public val errorCode: Int,
        public val errorMessage: String,
    ) : XrplFailure()

    /** A network connectivity error. */
    public data class NetworkError(
        public val message: String,
        public val cause: Throwable? = null,
    ) : XrplFailure()

    /** A local validation error (e.g., invalid transaction fields). */
    public data class ValidationError(
        public val message: String,
    ) : XrplFailure()

    /** A transaction engine code error (tec class). */
    public data class TecError(
        public val code: Int,
        public val message: String,
    ) : XrplFailure()

    /** The requested resource was not found. */
    public data object NotFound : XrplFailure()
}

// Convenience extensions

/** Returns the success value or null if this is a failure. */
public fun <T> XrplResult<T>.getOrNull(): T? =
    when (this) {
        is XrplResult.Success -> value
        is XrplResult.Failure -> null
    }

/** Returns the success value or throws [XrplException]. */
public fun <T> XrplResult<T>.getOrThrow(): T =
    when (this) {
        is XrplResult.Success -> value
        is XrplResult.Failure -> throw XrplException(error)
    }

/** Transforms the success value using [transform]. Failures pass through unchanged. */
public inline fun <T, R> XrplResult<T>.map(transform: (T) -> R): XrplResult<R> =
    when (this) {
        is XrplResult.Success -> XrplResult.Success(transform(value))
        is XrplResult.Failure -> this
    }

/** Transforms the success value into another [XrplResult]. Failures pass through unchanged. */
public inline fun <T, R> XrplResult<T>.flatMap(transform: (T) -> XrplResult<R>): XrplResult<R> =
    when (this) {
        is XrplResult.Success -> transform(value)
        is XrplResult.Failure -> this
    }

/** Executes [action] if this is a success. Returns this for chaining. */
public inline fun <T> XrplResult<T>.onSuccess(action: (T) -> Unit): XrplResult<T> =
    apply {
        if (this is XrplResult.Success) action(value)
    }

/** Executes [action] if this is a failure. Returns this for chaining. */
public inline fun <T> XrplResult<T>.onFailure(action: (XrplFailure) -> Unit): XrplResult<T> =
    apply {
        if (this is XrplResult.Failure) action(error)
    }

/** Applies [onSuccess] or [onFailure] depending on the result. */
public inline fun <T, R> XrplResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (XrplFailure) -> R,
): R =
    when (this) {
        is XrplResult.Success -> onSuccess(value)
        is XrplResult.Failure -> onFailure(error)
    }
