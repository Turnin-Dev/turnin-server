package com.turnin.common.db

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.TextColumnType

/**
 * psql의 STRING_AGG 를 사용하기 위한 클래스
 */
class StringAgg(
    val expr: Expression<*>,
    val delimiter: String,
    val orderBy: Expression<*>? = null,
) : Function<String?>(TextColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append("STRING_AGG(")
        append(expr)
        append(", '$delimiter'")
        if (orderBy != null) {
            append(" ORDER BY ")
            append(orderBy)
        }
        append(")")
    }
}
