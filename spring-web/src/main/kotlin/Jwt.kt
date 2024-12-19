package org.spring.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

object Jwt {
    val secret: String by lazy {
        WebConfig.Instance.server.secret
    }

    val botToken by lazy {
        WebConfig.Instance.server.botToken
    }

    fun isAdmin(id: Long?): Boolean {
        return WebConfig.Instance.server.adminUsers.contains(id)
    }

    val algorithm by lazy {
        Algorithm.HMAC256(secret)
    }
}

fun Application.setupJwt() {
    authentication {
        bearer("bot") {
            authenticate {
                return@authenticate if (Jwt.botToken.isEmpty()) {
                    null
                } else if (it.token == Jwt.botToken) {
                    AuthUser(role = AuthUser.Role.Bot)
                } else {
                    null
                }
            }
        }
        jwt {
            verifier(JWT.require(Jwt.algorithm).build())
            validate {
                val user = with(it.payload) {
                    val uid = getClaim("uid").asLong()
                    val name = getClaim("name").asString()
                    val role = getClaim("role").asString()
                    AuthUser(uid, name, role)
                }
                return@validate user
            }
            challenge { _, _ ->
                call.respond(DataVo(401, "Unauthorized", null))
            }
        }
    }
}

data class AuthUser(
    var uid: Long = 0,
    var name: String = "",
    var role: Role = Role.Unknown,
) {
    constructor(
        uid: Long = 0,
        name: String = "",
        role: String = "",
    ) : this(
        uid, name, Role.fromString(role)
    )

    enum class Role {
        Unknown, User, Admin, Bot;

        override fun toString() = when (this) {
            Unknown -> ""
            User -> "user"
            Admin -> "admin"
            Bot -> "bot"
        }

        companion object {
            fun fromString(string: String) =
                when (string) {
                    "user" -> User
                    "admin" -> Admin
                    "bot" -> Bot
                    else -> Unknown
                }

        }
    }

    fun token(): String = JWT.create()
        .withClaim("uid", uid)
        .withClaim("name", name)
        .withClaim("role", role.toString())
        .sign(Jwt.algorithm)

    fun isAdmin() = role == Role.Admin

}