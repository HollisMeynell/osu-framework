package org.spring.web.model

data class LoginUserVo (
    val uid:Long,
    val name: String,
    val token: String,
    val admin: Boolean,
    val info: OsuUserVo? = null,
)