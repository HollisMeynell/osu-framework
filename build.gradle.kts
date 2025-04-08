plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    application
    `java-library`
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

tasks.register("buildApplication") {
    group = "build"
    description = "Build the application fat jar"
    dependsOn(":spring-application:shadowJar")

    doLast {
        val sourceJar = file("spring-application/build/libs/app.jar")
        val targetDir = file("build/libs")
        val targetJar = targetDir.resolve("app.jar")

        targetDir.mkdirs()
        if (sourceJar.exists()) {
            sourceJar.copyTo(targetJar, true)
        } else {
            throw GradleException("Source JAR file not found: ${sourceJar.path}")
        }
    }
}

tasks.register<JavaExec>("runApplication") {
    group = "application"
    description = "Run the Application"
    mainClass = "org.spring.application.MainKt"
    classpath = project(":spring-application").sourceSets["main"].runtimeClasspath
}