package com.peekr.util.db

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.id.UserId

/**
 * 사용자를 비활성화 상태로 업데이트한다.
 *
 * @param userId 사용자 ID
 */
suspend fun setUserInactive(userId: UserId) = TestDatabaseFactory.dbQuery {
    UserEntity.findByIdAndUpdate(userId.value) {
        it.isActive = false
    }
}
