package com.github.kimcore.koreanbots.kord

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Mode
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
    mode: Mode,
    intervalMinutes: Int,
    useV2: Boolean
) : KoreanbotsClient(token, mode, intervalMinutes, useV2) {
    companion object {
        fun KoreanbotsClient.Companion.create(
            kord: Kord,
            token: String,
            mode: Mode = Mode.LISTENER,
            intervalMinutes: Int = 10,
            useV2: Boolean = false
        ): KordKoreanbotsClient {
            return KordKoreanbotsClient(kord, token, mode, intervalMinutes, useV2)
        }
    }

    init {
        when (mode) {
            Mode.LISTENER -> addListener(kord)
            Mode.LOOP -> startLoop { kord.guilds.count() }
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