package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.PagingData
import com.peekr.domain.friend.application.dto.FriendsPagingDataDto
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 친구 목록 페이지네이션 조회
 */
class GetFriendsUseCase(private val friendRepository: FriendRepository) {
    /**
     * 친구 목록을 페이지네이션을 사용하여 조회한다.
     *
     * @param userId 사용자 ID
     * @param offset 페이지 오프셋
     * @param size 페이지 크기
     *
     * @return [FriendsPagingDataDto] 친구 목록 페이지네이션 데이터 DTO
     */
    suspend operator fun invoke(
        userId: Long,
        offset: Long,
        size: Int,
    ): FriendsPagingDataDto {
        val userIdVO = UserId(userId)
        val friendsPagingData = friendRepository.getFriendsPagination(userIdVO, offset, size)

        return FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = offset,
                pageSize = size,
                totalSize = friendsPagingData.totalSize,
            ),
            friends = friendsPagingData.friends.map { it.toDto() },
        )
    }
}
