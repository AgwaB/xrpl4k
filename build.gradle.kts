plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.bcv)
    alias(libs.plugins.dokka)
}

apiValidation {
    ignoredProjects += listOf("xrpl4k-test-fixtures", "xrpl4k-bom")
}

val releaseVersion: String? by project

allprojects {
    group = "io.github.agwab"
    version = releaseVersion ?: "0.2.0-SNAPSHOT"
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    extensions.getByType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().download = false
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    extensions.getByType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().download = false
}
