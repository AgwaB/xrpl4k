plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set(project.name)
        description.set("xrpl4k — Kotlin Multiplatform SDK for the XRP Ledger")
        url.set("https://github.com/AgwaB/xrpl4k")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("agwab")
                name.set("AgwaB")
                url.set("https://github.com/AgwaB")
            }
        }
        scm {
            url.set("https://github.com/AgwaB/xrpl4k")
            connection.set("scm:git:git://github.com/AgwaB/xrpl4k.git")
            developerConnection.set("scm:git:ssh://github.com/AgwaB/xrpl4k.git")
        }
    }
}
