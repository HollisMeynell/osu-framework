plugins {
    `java-gradle-plugin`
}

dependencies {
    api(libs.asm)
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    mavenCentral()
}

gradlePlugin {
    plugins {
        // here we register our plugin with an ID
        register("extra-java-module-info") {
            id = "extra-java-module-info"
            implementationClass = "org.gradle.javamodules.ExtraModuleInfoPlugin"
        }
    }
}