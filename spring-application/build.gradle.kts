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