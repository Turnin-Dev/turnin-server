package com.turnin.util.db

import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId

/**
 * 사용자를 비활성화 상태로 업데이트한다.
 *
 * @param userId 사용자 ID
 */
suspend fun setUserInactiveForTest(userId: UserId) = TestDatabaseFactory.dbQuery {
    UserEntity.findByIdAndUpdate(userId.value) {
        it.isActive = false
    }
}

/**
 * 사용자 키워드를 비활성화 상태로 업데이트한다.
 *
 * @param userKeywordId 사용자 키워드 ID
 */
suspend fun setUserKeywordInactiveForTest(userKeywordId: UserKeywordId) = TestDatabaseFactory.dbQuery {
    UserKeywordEntity.findByIdAndUpdate(userKeywordId.value) {
        it.isActive = false
    }
}
