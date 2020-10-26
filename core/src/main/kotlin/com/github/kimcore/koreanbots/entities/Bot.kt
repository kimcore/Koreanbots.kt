package com.github.kimcore.koreanbots.entities

import com.github.kimcore.koreanbots.entities.enums.BotState
import com.github.kimcore.koreanbots.entities.enums.Status
import kotlin.collections.List

data class Bot(
    val id: String,
    val lib: String,
    val prefix: String,
    val name: String,
    val servers: Int?,
    val votes: Int,
    val intro: String,
    val desc: String,
    val avatar: String?,
    val url: String?,
    val web: String?,
    val git: String?,
    val category: List<String>,
    val tag: String,
    val discord: String?,
    val state: BotState,
    val verified: Boolean,
    val trusted: Boolean,
    val boosted: Boolean,
    val partnered: Boolean,
    val vanity: String?,
    val banner: String?,
    val status: Status?,
    val bg: String?,
    val owners: List<User>
)