dependencies {
    api(libs.jackson)
    api(libs.jackson.toml)
    api(libs.jackson.datatype)
    api(libs.kotlin.reflect)
    api(libs.kotlin.coroutines)
    api(libs.kotlin.logging)
    api(libs.ktor.client.core)
    api(libs.logback.classic)
    api(libs.logback.core)
}

plugins {
    `maven-publish`
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "core"
            version = project.version.toString()
            from(components["java"])
        }
    }
}
