package org.spring.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.time.Instant
import java.util.*

object Jwt {
    private val secret: String
        get() {
            return WebConfig.Instance.server.secret
        }

    val botToken by lazy {
        WebConfig.Instance.server.botToken
    }

    fun isAdmin(id: Long?): Boolean {
        return WebConfig.Instance.server.adminUsers.contains(id)
    }

    val algorithm: Algorithm by lazy {
        Algorithm.HMAC256(secret)
    }

    val userVerifier: JWTVerifier by lazy {
        JWT.require(algorithm).build()
    }

    val botVerifier: JWTVerifier by lazy {
        object : JWTVerifier {
            override fun verify(s: String): DecodedJWT {
                if (s == botToken) {
                    return botDecodedJWT
                }
                throw TokenExpiredException("Token expired", Instant.now())
            }

            override fun verify(d: DecodedJWT): DecodedJWT {
                if (d.token == botToken) {
                    return d
                }
                throw TokenExpiredException("Token expired", Instant.now())
            }
        }
    }

    val guestVerifier: JWTVerifier by lazy {
        object : JWTVerifier {
            override fun verify(s: String): DecodedJWT {
                return guestDecodedJWT
            }

            override fun verify(d: DecodedJWT): DecodedJWT {
                return guestDecodedJWT
            }
        }
    }

    fun validate (data: JWTCredential): AuthUser {
        val user = with(data.payload) {
            val uid = getClaim("uid").asLong()
            val name = getClaim("name").asString()
            val role = getClaim("role").asString()
            AuthUser(uid, name, role)
        }
        return user
    }

    val AuthenticationCheck = createRouteScopedPlugin("AuthenticationCheck") {
        pluginConfig.apply {
            on(AuthenticationChecked) { call ->
                val user = call.getAuthUserNullable()
                if (user != null) return@on

            }
        }
        onCallReceive { call ->
            call.request
        }
    }
}

fun Application.setupJwt() {
    install(Jwt.AuthenticationCheck)
    authentication {
        /*
        jwt {
            authHeader { call ->
                val header = call.request.headers["Authorization"]
                if (header.isNullOrBlank()) {
                    return@authHeader HttpAuthHeader.Single("NoAuth", "")
                }
                if (header.startsWith("Bearer ")) {
                    return@authHeader HttpAuthHeader.Single("Bearer", header.removePrefix("Bearer "))
                } else {
                    return@authHeader HttpAuthHeader.Single("Bot", header.removePrefix("Bot "))
                }
            }
            verifier { header ->
                val scheme = header.authScheme
                return@verifier when (scheme) {
                    "Bearer" -> Jwt.userVerifier
                    "Bot" -> Jwt.botVerifier
                    else -> Jwt.guestVerifier
                }
            }

            validate {
                println(it.payload.toString())
                return@validate AuthUser(role = AuthUser.Role.NoLogin)
            }
            challenge { _, _ ->
                call.respond(DataVo(401, "Unauthorized", null))
            }
        }
        bearer("bot") {
            authenticate {
                if (Jwt.botToken.isEmpty() || it.token != Jwt.botToken) {
                    return@authenticate null
                } else {
                    return@authenticate AuthUser(role = AuthUser.Role.Bot)
                }
            }
        }
         */
        jwt {
            verifier(Jwt.userVerifier)
            validate {
                Jwt.validate(it)
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
    var role: Role = Role.NoLogin,
) {
    constructor(
        uid: Long = 0,
        name: String = "",
        role: String = "",
    ) : this(
        uid, name, Role.fromString(role)
    )

    enum class Role {
        NoLogin, User, Admin, Bot;

        override fun toString() = when (this) {
            NoLogin -> ""
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
                    else -> NoLogin
                }

        }
    }

    fun token(): String = JWT.create()
        .withClaim("uid", uid)
        .withClaim("name", name)
        .withClaim("role", role.toString())
        .sign(Jwt.algorithm)

    fun isAdmin() = role == Role.Admin
    fun isLogin() = role != Role.NoLogin
}

val botDecodedJWT = SelfDecode { s ->
    when (s) {
        "uid" -> SelfClaim(id = 0)
        "name" -> SelfClaim(text = "bot")
        "role" -> SelfClaim(text = "bot")
        else -> SelfClaim()
    }
}

val guestDecodedJWT = SelfDecode { s ->
    when (s) {
        "uid" -> SelfClaim(id = 0)
        "name" -> SelfClaim(text = "unknown")
        "role" -> SelfClaim(text = "")
        else -> SelfClaim()
    }
}

class SelfDecode(
    private val claim: (String) -> Claim
) : DecodedJWT {
    override fun getIssuer() = ""
    override fun getSubject() = ""
    override fun getAudience() = mutableListOf<String>()
    override fun getExpiresAt(): Date = Date.from(Instant.MAX)
    override fun getNotBefore(): Date = Date.from(Instant.MIN)
    override fun getIssuedAt() = Date()
    override fun getId() = ""
    override fun getClaim(p0: String): Claim = claim(p0)
    override fun getClaims() = mutableMapOf<String, Claim>()
    override fun getAlgorithm() = ""
    override fun getType() = ""
    override fun getContentType() = ""
    override fun getKeyId() = ""
    override fun getHeaderClaim(p0: String): Claim = claim(p0)
    override fun getToken() = ""
    override fun getHeader() = ""
    override fun getPayload() = ""
    override fun getSignature() = ""
}

class SelfClaim(
    private val id: Long? = null,
    private val text: String? = null,
) : Claim {
    override fun isNull() = id == null && text == null
    override fun isMissing() = true
    override fun asBoolean() = false
    override fun asInt() = id?.toInt() ?: 0
    override fun asLong() = id ?: 0
    override fun asDouble() = 0.0
    override fun asString() = text ?: ""
    override fun asDate(): Date? = null
    override fun <T : Any?> asArray(p0: Class<T>?): Array<T> {
        throw JWTDecodeException("Not support")
    }

    override fun <T : Any?> asList(p0: Class<T>?): MutableList<T> {
        throw JWTDecodeException("Not support")
    }

    override fun asMap(): MutableMap<String, Any> {
        throw JWTDecodeException("Not support")
    }

    override fun <T : Any?> `as`(p0: Class<T>?): T {
        throw JWTDecodeException("Not support")
    }
}