package com.peekr.common.db.extension

import com.peekr.common.db.schema.UserKeywords
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere

/**
 * 활성화 사용자 키워드만 필터링 하는 쿼리
 */
fun Query.filterActiveUserKeyword(): Query =
    andWhere { UserKeywords.isActive eq true }
