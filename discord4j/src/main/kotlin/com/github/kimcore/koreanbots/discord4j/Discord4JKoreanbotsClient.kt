package com.github.kimcore.koreanbots.discord4j

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Strategy
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.event.domain.guild.GuildEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import kotlinx.coroutines.future.await
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

@Suppress("unused")
class Discord4JKoreanbotsClient(
    gatewayDiscordClient: GatewayDiscordClient, token: String, strategy: Strategy, intervalMinutes: Int
) : KoreanbotsClient(token, strategy, intervalMinutes) {
    companion object {
        fun KoreanbotsClient.Companion.create(
            gatewayDiscordClient: GatewayDiscordClient,
            token: String,
            strategy: Strategy = Strategy.LISTENER,
            intervalMinutes: Int = 10
        ): Discord4JKoreanbotsClient {
            return Discord4JKoreanbotsClient(gatewayDiscordClient, token, strategy, intervalMinutes)
        }
    }

    init {
        when (strategy) {
            Strategy.LISTENER -> addListener(gatewayDiscordClient)
            Strategy.LOOP -> startLoop { gatewayDiscordClient.guilds.count().await() }
        }
    }

    private fun addListener(gatewayDiscordClient: GatewayDiscordClient) {
        var initialServers = 0
        var updatable = false

        fun handle(event: GuildEvent) {
            if (shutdown) return

            val servers = event.client.guilds.count().block()!!.toInt()

            if (servers == initialServers) {
                updatable = true
                return
            }
            if (!updatable) return

            updateServersCount(servers)
        }

        gatewayDiscordClient.on(ReadyEvent::class.java).subscribe {
            initialServers = it.guilds.size
            updateServersCount(initialServers)
            gatewayDiscordClient.on(GuildCreateEvent::class.java).subscribe { event -> handle(event) }
            gatewayDiscordClient.on(GuildDeleteEvent::class.java).subscribe { event -> handle(event) }
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