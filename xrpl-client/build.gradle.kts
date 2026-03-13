plugins {
    id("xrpl.kmp-library")
    id("xrpl.lint")
    id("xrpl.publishing")
}

val catalog = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    "commonMainApi"(project(":xrpl-core"))
    "commonMainApi"(project(":xrpl-binary-codec"))
    "commonMainApi"(project(":xrpl-crypto"))
    "commonMainImplementation"(libs.ktor.client.core)
    "commonMainImplementation"(libs.ktor.client.content.negotiation)
    "commonMainImplementation"(libs.ktor.client.websockets)
    "commonMainImplementation"(libs.ktor.serialization.kotlinx.json)
    "commonMainImplementation"(libs.kotlinx.coroutines.core)

    // Platform-specific HTTP client engines (T5)
    "jvmMainImplementation"(libs.ktor.client.cio)
    "appleMainImplementation"(libs.ktor.client.darwin)
    "linuxMainImplementation"(libs.ktor.client.cio)
    "jsMainImplementation"(libs.ktor.client.js)

    // Test dependencies (T5/R6)
    "commonTestImplementation"(libs.ktor.client.mock)
    "commonTestImplementation"(libs.turbine)
    "commonTestImplementation"(libs.kotlinx.coroutines.test)
    "jvmTestImplementation"(catalog.findLibrary("kotest-property").get())
}
