package org.xrpl.sdk.core

/**
 * Marks DSL builder classes for XRPL SDK.
 *
 * Prevents scope leaks at compile time by restricting implicit receiver access
 * in nested builder blocks. All DSL builder classes must be annotated with this marker.
 *
 * @see DslMarker
 */
@DslMarker
@Target(AnnotationTarget.CLASS)
public annotation class XrplDsl
