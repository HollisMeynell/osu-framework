dependencies {
    api(project(":spring-core"))
    api(project(":spring-osu-api"))
    api(project(":spring-osu-extended"))
    api(project(":spring-osu-persistence"))
    api(project(":spring-osu-beatmap-mirror"))

    api(libs.hoplite)
    api(libs.hoplite.toml)
    api(libs.hikari)
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

