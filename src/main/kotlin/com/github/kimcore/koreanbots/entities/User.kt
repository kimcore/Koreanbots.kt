package com.github.kimcore.koreanbots.entities

import kotlin.collections.List

data class User(
    val id: String,
    val avatar: String?,
    val tag: String,
    val username: String,
    val perm: Int,
    val github: String?,
    val bots: List<Bot>
)