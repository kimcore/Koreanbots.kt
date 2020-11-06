package com.github.kimcore.koreanbots.jda

import com.github.kimcore.koreanbots.KoreanbotsClient
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

internal class JDAListener(private val client: KoreanbotsClient) : ListenerAdapter() {
    override fun onReady(event: ReadyEvent) = updateServersCount(event.jda)

    override fun onGuildJoin(event: GuildJoinEvent) = updateServersCount(event.jda)

    override fun onGuildLeave(event: GuildLeaveEvent) = updateServersCount(event.jda)

    private fun updateServersCount(jda: JDA) {
        val servers = if (jda.shardManager != null) {
            jda.shardManager!!.guilds
        } else {
            jda.guilds
        }.size
        client.updateServers(servers)
    }
}