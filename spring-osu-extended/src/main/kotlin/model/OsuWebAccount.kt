package org.spring.osu.extended.model

interface OsuWebAccount {
    var session: String
    var csrf: String?
    var name: String?
    var userID: Long?
    suspend fun update()
}