plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "osu-framework"
include("spring-application")
include("spring-core")
include("spring-osu-api")
include("spring-osu-beatmap-mirror")
include("spring-osu-extended")
include("spring-osu-persistence")
include("spring-web")
