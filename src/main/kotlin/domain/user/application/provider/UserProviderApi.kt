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
     * 사용자 ID로 사용자 존재 여부를 확인한다.
     *
     * @param userId 사용자 ID
     *
     * @return 존재하는 경우 `true`, 아닌 경우 `false`을 반환한다
     */
    suspend fun existsUser(userId: UserId): Boolean =
        userRepository.existsUser(userId)

    /**
     * 사용자 ID 목록으로 사용자 목록을 조회한다.
     *
     * @param userIds 사용자 ID 목록
     *
     * @return 사용자 목록
     */
    suspend fun findByIds(userIds: List<UserId>): List<UserDto> =
        userRepository.findByIds(userIds).map { it.toDto() }
}
