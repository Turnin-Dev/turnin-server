package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserProfileDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.provider.FriendProvider
import com.peekr.domain.user.domain.repository.UserRepository

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
     * @param includeBlocked 조회 시 차단 사용자 포함 여부 (`true`면 차단 사용자까지 함께 조회하고 `false`면 제외하고 조회한다.)
     *
     * @return [UserProfileDto] 사용자 프로필 DTO
     */
    suspend operator fun invoke(
        myUserId: Long,
        userId: Long,
        includeBlocked: Boolean,
    ): UserProfileDto? {
        // 0) 데이터 전처리
        val myUserVO = UserId(myUserId)
        val userIdVO = UserId(userId)

        // 1) 사용자, 친구 수, 친구 상태 조회
        val userDto = if (includeBlocked) {
            userRepository.findById(userIdVO)?.toDto() ?: return null
        } else {
            userRepository.findVisibleById(myUserVO, userIdVO)?.toDto() ?: return null
        }
        val friendsCount = friendProvider.countFriends(userDto.id)
        val friendshipStatus = friendProvider.getFriendStatus(myUserVO, userDto.id)

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
        )
    }
}
