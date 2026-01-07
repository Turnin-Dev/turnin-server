package com.peekr.common.db

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.postgresql.util.PGobject

/**
 * pgvector를 위한 커스텀 타입
 */
class VectorColumnType(private val dim: Int) : ColumnType<String>() {
    override fun sqlType(): String =
        if (currentDialect is PostgreSQLDialect) "vector($dim)" else "text"

    override fun valueFromDB(value: Any): String = when (value) {
        is PGobject -> value.value ?: "[]"
        is String -> value
        else -> value.toString()
    }

    override fun notNullValueToDB(value: String): Any =
        if (currentDialect is PostgreSQLDialect) {
            val pgObject = PGobject()
            pgObject.type = "vector"
            pgObject.value = value
            pgObject
        } else {
            value
        }

    override fun nonNullValueToString(value: String): String = "'$value'"
}
