package com.github.kimcore.koreanbots.kord

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Strategy
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.guild.GuildDeleteEvent
import com.gitlab.kordlib.core.on
import kotlinx.coroutines.flow.count

@Suppress("unused")
class KordKoreanbotsClient(
    kord: Kord,
    token: String,
    strategy: Strategy,
    intervalMinutes: Int
) : KoreanbotsClient(token, strategy, intervalMinutes) {
    companion object {
        fun KoreanbotsClient.Companion.create(
            kord: Kord,
            token: String,
            strategy: Strategy = Strategy.LISTENER,
            intervalMinutes: Int = 10
        ): KordKoreanbotsClient {
            return KordKoreanbotsClient(kord, token, strategy, intervalMinutes)
        }
    }

    init {
        when (strategy) {
            Strategy.LISTENER -> addListener(kord)
            Strategy.LOOP -> startLoop { kord.guilds.count() }
        }
    }

    private fun addListener(kord: Kord) {
        var initialServers = 0
        var updatable = false

        fun handle(servers: Int) {
            if (shutdown) return

            if (servers == initialServers) {
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

        kord.on<ReadyEvent> {
            initialServers = this.guildIds.size
            updateServersCount(initialServers)
        }
    }
}