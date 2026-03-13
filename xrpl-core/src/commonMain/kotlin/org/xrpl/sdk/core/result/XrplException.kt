package org.xrpl.sdk.core.result

/**
 * Exception thrown when [XrplResult.getOrThrow] is called on a failed result.
 */
public class XrplException(
    /** The failure that caused this exception. */
    public val failure: XrplFailure,
) : RuntimeException("XRPL operation failed: $failure")
