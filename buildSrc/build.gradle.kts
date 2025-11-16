import org.gradle.kotlin.dsl.gradlePlugin

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
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