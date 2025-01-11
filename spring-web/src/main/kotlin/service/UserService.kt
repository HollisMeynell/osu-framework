package org.spring.web.service

import org.spring.osu.AuthScope
import org.spring.osu.OsuApi
import org.spring.osu.OsuMode
import org.spring.web.AuthUser
import org.spring.web.DataVo
import org.spring.web.Jwt
import org.spring.web.LoginUserDto
import org.spring.web.entity.OsuAuth
import org.spring.web.model.OsuUserVo

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
        val jwt = AuthUser(
            uid = auth.id!!,
            name = auth.name,
            role = role,
        ).token()
        return DataVo(
            data = LoginUserDto(
                uid = auth.id!!,
                name = auth.name,
                token = jwt,
                admin = Jwt.isAdmin(auth.id),
            )
        )
    }

    suspend fun getUserInfo(uid: Long, mode: String? = null): DataVo<OsuUserVo> {
        var osuMode = OsuMode.getMode(mode)
        val user = OsuApi.getUser(user = uid, mode = osuMode)
        osuMode = user.rankHistory?.mode ?: osuMode
        return DataVo(data = OsuUserVo.extendedFromUser(user, osuMode))
    }

    suspend fun getUserInfo(name: String, mode: String? = null): DataVo<OsuUserVo> {
        var osuMode = OsuMode.getMode(mode)
        val user = OsuApi.getUser(name = name, mode = osuMode)
        osuMode = user.rankHistory?.mode ?: osuMode
        return DataVo(data = OsuUserVo.extendedFromUser(user, osuMode))
    }
}