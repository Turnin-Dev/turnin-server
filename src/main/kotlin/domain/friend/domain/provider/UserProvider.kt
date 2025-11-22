package com.peekr.domain.friend.domain.provider

import com.peekr.common.model.id.UserId

interface UserProvider {
    /**
     * 사용자 ID를 통해 사용자가 있는지 확인한다.
     */
    suspend fun existsUser(userId: UserId): Boolean
}
