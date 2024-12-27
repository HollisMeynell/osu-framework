dependencies {
    api(project(":spring-core"))
    api(project(":spring-osu-api"))
    api(project(":spring-osu-extended"))

    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.json)
    api(libs.exposed.jdbc)
    api(libs.exposed.time)
}