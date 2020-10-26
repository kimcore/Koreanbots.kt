package com.github.kimcore.koreanbots.entities

import kotlin.collections.List

data class SubmittedBot(
    val id: String,
    val date: Long,
    val category: List<String>,
    val lib: String,
    val prefix: String,
    val intro: String,
    val desc: String,
    val url: String?,
    val web: String?,
    val git: String?,
    val discord: String?,
    val state: Int,
    val owners: List<User>,
    val reason: String?
)