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
    api(libs.kotlin.io.core)
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
    artifactId = "spring-core"
}

