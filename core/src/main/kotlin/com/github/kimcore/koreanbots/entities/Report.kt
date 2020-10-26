package com.github.kimcore.koreanbots.entities

import com.github.kimcore.koreanbots.entities.enums.ReportedType

data class Report(
    val id: Int,
    val issuer: User,
    val type: ReportedType,
    val reported: String,
    val bot: Bot?,
    val state: Int,
    val category: String,
    val desc: String
)