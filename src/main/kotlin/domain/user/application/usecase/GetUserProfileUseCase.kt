package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserProfileDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.provider.FriendProvider
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 사용자 ID로 사용자 프로필을 조회한다.
 */
class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val friendProvider: FriendProvider,
) {
    suspend operator fun invoke(id: UserId): UserProfileDto? {
        val userDto = userRepository.findById(id)?.toDto() ?: return null
        val friendsCount = friendProvider.countFriends(id)
        return UserProfileDto(userDto, friendsCount)
    }
}
