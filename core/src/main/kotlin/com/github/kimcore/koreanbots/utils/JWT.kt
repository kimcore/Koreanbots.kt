package com.github.kimcore.koreanbots.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.*

internal object JWT {
    fun decodeID(token: String): String {
        val parts = token.split(".")
        val json = Base64.getDecoder().decode(parts[1]).decodeToString()
        return Gson().fromJson(json, JsonObject::class.java)["id"].asString
    }
}