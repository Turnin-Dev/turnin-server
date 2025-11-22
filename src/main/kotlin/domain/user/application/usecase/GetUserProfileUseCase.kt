package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserProfileDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 사용자 ID로 사용자를 조회한다.
 */
class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(id: UserId): UserProfileDto? {
        val userProfile = userRepository.findUserProfileById(id)
        return userProfile?.toDto()
    }
}
