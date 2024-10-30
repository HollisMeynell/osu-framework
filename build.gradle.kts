plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    application
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
}
