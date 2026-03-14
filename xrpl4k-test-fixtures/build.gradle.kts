plugins {
    id("xrpl4k.kmp-library")
    id("xrpl4k.lint")
}

dependencies {
    "commonMainApi"(project(":xrpl4k-core"))
    "commonMainApi"(project(":xrpl4k-client"))
    "commonMainImplementation"(libs.ktor.client.mock)
    "commonMainImplementation"(libs.kotlinx.coroutines.test)
}
