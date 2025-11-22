package com.peekr.domain.user.application.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 외부에 제공할 사용자 API
 */
class UserProviderApi(private val userRepository: UserRepository) {
    /**
     * 사용자 ID로 사용자를 조회한다.
     *
     * @param userId 사용자 ID
     *
     * @return 조회에 성공 시 [UserDto], 실패 시 `null`을 반환한다
     */
    suspend fun findById(userId: UserId): UserDto? =
        userRepository.findById(userId)?.toDto()
}
