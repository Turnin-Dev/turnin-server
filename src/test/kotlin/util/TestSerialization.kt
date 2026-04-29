package com.turnin.util

import kotlinx.serialization.json.Json

object TestSerialization {
    inline fun <reified T> String.decode(): T = Json.decodeFromString<T>(this)
}
