package com.github.kimcore.koreanbots

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloHttpException
import com.github.kimcore.koreanbots.entities.Bot
import com.github.kimcore.koreanbots.entities.List
import com.github.kimcore.koreanbots.entities.SubmittedBot
import com.github.kimcore.koreanbots.type.ListType as GraphQLListType
import com.github.kimcore.koreanbots.entities.enums.ListType
import com.github.kimcore.koreanbots.entities.internal.Mode
import com.github.kimcore.koreanbots.entities.internal.exceptions.ClientNotReusableException
import com.github.kimcore.koreanbots.entities.internal.exceptions.LoopAlreadyRunningException
import com.github.kimcore.koreanbots.entities.internal.exceptions.ModeNotAllowedException
import com.github.kimcore.koreanbots.utils.JWT
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.concurrent.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class KoreanbotsClient(
    val token: String,
    val mode: Mode = Mode.LISTENER,
    val intervalMinutes: Int = 10
) {
    private val log = LoggerFactory.getLogger("KoreanbotsClient")
    private lateinit var serversProvider: suspend () -> Int
    private val endpoint = "https://api.beta.koreanbots.dev/v2"
    private val apolloClient = ApolloClient.builder()
        .serverUrl("$endpoint/graphql/endpoint")
        .callFactory {
            OkHttpClient().newCall(
                it.newBuilder().addHeader("Authorization", "Bearer $token").build()
            )
        }
        .build()
    private val threadPool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var runningFuture: ScheduledFuture<*>? = null
    protected var shutdownChild: () -> Unit = {}
    protected var shutdown = false
    val selfId: String = JWT.decodeID(token)
    val isShutdown: Boolean
        get() = shutdown

    companion object

    fun updateServers(servers: Int) = GlobalScope.launch {
        try {
            apolloClient.mutate(
                UpdateServersMutation(selfId.toInput(), servers.toInput())
            ).await()
        } catch (e: ApolloHttpException) {
            when (e.code()) {
                409 -> log.warn("Encountered rate limit while updating servers count, You should consider changing update interval or using another mode.")
                else -> log.error("Received unknown error from koreanbots: ${e.message()}")
            }
        }
    }

    suspend fun getBot(botId: String): Bot? {
        val response = apolloClient.query(BotQuery(botId)).await()
        if (response.data == null) return null
        val gson = Gson()
        return gson.fromJson(gson.toJson(response.data!!.bot), Bot::class.java)
    }

//    suspend fun getSubmittedBot(id: String, date: Int): SubmittedBot? {
//        val response = apolloClient.query(
//            ListQuery(GraphQLListType.valueOf(listType.name), page.toInput(), query.toInput())
//        ).await()
//        if (response.data == null) return null
//        val gson = Gson()
//        return gson.fromJson(gson.toJson(response.data!!.list), List::class.java)
//    }

    suspend fun getList(listType: ListType, page: Int, query: String): List? {
        val response = apolloClient.query(
            ListQuery(GraphQLListType.valueOf(listType.name), page.toInput(), query.toInput())
        ).await()
        if (response.data == null) return null
        val gson = Gson()
        return gson.fromJson(gson.toJson(response.data!!.list), List::class.java)
    }

    fun stopLoop() {
        runningFuture?.cancel(true)?.run { log.debug("Stopped loop thread") }
        runningFuture = null
    }

    fun startLoop() = startLoop(serversProvider)

    protected fun startLoop(serversProvider: suspend () -> Int) {
        if (runningFuture != null) throw LoopAlreadyRunningException()
        if (shutdown) throw ClientNotReusableException()
        if (mode != Mode.LOOP) throw ModeNotAllowedException()

        this.serversProvider = serversProvider

        runningFuture = threadPool.scheduleAtFixedRate({
            val servers = runBlocking { serversProvider() }
            updateServers(servers)
        }, 0L, intervalMinutes.toLong(), TimeUnit.MINUTES)

        log.debug("Started loop thread")
    }

    fun shutdown() {
        stopLoop()
        shutdownChild()
        shutdown = true
    }
}