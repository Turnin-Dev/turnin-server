package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.OtherUserProfileDto
import com.peekr.domain.user.domain.provider.FriendProvider

/**
 * (본인 관점) 다른 사용자 프로필 조회
 */
class GetOtherUserProfileUseCase(
    private val friendProvider: FriendProvider,
    private val getProfileUseCase: GetProfileUseCase,
) {
    /**
     * (본인 관점) 다른 사용자의 프로필을 조회한다.
     *
     * @param userId 나의 사용자 ID
     * @param otherUserId 다른 사용자 ID
     *
     * @return [OtherUserProfileDto] 다른 사용자 프로필 DTO
     */
    suspend operator fun invoke(
        userId: UserId,
        otherUserId: Long,
    ): OtherUserProfileDto? {
        val otherUserIdVO = UserId(otherUserId)
        val userProfileDto = getProfileUseCase(userId) ?: return null
        val friendshipStatus = friendProvider.getFriendshipStatus(userId, otherUserIdVO)
        return OtherUserProfileDto(
            userProfileDto = userProfileDto,
            friendshipStatus = friendshipStatus,
        )
    }
}
