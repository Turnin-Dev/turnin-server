package com.peekr.domain.user.application.usecase

import com.peekr.common.model.UserId
import com.peekr.domain.user.application.dto.UserProfileDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.service.UserService

/**
 * 사용자 ID로 사용자를 조회한다.
 */
class GetUserProfileUseCase(private val userService: UserService) {
    suspend operator fun invoke(id: UserId): UserProfileDto? {
        val userProfile = userService.getUserProfileById(id)
        return userProfile?.toDto()
    }
}
