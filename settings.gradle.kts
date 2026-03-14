pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "xrpl4k"

include(":xrpl4k-core")
include(":xrpl4k-binary-codec")
include(":xrpl4k-crypto")
include(":xrpl4k-client")
include(":xrpl4k-bom")
include(":xrpl4k-test-fixtures")
