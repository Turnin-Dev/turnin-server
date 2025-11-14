package com.peekr.domain.user.application.usecase

import com.peekr.common.model.UserId
import com.peekr.domain.user.application.dto.UserDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 사용자 ID로 사용자를 조회한다.
 */
class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(id: UserId): UserDto? {
        val user = userRepository.findById(id)
        return user?.toDto()
    }
}
