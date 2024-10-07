dependencies {
    api(project(":spring-core"))

    api(libs.ktor.server.auth)
    api(libs.ktor.server.core)
    api(libs.ktor.server.cio)
    api(libs.ktor.server.cors)
    api(libs.ktor.server.head)
    api(libs.ktor.server.status)
    api(libs.ktor.server.partial)
    api(libs.ktor.server.compression)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.serialization.jackson)
}