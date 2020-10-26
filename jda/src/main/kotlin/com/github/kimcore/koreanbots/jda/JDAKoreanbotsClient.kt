package com.github.kimcore.koreanbots.jda

import com.github.kimcore.koreanbots.KoreanbotsClient
import com.github.kimcore.koreanbots.entities.internal.Strategy
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.sharding.ShardManager

@Suppress("unused")
class JDAKoreanbotsClient private constructor(
    serversProvider: suspend () -> Int,
    addListener: (client: KoreanbotsClient) -> JDAListener,
    private val removeListener: (listener: JDAListener?) -> Unit,
    token: String,
    strategy: Strategy,
    intervalMinutes: Int
) : KoreanbotsClient(token, strategy, intervalMinutes) {
    private var listener: JDAListener? = null

    companion object {
        fun KoreanbotsClient.Companion.create(
            jda: JDA,
            token: String,
            strategy: Strategy = Strategy.LISTENER,
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
                strategy,
                intervalMinutes
            )
        }

        fun KoreanbotsClient.Companion.create(
            shardManager: ShardManager,
            token: String,
            strategy: Strategy = Strategy.LISTENER,
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
                strategy,
                intervalMinutes
            )
        }
    }

    init {
        when (strategy) {
            Strategy.LISTENER -> listener = addListener(this)
            Strategy.LOOP -> startLoop(serversProvider)
        }
        shutdownChild = { removeListener(listener) }
    }
}