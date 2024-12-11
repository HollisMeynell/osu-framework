package org.spring.web.service

import org.spring.osu.AuthScope
import org.spring.osu.OsuApi
import org.spring.web.DataVo
import org.spring.web.Jwt
import org.spring.web.JwtUser
import org.spring.web.LoginUserDto
import org.spring.web.databases.OsuAuth

object UserService {
    fun oauthUrl() = DataVo(
        data = OsuApi.getOauthUrl(
            "state",
            AuthScope.Identify,
            AuthScope.Public,
            AuthScope.FriendsRead,
        )
    )

    suspend fun login(code: String): DataVo<LoginUserDto> {
        val auth = OsuAuth(refreshToken = code)
        OsuApi.refreshUserAuth(auth)
        val role = if (Jwt.isAdmin(auth.id)) "admin" else "user"
        val jwt = JwtUser(
            uid = auth.id!!,
            name = auth.name,
            role = role,
        ).token(Jwt.secret)
        return DataVo(
            data = LoginUserDto(
                uid = auth.id!!,
                name = auth.name,
                token = jwt,
                admin = Jwt.isAdmin(auth.id),
            )
        )
    }

}