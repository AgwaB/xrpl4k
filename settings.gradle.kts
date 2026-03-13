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

rootProject.name = "xrpl-kotlin"

include(":xrpl-core")
include(":xrpl-binary-codec")
include(":xrpl-crypto")
include(":xrpl-client")
include(":xrpl-bom")
include(":xrpl-test-fixtures")
