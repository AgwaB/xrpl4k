package org.xrpl.sdk.codec

import org.xrpl.sdk.core.XrplSdk
import kotlin.test.Test
import kotlin.test.assertTrue

class ModuleDependencyTest {
    @Test
    fun `xrpl-core types are accessible from xrpl-binary-codec`() {
        // Validates the diamond dependency direction: sibling modules can see xrpl-core types
        val version = XrplSdk.VERSION
        assertTrue(version.isNotEmpty(), "Should be able to access XrplSdk.VERSION from xrpl-binary-codec")
    }
}
