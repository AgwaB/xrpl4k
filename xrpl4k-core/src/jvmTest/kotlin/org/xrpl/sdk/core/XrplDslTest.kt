package org.xrpl.sdk.core

import kotlin.test.Test
import kotlin.test.assertTrue

@XrplDsl
private class SampleDslScope

class XrplDslTest {
    @Test
    fun `XrplDsl annotation is applicable to classes`() {
        // @XrplDsl is annotated with @DslMarker (compile-time guarantee).
        // If this file compiles, the DslMarker meta-annotation is correct.
        val scope = SampleDslScope()
        assertTrue(scope is SampleDslScope, "@XrplDsl should be applicable to classes")
    }

    @Test
    fun `XrplDsl targets CLASS only`() {
        val target =
            XrplDsl::class.annotations
                .filterIsInstance<Target>()
                .firstOrNull()
        assertTrue(target != null, "@XrplDsl should have @Target annotation")
        assertTrue(
            target.allowedTargets.contentEquals(arrayOf(AnnotationTarget.CLASS)),
            "@XrplDsl should target CLASS only",
        )
    }
}
