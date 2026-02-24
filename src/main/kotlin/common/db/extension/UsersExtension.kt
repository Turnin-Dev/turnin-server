package com.peekr.common.db.extension

import com.peekr.common.db.schema.Users
import com.peekr.common.model.id.UserId
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.intLiteral

/**
 * 사용자 존재 여부 확인
 *
 * @param userId 사용자 ID
 */
fun Users.existsUser(userId: UserId): Boolean =
    this
        .select(intLiteral(1))
        .where { Users.id eq userId.value }
        .filterActiveUser()
        .limit(1)
        .any()

/**
 * 활성화 사용자만 필터링 하는 쿼리
 */
fun Query.filterActiveUser(): Query =
    andWhere { Users.isActive eq true }
