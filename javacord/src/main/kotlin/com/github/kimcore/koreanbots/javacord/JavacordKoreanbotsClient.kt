package com.github.kimcore.koreanbots.javacord

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Mode
import org.javacord.api.DiscordApi

@Suppress("unused")
class JavacordKoreanbotsClient(
    private val discordApi: DiscordApi, token: String, mode: Mode, intervalMinutes: Int, useV2: Boolean
) : KoreanbotsClient(token, mode, intervalMinutes, useV2) {
    private var listener: JavacordListener? = null

    companion object {
        fun KoreanbotsClient.Companion.create(
            discordApi: DiscordApi,
            token: String,
            mode: Mode = Mode.LISTENER,
            intervalMinutes: Int = 10,
            useV2: Boolean = false
        ): JavacordKoreanbotsClient {
            return JavacordKoreanbotsClient(discordApi, token, mode, intervalMinutes, useV2)
        }
    }

    init {
        when (mode) {
            Mode.LISTENER -> addListener()
            Mode.LOOP -> startLoop { discordApi.servers.size }
        }
        shutdownChild = { discordApi.removeListener(listener) }
    }

    private fun addListener() {
        listener = JavacordListener(this)
        discordApi.addListener(listener)
        updateServersCount(discordApi.servers.size)
    }
}