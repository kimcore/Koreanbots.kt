package com.github.kimcore.koreanbots.javacord

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Strategy
import org.javacord.api.DiscordApi

@Suppress("unused")
class JavacordKoreanbotsClient(
    private val discordApi: DiscordApi, token: String, strategy: Strategy, intervalMinutes: Int
) : KoreanbotsClient(token, strategy, intervalMinutes) {
    private var listener: JavacordListener? = null

    companion object {
        fun KoreanbotsClient.Companion.create(
            discordApi: DiscordApi,
            token: String,
            strategy: Strategy = Strategy.LISTENER,
            intervalMinutes: Int = 10
        ): JavacordKoreanbotsClient {
            return JavacordKoreanbotsClient(discordApi, token, strategy, intervalMinutes)
        }
    }

    init {
        when (strategy) {
            Strategy.LISTENER -> addListener()
            Strategy.LOOP -> startLoop { discordApi.servers.size }
        }
        shutdownChild = { discordApi.removeListener(listener) }
    }

    private fun addListener() {
        listener = JavacordListener(this)
        discordApi.addListener(listener)
        updateServersCount(discordApi.servers.size)
    }
}