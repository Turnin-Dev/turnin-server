package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.MyProfileDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.provider.FriendProvider
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 나의 사용자 ID로 나의 프로필을 조회한다.
 */
class GetMyProfileUseCase(
    private val userRepository: UserRepository,
    private val friendProvider: FriendProvider,
) {
    suspend operator fun invoke(id: UserId): MyProfileDto? {
        val userDto = userRepository.findById(id)?.toDto() ?: return null
        val friendsCount = friendProvider.countFriends(id)
        return MyProfileDto(
            userId = userDto.id.value,
            displayId = userDto.displayId,
            name = userDto.name,
            profileImageUrl = userDto.profileImageUrl,
            introduce = userDto.introduce,
            isActive = userDto.isActive,
            lastLoginAt = userDto.lastLoginAt,
            friendsCount = friendsCount,
        )
    }
}
