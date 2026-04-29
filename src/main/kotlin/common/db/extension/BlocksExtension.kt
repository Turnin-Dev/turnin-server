package com.turnin.common.db.extension

import com.turnin.common.db.schema.Blocks
import com.turnin.common.model.id.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.or

/**
 * 차단 사용자 여부 확인
 *
 * [userId1]과 [userId2]의 차단 관계를 확인한다.
 *
 * 둘 중 한 명이라도 서로를 차단한 관계라면 `true`를 반환하고 아니라면 `false`를 반환한다.
 *
 * @param userId1 차단 관계 사용자 1 ID
 * @param userId2 차단 관계 사용자 2 ID
 */
fun Blocks.isBlockedRelationship(
    userId1: UserId,
    userId2: UserId,
): Boolean =
    this
        .select(intLiteral(1))
        .where {
            (Blocks.blockerId eq userId1.value and (Blocks.blockedId eq userId2.value)) or
                (Blocks.blockerId eq userId2.value and (Blocks.blockedId eq userId1.value))
        }.limit(1)
        .any()
