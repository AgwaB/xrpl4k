plugins {
    id("xrpl4k.kmp-library")
    id("xrpl4k.lint")
    id("xrpl4k.publishing")
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
    "commonMainApi"(project(":xrpl4k-core"))
    "jvmTestImplementation"(catalog.findLibrary("kotest-property").get())
}
