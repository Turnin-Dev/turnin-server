package com.peekr.common.db

import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table

/**
 * 카운팅해주는 서브쿼리 함수
 *
 * @property table 테이블 클래스
 * @property count 카운팅 식별자
 * @property where 카운팅 where 문
 *
 * @see <a href="https://jassielcastro.medium.com/exposed-in-action-customizing-results-with-subqueries-in-ktor-e0eda69a7b31">reference</a>
 */
open class SubCountQueryFunction<T : Table>(
    private val table: Alias<T>,
    private val count: String = "*",
    private val where: SqlExpressionBuilder.() -> Op<Boolean>,
) : Function<Long>(LongColumnType()) {
    /**
     * Return example:
     * (SELECT COUNT(*) FROM Table as TableAlias WHERE TableAlias.id = 1)
     */
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append('(')
        append("SELECT COUNT($count) FROM ${table.delegate.tableName} as ${table.alias} ")
        append("WHERE ${SqlExpressionBuilder.where()}")
        append(')')
    }
}
