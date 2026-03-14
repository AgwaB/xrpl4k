package org.xrpl.sdk.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KotestRunnerSmokeTest : FunSpec({
    test("Kotest FunSpec is discovered by JUnit5 runner") {
        (1 + 1) shouldBe 2
    }

    test("XrplSdk VERSION is set") {
        XrplSdk.VERSION shouldBe "1.0.0-SNAPSHOT"
    }
})
