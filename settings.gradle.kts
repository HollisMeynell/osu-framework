plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "osu-framework"
include("spring-core")
include("spring-application")
include("spring-osu-api")
include("spring-osu-extended")
include("spring-web")
