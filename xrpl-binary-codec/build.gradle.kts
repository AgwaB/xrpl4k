plugins {
    id("xrpl.kmp-library")
    id("xrpl.lint")
    id("xrpl.publishing")
}

val catalog = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    "commonMainApi"(project(":xrpl-core"))
    "commonMainImplementation"(catalog.findLibrary("kotlinx-serialization-json").get())
    "jvmTestImplementation"(catalog.findLibrary("kotest-property").get())
}
