package com.turnin.domain.user.application.usecase

import com.turnin.common.model.FriendStatus
import com.turnin.common.model.id.UserId
import com.turnin.domain.user.application.dto.UserProfileDto
import com.turnin.domain.user.application.dto.toDto
import com.turnin.domain.user.domain.provider.FriendProvider
import com.turnin.domain.user.domain.repository.UserRepository

/**
 * 사용자 ID로 사용자 프로필 조회
 *
 * @see invoke
 */
class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val friendProvider: FriendProvider,
) {
    /**
     * 사용자 ID로 사용자 프로필을 조회한다.
     *
     * @param myUserId 나의 사용자 ID
     * @param userId 조회할 사용자 ID
     *
     * @return [UserProfileDto] 사용자 프로필 DTO
     */
    suspend operator fun invoke(
        myUserId: Long,
        userId: Long,
    ): UserProfileDto? {
        // 0) 데이터 전처리
        val myUserVO = UserId(myUserId)
        val userIdVO = UserId(userId)

        // 1) 사용자, 친구 수, 친구 상태 조회 (isBlocked 여부에 따른 마스킹 처리는 여기서 수행)
        val user = userRepository.findVisibleById(myUserVO, userIdVO) ?: return null
        val userDto = user.toDto()
        val friendsCount = if (user.isBlocked) {
            0
        } else {
            friendProvider.countFriends(userDto.id)
        }
        val friendshipStatus = if (user.isBlocked) {
            FriendStatus.NOTHING
        } else {
            friendProvider.getFriendStatus(myUserVO, userDto.id)
        }

        // 2) 최종 반환
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
            isBlocked = user.isBlocked,
        )
    }
}
