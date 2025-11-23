import org.gradle.kotlin.dsl.gradlePlugin

plugins {
    `kotlin-dsl`
    alias(libs.plugins.jreleaser)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
}

dependencies {
    implementation(libs.jreleaser.lib)
}

gradlePlugin {
    plugins {
        create("skija") {
            id = "skija"
            implementationClass = "Skija"
        }
        create("maven-publish-plugin") {
            id = "maven-publish-plugin"
            implementationClass = "MavenPush"
        }
    }
}