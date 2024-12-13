plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    application

    `java-library`
    `maven-publish`
}

application {
    mainClass = "org.spring.application.Main"
}

allprojects {
    group = "org.spring"
    version = "0.0.1"

    plugins.apply(rootProject.libs.plugins.kotlin.jvm.get().pluginId)

    repositories {
//        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        mavenCentral()
    }

    kotlin {
        jvmToolchain(21)
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }
}

tasks.register("publishAllToLocal") {
    dependsOn(
        ":spring-core:publishToMavenLocal",
        ":spring-osu-api:publishToMavenLocal",
        ":spring-osu-extended:publishToMavenLocal",
    )
}

tasks.register<JavaExec>("runApplication") {
    group = "spring-application"
    description = "Run the Application"
    mainClass = "org.spring.application.MainKt"
    classpath = project(":spring-application").sourceSets["main"].runtimeClasspath
}