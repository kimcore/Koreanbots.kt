package com.github.kimcore.koreanbots.javacord

import com.github.kimcore.koreanbots.KoreanbotsClient
import org.javacord.api.event.server.ServerJoinEvent
import org.javacord.api.event.server.ServerLeaveEvent
import org.javacord.api.listener.server.ServerJoinListener
import org.javacord.api.listener.server.ServerLeaveListener

internal class JavacordListener(private val client: KoreanbotsClient) : ServerJoinListener, ServerLeaveListener {
    override fun onServerJoin(event: ServerJoinEvent?) {
        val servers = event!!.api.servers.size
        client.updateServersCount(servers)
    }

    override fun onServerLeave(event: ServerLeaveEvent?) {
        val servers = event!!.api.servers.filter { it != event.server }.size
        client.updateServersCount(servers)
    }
}