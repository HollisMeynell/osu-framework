package org.spring.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.Principal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

object Jwt {
    lateinit var secret: String
    fun isAdmin(id: Long?): Boolean {
        return false
    }
}

fun Application.setupJwt(secret: String) {
    Jwt.secret = secret
    authentication {
        jwt {
            verifier(JWT.require(Algorithm.HMAC256(secret)).build())
            validate {
                val user = with(it.payload) {
                    val uid = getClaim("uid").asLong()
                    val name = getClaim("name").asString()
                    val role = getClaim("role").asString()
                    JwtUser(uid, name, role)
                }
                return@validate user
            }
            challenge { defaultScheme, realm ->
                call.respond(DataVo(401, "Unauthorized", null))
            }
        }
    }
}

data class JwtUser(
    var uid: Long = 0,
    var name: String = "",
    var role: String = "",
) : Principal {
    fun token(secret: String) = JWT.create()
        .withClaim("uid", uid)
        .withClaim("name", name)
        .withClaim("role", role)
        .sign(Algorithm.HMAC256(secret))
}

