package org.spring.web

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.addResourceSource
import org.spring.core.ProxyConfig
import org.spring.osu.OsuApiConfig
import org.spring.osu.beatmap.mirror.OsuBeatmapMirrorConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

data class WebConfig(
    var osu: OsuApiConfig,
    var database: DatabaseConfig,
    var mirror: OsuBeatmapMirrorConfig,
    var proxy: ProxyConfig? = null,
    var server: ServerConfig,
) {
    init {
        if (proxy != null && osu.proxy == null) {
            osu.proxy = proxy
        }
    }

    fun selfUrl(): String {
        return if (server.ssl) "https://${server.localUrl}" else "http://${server.localUrl}"
    }

    data class ServerConfig(
        var port: Int = 8080,
        var secret: String = "*",
        val localUrl: String = "localhost$port",
        val ssl: Boolean = false,
        var cors: List<String>? = null,
        var adminUsers: Set<Long> = emptySet(),
        var botToken: String = "",
    )

    data class DatabaseConfig(
        val driver: String = "org.postgresql.Driver",
        val url: String,
        val username: String,
        val password: String
    )

    companion object {
        lateinit var Instance: WebConfig
            private set

        fun loadFromFile(): WebConfig {
            val local = System.getProperty("user.dir")
            val builder = ConfigLoaderBuilder.default()
            Path(local, "config", "config.toml").load(builder)
            Path(local, "config.toml").load(builder)
            builder.addResourceSource("/config.toml", true)
            return builder.build().loadConfigOrThrow<WebConfig>().also { Instance = it }
        }

        private fun Path.load(builder: ConfigLoaderBuilder) {
            this.takeIf {
                Files.isRegularFile(this)
            }?.let {
                builder.addSource(PropertySource.path(it))
            }
        }
    }
}
