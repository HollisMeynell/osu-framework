package org.spring.osu.module

interface UserAuth {
    var osuID: Long?
    var osuName: String?
    var refreshToken: String?
    var accessToken: String?
    var expires: Long

    suspend fun update()

    fun isExpired() = System.currentTimeMillis() > expires

    fun isAuthed(): Boolean = accessToken != null

    fun refresh(time: Long, access: String, refresh: String? = null) {
        accessToken = access
        if (refresh != null) refreshToken = refresh
        expires = System.currentTimeMillis() + time * 1000
    }
}

