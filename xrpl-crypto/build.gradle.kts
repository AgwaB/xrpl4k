plugins {
    id("xrpl.kmp-library")
    id("xrpl.lint")
    id("xrpl.publishing")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.bouncycastle.provider)
        }
    }
}

val catalog = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    "commonMainApi"(project(":xrpl-core"))
    "jvmTestImplementation"(catalog.findLibrary("kotest-property").get())
}
