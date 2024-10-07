package org.spring.osu

import io.ktor.client.statement.*

class OsuApiNotInitException():Exception("Osu api not init.")

class OsuApiException(
    response: HttpResponse,
    var info:String,
    var code:Int = response.status.value,
    var status:String = response.status.description,
    var api:String = response.request.url.toString(),
) : Exception(info)