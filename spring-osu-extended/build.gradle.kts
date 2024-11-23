import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    api(project(":spring-core"))
    api(project(":spring-osu-api"))

    api(libs.ktor.client.core)
    api(libs.xz)
    api(libs.commons.compress)
}

plugins {
    alias(libs.plugins.shadow)
    `maven-publish`
}

tasks.withType<ShadowJar> {
    minimize {
        exclude(
            "com.fasterxml.jackson.*",
            "org.spring.osu.extended.rosu.*",
            "org.spring.osu.model.*",
            "org.spring.core.*",
        )
    }
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.spring.osu"
            artifactId = "osu-extended"
            version = project.version.toString()
            from(components["java"])
        }
    }
}