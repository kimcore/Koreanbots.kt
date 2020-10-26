package com.github.kimcore.koreanbots.entities

import com.github.kimcore.koreanbots.entities.enums.ListType
import kotlin.collections.List

data class List(
    val type: ListType,
    val data: List<Bot>,
    val currentPage: Int,
    val totalPage: Int

)