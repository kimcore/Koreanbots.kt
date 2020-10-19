package com.github.kimcore.koreanbots

import com.github.kimcore.koreanbots.entities.internal.Strategy
import com.github.kimcore.koreanbots.listeners.JDAListener
import com.github.kimcore.koreanbots.listeners.JavacordListener
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.GuildDeleteEvent
import com.gitlab.kordlib.core.event.gateway.ReadyEvent as KordReadyEvent
import com.gitlab.kordlib.core.on
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.guild.GuildEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.guild.GuildCreateEvent as Discord4JGuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent as Discord4JGuildDeleteEvent
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.sharding.ShardManager
import org.javacord.api.DiscordApi
import reactor.core.publisher.Mono
import java.lang.UnsupportedOperationException
import java.util.concurrent.*
import javax.management.InstanceAlreadyExistsException

@Suppress("MemberVisibilityCanBePrivate", "unused")
class KoreanbotsClient {
    val token: String
    val strategy: Strategy
    private val threadPool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var runningFuture: ScheduledFuture<*>? = null

    constructor(jda: JDA, token: String, strategy: Strategy = Strategy.LISTENER, intervalSeconds: Long = 600L) {
        this.token = token
        this.strategy = strategy
        when (strategy) {
            Strategy.LISTENER -> addJDAListener(jda)
            Strategy.LOOP -> loop({ jda.guilds.size }, intervalSeconds)
        }
    }

    constructor(
        shardManager: ShardManager,
        token: String,
        strategy: Strategy = Strategy.LISTENER,
        intervalSeconds: Long = 600L
    ) {
        this.token = token
        this.strategy = strategy
        when (strategy) {
            Strategy.LISTENER -> addJDAShardManagerListener(shardManager)
            Strategy.LOOP -> loop({ shardManager.guilds.size }, intervalSeconds)
        }
    }

    constructor(kord: Kord, token: String, strategy: Strategy = Strategy.LISTENER, intervalSeconds: Long = 600L) {
        this.token = token
        this.strategy = strategy
        when (strategy) {
            Strategy.LISTENER -> addKordListener(kord)
            Strategy.LOOP -> loop({ kord.guilds.count() }, intervalSeconds)
        }
    }

    constructor(
        discordApi: DiscordApi,
        token: String,
        strategy: Strategy = Strategy.LISTENER,
        intervalSeconds: Long = 600L
    ) {
        this.token = token
        this.strategy = strategy
        when (strategy) {
            Strategy.LISTENER -> addJavacordListener(discordApi)
            Strategy.LOOP -> loop({ discordApi.servers.size }, intervalSeconds)
        }
    }

    constructor(
        gatewayDiscordClient: GatewayDiscordClient,
        token: String,
        strategy: Strategy = Strategy.LISTENER,
        intervalSeconds: Long = 600L
    ) {
        this.token = token
        this.strategy = strategy
        when (strategy) {
            Strategy.LISTENER -> addDiscord4JListener(gatewayDiscordClient)
            Strategy.LOOP -> loop({ gatewayDiscordClient.guilds.count().await() }, intervalSeconds)
        }
    }

    internal fun updateServersCount(servers: Int) {
        println(servers) // TODO()
    }

    fun shutdown() {
        runningFuture?.cancel(true)
    }

    fun loop(serversProvider: suspend () -> Int, intervalSeconds: Long) {
        if (runningFuture != null) {
            throw InstanceAlreadyExistsException("이미 실행 중입니다.")
        }

        if (strategy != Strategy.LOOP) {
            throw UnsupportedOperationException("Strategy.LOOP 모드에서만 루프를 사용할 수 있습니다!")
        }

        runningFuture = threadPool.scheduleAtFixedRate({
            while (true) {
                val servers = runBlocking { serversProvider() }
                updateServersCount(servers)
            }
        }, 0L, intervalSeconds, TimeUnit.SECONDS)
    }

    private fun addJDAListener(jda: JDA) {
        val listener = JDAListener(this)
        jda.addEventListener(listener)
    }

    private fun addJDAShardManagerListener(shardManager: ShardManager) {
        val listener = JDAListener(this)
        shardManager.addEventListener(listener)
    }

    private fun addKordListener(kord: Kord) {
        var initialServers = 0
        var updatable = false

        fun handle(servers: Int) {
            if (!updatable && servers == initialServers) {
                updatable = true
                return
            }
            if (!updatable) return

            updateServersCount(servers)
        }

        kord.on<GuildCreateEvent> {
            handle(this.kord.guilds.count())
        }

        kord.on<GuildDeleteEvent> {
            handle(this.kord.guilds.count())
        }

        kord.on<KordReadyEvent> {
            initialServers = this.guildIds.size
            updateServersCount(initialServers)
        }
    }

    private fun addJavacordListener(discordApi: DiscordApi) {
        val listener = JavacordListener(this@KoreanbotsClient)
        discordApi.addListener(listener)

        updateServersCount(discordApi.servers.size)
    }

    private fun addDiscord4JListener(gatewayDiscordClient: GatewayDiscordClient) {
        var initialServers = 0
        var updatable = false

        fun handle(event: GuildEvent) {
            val servers = event.client.guilds.count().block()!!.toInt()

            if (!updatable && servers == initialServers) {
                updatable = true
                return
            }
            if (!updatable) return

            updateServersCount(servers)
        }

        gatewayDiscordClient.on(ReadyEvent::class.java).subscribe {
            initialServers = it.guilds.size
            updateServersCount(initialServers)
            gatewayDiscordClient.on(Discord4JGuildCreateEvent::class.java).subscribe { event -> handle(event) }
            gatewayDiscordClient.on(Discord4JGuildDeleteEvent::class.java).subscribe { event -> handle(event) }
        }
    }

    private suspend fun Mono<Long>.await(): Int {
        val future = CompletableFuture<Int>()
        this.subscribe {
            future.complete(it.toInt())
        }
        return future.await()
    }
}