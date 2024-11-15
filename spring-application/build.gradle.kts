import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
    application
}

dependencies {
    api(project(":spring-core"))
    api(project(":spring-web"))
    api(project(":spring-osu-api"))
    api(project(":spring-osu-extended"))
    api(project(":spring-osu-persistence"))
    api(project(":spring-osu-beatmap-mirror"))

    api(libs.hikari)
    api(libs.postgresql)
}

application {
    mainClass = "org.spring.application.Main"
}

tasks.withType<ShadowJar> {
    minimize()
    archiveFileName = "app.jar"
}