dependencies {
    api(project(":spring-core"))

    api(libs.kotlin.reflect)
    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.jackson)
}

plugins {
    `maven-publish-plugin`
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

java {
    withSourcesJar()
    withJavadocJar()
}

`maven-publish-plugin` {
    groupId = "xyz.365246692.mvn"
    artifactId = "spring-api"
}