plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.plugin)
    implementation(libs.ktlint.gradle)
    implementation(libs.vanniktech.publish.plugin)
    implementation(libs.bcv.plugin)
    implementation(libs.dokka.gradle.plugin)
}
