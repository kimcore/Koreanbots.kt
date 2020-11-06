package com.github.kimcore.koreanbots.catnip

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Mode
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.shard.DiscordEvent

@Suppress("unused")
class CatnipKoreanbotsClient(
    catnip: Catnip, token: String, mode: Mode, intervalMinutes: Int
) : KoreanbotsClient(token, mode, intervalMinutes) {
    companion object {
        fun KoreanbotsClient.Companion.create(
            catnip: Catnip,
            token: String,
            mode: Mode = Mode.LISTENER,
            intervalMinutes: Int = 10
        ): CatnipKoreanbotsClient {
            return CatnipKoreanbotsClient(catnip, token, mode, intervalMinutes)
        }
    }

    init {
        when (mode) {
            Mode.LISTENER -> addListener(catnip)
            Mode.LOOP -> startLoop { catnip.cache().guilds().size().toInt() }
            Mode.NONE -> {
            }
        }
    }

    private fun addListener(catnip: Catnip) {
        val handle = fun(guild: Guild) {
            if (shutdown) return

            val servers = guild.catnip().cache().guilds().size().toInt()
            updateServers(servers)
        }

        catnip.on(DiscordEvent.READY) {
            updateServers(it.guilds().size)
            catnip.on(DiscordEvent.GUILD_CREATE, handle)
            catnip.on(DiscordEvent.GUILD_DELETE, handle)
        }
    }
}