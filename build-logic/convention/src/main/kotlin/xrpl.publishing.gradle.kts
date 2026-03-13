plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set(project.name)
        description.set("XRPL Kotlin Multiplatform SDK")
        url.set("https://github.com/anthropics/xrpl-kotlin")
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                name.set("XRPL Kotlin Contributors")
                url.set("https://github.com/anthropics/xrpl-kotlin")
            }
        }
        scm {
            url.set("https://github.com/anthropics/xrpl-kotlin")
            connection.set("scm:git:git://github.com/anthropics/xrpl-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/anthropics/xrpl-kotlin.git")
        }
    }
}
