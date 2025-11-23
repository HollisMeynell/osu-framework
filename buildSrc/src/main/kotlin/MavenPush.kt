import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.plugins.signing.SigningExtension
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.model.Active

class MavenPush : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("maven-publish-plugin", MavenPublishExtension::class.java)

        target.setPlugins("maven-publish")
        target.setPlugins("signing")
        target.setPlugins("org.jreleaser")

        target.tasks.register("pushToMavenCentral") {
            dependsOn("publish")
            dependsOn("jreleaserFullRelease")
        }

        target.afterEvaluate {
            val publishURI = layout.buildDirectory.dir("publish").get()
            configPublishing(extension, publishURI)
            configSigning()
            configJreleaser(publishURI)
        }
    }

    private fun Project.setPlugins(id: String) {
        plugins.apply(id)
    }

    private fun Project.configPublishing(extension: MavenPublishExtension, publishURI: Directory) {
        extensions.configure<PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("maven") {
                    groupId = extension.groupId ?: project.group.toString()
                    artifactId = extension.artifactId ?: project.name
                    version = extension.version ?: project.version.toString()

                    description = extension.description ?: project.description

                    components.findByName("java")?.let { from(it) }

                    tasks.findByName("sourcesJar")?.let { artifact(it) }
                    tasks.findByName("javadocJar")?.let { artifact(it) }

                    pom {
                        name.set(project.name)
                        description.set(extension.description ?: project.description)
                        url.set("https://github.com/HollisMeynell/osu-framework")

                        licenses {
                            license {
                                name.set("MIT")
                                url.set("https://opensource.org/license/mit")
                                distribution.set("repo")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/HollisMeynell/osu-framework.git")
                            developerConnection.set("scm:git:ssh://github.com:HollisMeynell/osu-framework.git")
                            url.set("https://github.com/HollisMeynell/osu-framework")
                        }
                        developers {
                            developer {
                                id.set("spring")
                                name.set("spring")
                                email.set("365246692@qq.com")
                            }
                        }
                    }
                }
            }

            repositories {
                maven {
                    url = publishURI.asFile.toURI()
                }
            }
        }
    }

    private fun Project.configSigning() {
        val key = System.getenv("GPG_PRIVATE_KEY")?.let {
            String(java.util.Base64.getDecoder().decode(it))
        }
        val pass = System.getenv("GPG_PASSPHRASE")

        extensions.configure(SigningExtension::class.java) {
            if (!key.isNullOrBlank()) {
                useInMemoryPgpKeys(key, pass)

                val publishing = extensions.getByType(PublishingExtension::class.java)
                sign(publishing.publications)
            }
        }
    }

    private fun Project.configJreleaser(publishURI: Directory) {
        extensions.configure<JReleaserExtension>("jreleaser") {
            gitRootSearch.set(false)
            release {
                enabled.set(false)
                github { enabled.set(false) }
                gitlab { enabled.set(false) }
                gitea { enabled.set(false) }
            }
            signing {
                active.set(Active.RELEASE)
                armored.set(true)
            }

            deploy {
                maven {
                    mavenCentral {
                        register("sonatype") {
                            active.set(Active.RELEASE)
                            url.set("https://central.sonatype.com/api/v1/publisher")
                            stagingRepository(publishURI.asFile.relativeTo(projectDir).path)
                        }
                    }
                }
            }
        }
    }
}

open class MavenPublishExtension {
    var groupId: String? = null
    var artifactId: String? = null
    var version: String? = null
    var description: String? = null
}
