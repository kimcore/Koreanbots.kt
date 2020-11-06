package com.github.kimcore.koreanbots.entities

import kotlin.collections.List
import com.github.kimcore.koreanbots.entities.enums.ListType

@Suppress("unused")
data class List(
    val type: ListType,
    val data: List<Bot>,
    val currentPage: Int,
    val totalPage: Int
)