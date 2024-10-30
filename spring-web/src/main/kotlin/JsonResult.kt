package org.spring.web

data class LoginUserDto (
    val uid:Long,
    val name: String,
    val token: String,
    val admin: Boolean,
)