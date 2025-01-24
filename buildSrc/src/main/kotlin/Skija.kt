import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies


class Skija : Plugin<Project> {
    private val os = System.getProperty("os.name").lowercase()

    override fun apply(target: Project) {
        val libs = target.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
        val skijaLib = when {
            os.startsWith("linux") -> libs.findLibrary("skija.linux").get()
            os.startsWith("win") -> libs.findLibrary("skija.windows").get()
            os.startsWith("mac") -> libs.findLibrary("skija.macos").get()
            else -> error("Unsupported OS: $os")
        }
        target.dependencies {
            add("api", skijaLib)
        }
    }
}