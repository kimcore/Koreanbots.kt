package com.github.kimcore.koreanbots.javacord

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Mode
import org.javacord.api.DiscordApi

@Suppress("unused")
class JavacordKoreanbotsClient(
    private val discordApi: DiscordApi, token: String, mode: Mode, intervalMinutes: Int
) : KoreanbotsClient(token, mode, intervalMinutes) {
    private var listener: JavacordListener? = null

    companion object {
        fun KoreanbotsClient.Companion.create(
            discordApi: DiscordApi,
            token: String,
            mode: Mode = Mode.LISTENER,
            intervalMinutes: Int = 10
        ): JavacordKoreanbotsClient {
            return JavacordKoreanbotsClient(discordApi, token, mode, intervalMinutes)
        }
    }

    init {
        when (mode) {
            Mode.LISTENER -> addListener()
            Mode.LOOP -> startLoop { discordApi.servers.size }
            Mode.NONE -> {
            }
        }
        shutdownChild = { discordApi.removeListener(listener) }
    }

    private fun addListener() {
        listener = JavacordListener(this)
        discordApi.addListener(listener)
        updateServers(discordApi.servers.size)
    }
}