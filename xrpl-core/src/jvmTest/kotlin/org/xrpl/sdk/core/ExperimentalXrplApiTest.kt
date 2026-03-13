package org.xrpl.sdk.core

import kotlin.test.Test
import kotlin.test.assertTrue

class ExperimentalXrplApiTest {
    @ExperimentalXrplApi
    private fun experimentalFunction(): String = "experimental"

    @Test
    fun `ExperimentalXrplApi has BINARY retention`() {
        val retention =
            ExperimentalXrplApi::class.annotations
                .filterIsInstance<Retention>()
                .firstOrNull()
        assertTrue(retention != null, "@ExperimentalXrplApi should have @Retention annotation")
        assertTrue(
            retention.value == AnnotationRetention.BINARY,
            "@ExperimentalXrplApi should have BINARY retention",
        )
    }

    @Test
    fun `ExperimentalXrplApi targets CLASS, FUNCTION, PROPERTY, TYPEALIAS`() {
        val target =
            ExperimentalXrplApi::class.annotations
                .filterIsInstance<Target>()
                .firstOrNull()
        assertTrue(target != null, "@ExperimentalXrplApi should have @Target annotation")
        val expected =
            setOf(
                AnnotationTarget.CLASS,
                AnnotationTarget.FUNCTION,
                AnnotationTarget.PROPERTY,
                AnnotationTarget.TYPEALIAS,
            )
        assertTrue(
            target.allowedTargets.toSet() == expected,
            "@ExperimentalXrplApi should target CLASS, FUNCTION, PROPERTY, TYPEALIAS",
        )
    }

    @Test
    fun `OptIn mechanism works at runtime`() {
        @OptIn(ExperimentalXrplApi::class)
        val result = experimentalFunction()
        assertTrue(result == "experimental", "Opt-in mechanism should allow calling experimental APIs")
    }
}
