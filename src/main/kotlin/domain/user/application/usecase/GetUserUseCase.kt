package com.turnin.domain.user.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.domain.user.application.dto.UserDto
import com.turnin.domain.user.application.dto.toDto
import com.turnin.domain.user.domain.repository.UserRepository

/**
 * 사용자 ID로 사용자를 조회한다.
 */
class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(currentId: UserId, id: UserId): UserDto? {
        val user = userRepository.findVisibleById(currentId, id)
        return user?.toDto()
    }
}
