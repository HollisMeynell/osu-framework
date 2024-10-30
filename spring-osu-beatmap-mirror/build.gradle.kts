dependencies {
    api(project(":spring-core"))
    api(project(":spring-osu-api"))
    api(project(":spring-osu-extended"))
    api(project(":spring-osu-persistence"))

    api(libs.xz)
    api(libs.commons.compress)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.time)
}