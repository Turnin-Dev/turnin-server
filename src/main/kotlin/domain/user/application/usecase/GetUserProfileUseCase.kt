package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserProfileDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.provider.FriendProvider
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 사용자 ID로 사용자 프로필 조회
 */
class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val friendProvider: FriendProvider,
) {
    /**
     * 사용자 ID로 사용자 프로필을 조회한다.
     *
     * @param myUserId 나의 사용자 ID
     * @param userId 사용자 ID
     *
     * @return [UserProfileDto] 사용자 프로필 DTO
     */
    suspend operator fun invoke(
        myUserId: UserId,
        userId: Long,
    ): UserProfileDto? {
        val userIdVO = UserId(userId)
        val userDto = userRepository.findById(userIdVO)?.toDto() ?: return null
        val friendsCount = friendProvider.countFriends(userDto.id)
        val friendshipStatus = friendProvider.getFriendStatus(myUserId, userDto.id)
        return UserProfileDto(
            userId = userDto.id.value,
            displayId = userDto.displayId,
            userName = userDto.userName,
            profileImageUrl = userDto.profileImageUrl,
            introduce = userDto.introduce,
            isActive = userDto.isActive,
            lastLoginAt = userDto.lastLoginAt,
            friendsCount = friendsCount,
            friendStatus = friendshipStatus,
        )
    }
}
