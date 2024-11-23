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