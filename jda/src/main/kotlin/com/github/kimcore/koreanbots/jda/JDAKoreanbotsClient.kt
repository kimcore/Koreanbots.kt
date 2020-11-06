package com.github.kimcore.koreanbots.jda

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Mode
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.sharding.ShardManager

@Suppress("unused")
class JDAKoreanbotsClient private constructor(
    serversProvider: suspend () -> Int,
    addListener: (client: KoreanbotsClient) -> JDAListener,
    private val removeListener: (listener: JDAListener?) -> Unit,
    token: String,
    mode: Mode,
    intervalMinutes: Int
) : KoreanbotsClient(token, mode, intervalMinutes) {
    private var listener: JDAListener? = null

    companion object {
        fun KoreanbotsClient.Companion.create(
            jda: JDA,
            token: String,
            mode: Mode = Mode.LISTENER,
            intervalMinutes: Int = 10
        ): JDAKoreanbotsClient {
            return JDAKoreanbotsClient(
                { jda.shardManager?.guilds?.size ?: jda.guilds.size },
                {
                    val listener = JDAListener(it)
                    jda.addEventListener(listener)
                    listener
                },
                { jda.removeEventListener(it) },
                token,
                mode,
                intervalMinutes
            )
        }

        fun KoreanbotsClient.Companion.create(
            shardManager: ShardManager,
            token: String,
            mode: Mode = Mode.LISTENER,
            intervalMinutes: Int = 10
        ): JDAKoreanbotsClient {
            return JDAKoreanbotsClient(
                { shardManager.guilds.size },
                {
                    val listener = JDAListener(it)
                    shardManager.addEventListener(listener)
                    listener
                },
                { shardManager.removeEventListener(it) },
                token,
                mode,
                intervalMinutes
            )
        }
    }

    init {
        when (mode) {
            Mode.LISTENER -> listener = addListener(this)
            Mode.LOOP -> startLoop(serversProvider)
            Mode.NONE -> {
            }
        }
        shutdownChild = { removeListener(listener) }
    }
}