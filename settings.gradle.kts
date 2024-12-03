plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
rootProject.name = "osu-framework"
include("spring-core")
include("spring-application")
include("spring-osu-api")
include("spring-osu-extended")
include("spring-web")
include("spring-osu-persistence")
include("spring-osu-beatmap-mirror")
include("spring-image")
