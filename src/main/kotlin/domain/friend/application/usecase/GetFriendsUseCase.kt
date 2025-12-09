package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.domain.friend.application.dto.FriendDto
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 친구 목록 조회
 */
class GetFriendsUseCase(private val friendRepository: FriendRepository) {
    /**
     * 친구 목록을 조회한다.
     *
     * @param userId 사용자 ID
     */
    suspend operator fun invoke(
        userId: Long,
        paginationParams: PaginationParams,
    ): List<FriendDto> {
        val userIdVO = UserId(userId)
        return friendRepository.getFriendsPagination(userIdVO, paginationParams).map { it.toDto() }
    }
}
