plugins {
    id("xrpl.kmp-library")
    id("xrpl.lint")
    id("xrpl.publishing")
}

dependencies {
    "commonMainApi"(libs.kotlinx.serialization.json)
    "commonMainApi"(libs.kotlinx.datetime)
}
