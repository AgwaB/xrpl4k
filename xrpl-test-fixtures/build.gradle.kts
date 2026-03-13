plugins {
    id("xrpl.kmp-library")
    id("xrpl.lint")
}

dependencies {
    "commonMainApi"(project(":xrpl-core"))
    "commonMainApi"(project(":xrpl-client"))
    "commonMainImplementation"(libs.ktor.client.mock)
    "commonMainImplementation"(libs.kotlinx.coroutines.test)
}
