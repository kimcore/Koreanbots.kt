package com.github.kimcore.koreanbots.catnip

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Strategy
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.shard.DiscordEvent

@Suppress("unused")
class CatnipKoreanbotsClient(
    catnip: Catnip, token: String, strategy: Strategy, intervalMinutes: Int
) : KoreanbotsClient(token, strategy, intervalMinutes) {
    companion object {
        fun KoreanbotsClient.Companion.create(
            catnip: Catnip,
            token: String,
            strategy: Strategy = Strategy.LISTENER,
            intervalMinutes: Int = 10
        ): CatnipKoreanbotsClient {
            return CatnipKoreanbotsClient(catnip, token, strategy, intervalMinutes)
        }
    }

    init {
        when (strategy) {
            Strategy.LISTENER -> addListener(catnip)
            Strategy.LOOP -> startLoop { catnip.cache().guilds().size().toInt() }
        }
    }

    private fun addListener(catnip: Catnip) {
        val handle = fun (guild: Guild) {
            if (shutdown) return

            val servers = guild.catnip().cache().guilds().size().toInt()
            updateServersCount(servers)
        }

        catnip.on(DiscordEvent.READY) {
            updateServersCount(it.guilds().size)
            catnip.on(DiscordEvent.GUILD_CREATE, handle)
            catnip.on(DiscordEvent.GUILD_DELETE, handle)
        }
    }
}