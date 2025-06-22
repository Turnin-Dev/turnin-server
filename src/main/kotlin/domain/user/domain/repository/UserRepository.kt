package com.peekr.domain.user.domain.repository

import com.peekr.domain.user.domain.model.User

interface UserRepository {
    /**
     * ID를 통해 사용자를 조회한다.
     *
     * @param id 사용자 ID
     *
     * @return [com.peekr.domain.user.domain.model.User] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun getUserById(id: Long): User?
}
