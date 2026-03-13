package org.xrpl.sdk.core

/**
 * Marks declarations that are experimental in the XRPL Kotlin SDK.
 *
 * Experimental APIs may change or be removed without prior notice in minor versions.
 * Consumers must opt in with `@OptIn(ExperimentalXrplApi::class)` to use these APIs.
 *
 * Once an experimental API is stabilized, this annotation will be removed and the API
 * will follow the standard deprecation lifecycle for any future changes.
 */
@RequiresOptIn(
    message = "This XRPL SDK API is experimental and may change without notice.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
public annotation class ExperimentalXrplApi
