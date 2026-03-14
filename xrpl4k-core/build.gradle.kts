plugins {
    id("xrpl4k.kmp-library")
    id("xrpl4k.lint")
    id("xrpl4k.publishing")
}

dependencies {
    "commonMainApi"(libs.kotlinx.serialization.json)
    "commonMainApi"(libs.kotlinx.datetime)
}
