plugins {
//    alias(libs.plugins.extra.module.info)
}
dependencies {
    api(project(":spring-core"))
    api(project(":spring-osu-api"))
    api(project(":spring-osu-extended"))

    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.time)
}
/*
extraJavaModuleInfo {
    module("org.jetbrains.exposed:exposed-core", "org.jetbrains.exposed.core") {
        exports("org.apache.commons.beanutils")
        requiresTransitive("org.apache.commons.logging")
        requires("java.sql")
        requires("java.desktop")
    }
}
 */