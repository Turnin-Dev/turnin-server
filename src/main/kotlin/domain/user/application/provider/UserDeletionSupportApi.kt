package com.peekr.domain.user.application.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 외부에 제공할 User 삭제 제공 API
 */
class UserDeletionSupportApi(private val userRepository: UserRepository) {
    /**
     * 사용자를 비활성화한다.
     *
     * ###### 해당 메서드는 [userId]의 모든 데이터를 지우므로 주의해서 사용해야 한다.
     *
     * @param userId 비활성화할 사용자 ID
     */
    suspend fun deactivate(userId: UserId) =
        userRepository.deactivate(userId)
}
