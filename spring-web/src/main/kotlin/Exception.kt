package org.spring.web

open class HttpTipsException(
    var code: Int = 400,
    message: String = "Bad Request",
    val cuse: Throwable? = null,
): RuntimeException(message, cuse)

class PermissionException(): HttpTipsException(401, "Unauthorized")
class NotFoundException(): HttpTipsException(404, "Not Found")