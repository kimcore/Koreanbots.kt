package com.github.kimcore.koreanbots

import com.apollographql.apollo.ApolloClient
import com.github.kimcore.koreanbots.entities.internal.Strategy
import com.github.kittinunf.fuel.httpPost
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.*
import javax.management.InstanceAlreadyExistsException
import kotlin.UnsupportedOperationException

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class KoreanbotsClient(
    val token: String,
    val strategy: Strategy = Strategy.LISTENER,
    val intervalMinutes: Int = 10
) {
    private lateinit var serversProvider: suspend () -> Int
    private val endpoint = "https://api.beta.koreanbots.dev/v2"
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
        println("updated: $servers")
        "$endpoint/bots/servers".httpPost()
    }

    fun stopLoop() {
        runningFuture?.cancel(true)
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

        if (strategy != Strategy.LOOP) {
            throw UnsupportedOperationException("Strategy.LOOP 모드에서만 루프를 사용할 수 있습니다!")
        }

        this.serversProvider = serversProvider

        runningFuture = threadPool.scheduleAtFixedRate({
            val servers = runBlocking { serversProvider() }
            updateServersCount(servers)
        }, 0L, intervalMinutes.toLong(), TimeUnit.MINUTES)
    }

    fun shutdown() {
        stopLoop()
        shutdownChild()
        shutdown = true
    }
}