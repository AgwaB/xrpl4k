plugins {
    id("xrpl4k.kmp-library")
    id("xrpl4k.lint")
    id("xrpl4k.publishing")
}

val catalog = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    "commonMainApi"(project(":xrpl4k-core"))
    "commonMainImplementation"(catalog.findLibrary("kotlinx-serialization-json").get())
    "jvmTestImplementation"(catalog.findLibrary("kotest-property").get())
}
