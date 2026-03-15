import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
}

// Wire consumer ProGuard rules so Android consumers get them automatically
val consumerRulesFile = project.file("consumer-rules.pro")
if (consumerRulesFile.exists()) {
    configurations.create("consumerProguardFiles") {
        isCanBeConsumed = true
        isCanBeResolved = false
        outgoing.artifact(consumerRulesFile)
    }
}

val catalog = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

kotlin {
    explicitApi()

    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
    }

    jvm()

    js {
        browser()
        nodejs()
    }

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    linuxX64()

    macosArm64()
    macosX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(catalog.findLibrary("kotlinx-coroutines-test").get())
            implementation(catalog.findLibrary("kotest-framework-engine").get())
            implementation(catalog.findLibrary("kotest-assertions-core").get())
        }
        jvmTest.dependencies {
            implementation(catalog.findLibrary("kotest-runner-junit5").get())
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            if (!project.hasProperty("integration")) {
                systemProperty("kotest.tags", "!Integration")
            }
        }
    }
}
