package com.peekr.common.db

import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.PGobject

/**
 * pgvector를 위한 커스텀 타입
 */
class VectorColumnType(private val dim: Int) : ColumnType<String>() {
    override fun sqlType(): String = "vector($dim)"

    override fun valueFromDB(value: Any): String = when (value) {
        is PGobject -> value.value ?: "[]"
        is String -> value
        else -> value.toString()
    }

    override fun notNullValueToDB(value: String): Any {
        val pgObject = PGobject()
        pgObject.type = "vector"
        pgObject.value = value
        return pgObject
    }

    override fun nonNullValueToString(value: String): String = "'$value'"
}
