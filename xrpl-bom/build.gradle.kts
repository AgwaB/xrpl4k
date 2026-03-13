plugins {
    `java-platform`
    id("xrpl.publishing")
}

dependencies {
    constraints {
        api(project(":xrpl-core"))
        api(project(":xrpl-binary-codec"))
        api(project(":xrpl-crypto"))
        api(project(":xrpl-client"))
    }
}
