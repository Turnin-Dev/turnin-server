package com.peekr.domain.user.domain.service

import com.peekr.domain.core.model.UserId
import com.peekr.domain.user.domain.model.User

interface UserService {
    /**
     * ID를 통해 사용자를 조회한다.
     *
     * @param id 사용자 ID
     *
     * @return [User] - 사용자가 존재하지 않을 때는 **`null`** 반환
     */
    suspend fun getUserById(id: UserId): User?
}
