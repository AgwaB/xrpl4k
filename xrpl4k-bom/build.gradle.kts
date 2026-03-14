plugins {
    `java-platform`
    id("xrpl4k.publishing")
}

dependencies {
    constraints {
        api(project(":xrpl4k-core"))
        api(project(":xrpl4k-binary-codec"))
        api(project(":xrpl4k-crypto"))
        api(project(":xrpl4k-client"))
    }
}
