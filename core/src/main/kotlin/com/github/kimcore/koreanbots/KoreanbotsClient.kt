package com.github.kimcore.koreanbots

import com.apollographql.apollo.ApolloClient
import com.github.kimcore.koreanbots.entities.internal.Mode
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.*
import javax.management.InstanceAlreadyExistsException
import kotlin.UnsupportedOperationException

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class KoreanbotsClient(
    val token: String,
    val mode: Mode = Mode.LISTENER,
    val intervalMinutes: Int = 10,
    val useV2: Boolean = false
) {
    private val log = LoggerFactory.getLogger("KoreanbotsClient")
    private lateinit var serversProvider: suspend () -> Int
    private val endpoint = if (useV2) "https://api.beta.koreanbots.dev/v2" else "https://api.koreanbots.dev"
    private val apolloClient = ApolloClient.builder()
        .serverUrl("$endpoint/graphql/endpoint")
        .build()
    private val threadPool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var runningFuture: ScheduledFuture<*>? = null
    protected var shutdownChild: () -> Unit = {}
    protected var shutdown = false
    val isShutdown: Boolean
        get() = shutdown

    companion object

    fun updateServersCount(servers: Int) = GlobalScope.launch {
        if (useV2) updateServersCountV2(servers)
        else updateServersCountV1(servers)
    }

    private suspend fun updateServersCountV1(servers: Int) {
        val body = JsonObject()
        body.addProperty("servers", servers)
        val response = "$endpoint/bots/servers".httpPost()
            .body(body.toString())
            .set("Content-Type", "application/json")
            .set("token", token)
            .awaitStringResponse()
            .second
        when (response.statusCode) {
            200 -> log.debug("Successfully updated servers count to $servers")
            429 -> log.debug("Encountered rate limit while updating servers count, You should consider changing update interval or using another mode.")
            else -> {
                val json = Gson().fromJson(response.data.decodeToString(), JsonObject::class.java)
                val message = json["message"].asString
                log.debug("Received unknown error from koreanbots: $message")
            }
        }
    }

    private suspend fun updateServersCountV2(servers: Int) {
//        apolloClient.mutate(UpdateServersMutation)
    }

    suspend fun getBot(botId: String) {

    }

    fun stopLoop() {
        runningFuture?.cancel(true)?.run { log.debug("Stopped loop thread") }
        runningFuture = null
    }

    fun startLoop() = startLoop(serversProvider)

    protected fun startLoop(serversProvider: suspend () -> Int) {
        if (runningFuture != null) {
            throw InstanceAlreadyExistsException("이미 실행 중입니다.")
        }

        if (shutdown) {
            throw UnsupportedOperationException("종료된 클라이언트를 다시 사용할 수 없습니다!")
        }

        if (mode != Mode.LOOP) {
            throw UnsupportedOperationException("LOOP 모드에서만 루프를 사용할 수 있습니다!")
        }

        this.serversProvider = serversProvider

        runningFuture = threadPool.scheduleAtFixedRate({
            val servers = runBlocking { serversProvider() }
            updateServersCount(servers)
        }, 0L, intervalMinutes.toLong(), TimeUnit.MINUTES)

        log.debug("Started loop thread")
    }

    fun shutdown() {
        stopLoop()
        shutdownChild()
        shutdown = true
    }
}