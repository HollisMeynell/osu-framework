package org.spring.web

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.addResourceSource
import org.spring.core.ProxyConfig
import org.spring.osu.OsuApiConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

data class WebConfig(
    var osu: OsuApiConfig,
    var database: DatabaseConfig,
    var proxy: ProxyConfig? = null,
    var server: ServerConfig,
) {
    init {
        if (proxy != null && osu.proxy == null) {
            osu.proxy = proxy
        }
    }

    data class ServerConfig(
        var port: Int = 8080,
        var secret: String = "*",
        var cros: List<String>? = null,
    )

    data class DatabaseConfig(
        val driver: String = "org.postgresql.Driver",
        val url: String,
        val username: String,
        val password: String
    )

    companion object {
        fun loadFromFile(): WebConfig {
            val local = System.getProperty("user.dir")
            val builder = ConfigLoaderBuilder.default()
            Path(local, "config", "config.toml").load(builder)
            Path(local, "config.toml").load(builder)
            builder.addResourceSource("/config.toml", true)

            return builder.build().loadConfigOrThrow<WebConfig>()
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
