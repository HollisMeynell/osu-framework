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

    suspend fun login(code: String): LoginUserDto {
        val auth = OsuAuth(refreshToken = code)
        OsuApi.refreshUserAuth(auth)
        val jwt = JwtUser(
            uid = auth.id!!,
            name = auth.name,
            role = "user",
        ).token(Jwt.secret)
        return LoginUserDto(
            uid = auth.id!!,
            name = auth.name,
            token = jwt,
            admin = Jwt.isAdmin(auth.id),
        )
    }

}