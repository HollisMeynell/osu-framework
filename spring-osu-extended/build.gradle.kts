import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate

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

task("buildNative") {
    val libDir = layout.projectDirectory.dir("src/main/resources/lib").asFile.toPath()
    Files.createDirectories(libDir)
    val needBuild: Boolean =
        Files.find(libDir, 1, { path, _ ->
            path.fileName.toString().endsWith(".so") || path.fileName.toString().endsWith(".dll") || path.fileName.toString().endsWith(".dylib")
        }).count() == 0L
    println(needBuild)
    if (!needBuild) {
        return@task
    }
    val nativeDir = layout.projectDirectory.dir("native").asFile
    val testCmd = ProcessBuilder("cargo", "--version")
        .directory(nativeDir)
        .start()
    if (testCmd.waitFor() != 0)
        throw Exception("rust environment not find, can not build.")
println("cargo build")
    val cmd = ProcessBuilder("cargo", "build", "--release")
        .directory(nativeDir)
        .start()
    if (cmd.waitFor() != 0) throw Exception("build rust error.")
    Files.find(nativeDir.toPath().resolve("target"), 2, { path, attr ->
        path.fileName.toString().endsWith(".so") || path.fileName.toString().endsWith(".dll") || path.fileName.toString().endsWith(".dylib")
    }).forEach {
        Files.copy(it, libDir.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING)
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("buildNative")
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "osu-extended"
            version = project.version.toString()
            from(components["java"])
        }
    }
}