package org.spring.core

import io.ktor.client.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

class HttpClientRateLimiter(
    val client: HttpClient,
) {
    suspend fun request(request: HttpRequestBuilder): HttpResponse {
        val response = CompletableDeferred<HttpResponse>()
        val rateLimitRequest = RateLimitRequest(client, request, response = response)
        tasks.send(rateLimitRequest)
        return response.await()
    }

    suspend fun prepare(request: HttpRequestBuilder): HttpStatement {
        val statement = CompletableDeferred<HttpStatement>()
        val rateLimitRequest = RateLimitRequest(client, request, statement = statement)
        tasks.send(rateLimitRequest)
        return statement.await()
    }

    suspend fun request(act: suspend HttpRequestBuilder.() -> Unit): HttpResponse {
        val response = CompletableDeferred<HttpResponse>()
        val request = HttpRequestBuilder()
        request.act()
        val rateLimitRequest = RateLimitRequest(client, request, response = response)
        tasks.send(rateLimitRequest)
        return response.await()
    }

    suspend fun prepare(act: suspend HttpRequestBuilder.() -> Unit): HttpStatement {
        val statement = CompletableDeferred<HttpStatement>()
        val request = HttpRequestBuilder()
        request.act()
        val rateLimitRequest = RateLimitRequest(client, request, statement = statement)
        tasks.send(rateLimitRequest)
        return statement.await()
    }

    companion object {
        private const val maxRetry = 4
        private const val rateSecond = 30
        private const val rateMinute = 1000
        private const val rateHour = 40000
        private val tick = 0.2.seconds

        private val rateMax = Array(3) { 0 }
        private val rateVelocity = Array(3) { 0 }

        private val token = IntArray(3) { i -> rateMax[i] }

        private val tasks = Channel<RateLimitRequest>(0)
        private val scope = CoroutineScope(Dispatchers.Default)

        init {
            rateMax[0] = rateSecond
            rateMax[1] = rateMinute
            rateMax[2] = rateHour

            rateVelocity[0] = rateSecond / 5
            rateVelocity[1] = rateMinute / 60 / 5
            rateVelocity[2] = rateHour / 3600 / 5

            scope.ticker(tick) {
                repeat(3) { i ->
                    if (token[i] < rateMax[i]) {
                        token[i] = min(rateMax[i], token[i] + rateVelocity[i])
                    }
                }
            }
            scope.launch {
                while (true) {
                    if (checkToken()) {
                        val task = tasks.receive()
                        launch { action(task) }
                    } else {
                        delay(tick)
                    }
                }
            }
        }


        private suspend fun action(task: RateLimitRequest) {
            val (client, request, responseCallback, statementCallback) = task
            try {
                if (responseCallback != null) {
                    val response = client.request(request)
                    if (checkResponse(response)) {
                        task.retry++
                        response.cancel()
                        tasks.send(task)
                        return
                    }
                    responseCallback.complete(response)
                } else if (statementCallback != null) {
                    val statement = client.prepareRequest(request)
                    statementCallback.complete(statement)
                }
            } catch (t: Throwable) {
                if (checkException(t) && task.retry < maxRetry) {
                    task.retry++
                    tasks.send(task)
                    return
                }
                responseCallback?.completeExceptionally(t)
                statementCallback?.completeExceptionally(t)
            }
        }

        private fun checkToken(): Boolean {
            for (i in token.indices) {
                if (token[i] == 0) {
                    for (j in 0 until i) {
                        token[j]++
                    }
                    return false
                }
                token[i]--
            }
            return true
        }

        private fun checkResponse(response: HttpResponse): Boolean {
            return response.status.value == HttpStatusCode.TooManyRequests.value
        }

        private fun checkException(t: Throwable): Boolean {
            return t is HttpRequestTimeoutException || t is ConnectTimeoutException || t is SocketTimeoutException
        }
    }

    data class RateLimitRequest(
        val client: HttpClient,
        val request: HttpRequestBuilder,
        val response: CompletableDeferred<HttpResponse>? = null,
        val statement: CompletableDeferred<HttpStatement>? = null,
        var retry: Int = 0,
    )
}

