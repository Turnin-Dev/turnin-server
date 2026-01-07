package com.peekr.common.db

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder

class FloatDistance(
    val expr1: Expression<*>,
    val expr2: Expression<*>,
) : Op<Double>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("$expr1 <=> $expr2")
    }
}

infix fun Expression<*>.floatDistance(other: Expression<*>): FloatDistance =
    FloatDistance(this, other)
