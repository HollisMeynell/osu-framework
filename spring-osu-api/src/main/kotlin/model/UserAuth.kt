package org.spring.osu.model

interface UserAuth {
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

    /**
     * for other client auth
     */
    fun getClient(): Client? = null

    data class Client(
        val id: Long,
        val secret: String,
        val redirectUrl: String,
    )
}

