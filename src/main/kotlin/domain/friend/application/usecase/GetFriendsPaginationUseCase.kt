package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.common.util.pagination.PagingData
import com.peekr.domain.friend.application.dto.FriendsPagingDataDto
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 친구 목록 페이지네이션 조회
 */
class GetFriendsPaginationUseCase(private val friendRepository: FriendRepository) {
    /**
     * 친구 목록을 페이지네이션을 사용하여 조회한다.
     *
     * @param userId 사용자 ID
     * @param paginationParams 페이지네이션 파라미터
     *
     * @return [FriendsPagingDataDto] 친구 목록 페이지네이션 데이터 DTO
     */
    suspend operator fun invoke(
        userId: Long,
        paginationParams: PaginationParams,
    ): FriendsPagingDataDto {
        val userIdVO = UserId(userId)
        val friendsPagingData = friendRepository.getFriendsPagination(
            userId = userIdVO,
            offset = paginationParams.offset,
            size = paginationParams.size,
        )

        return FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = paginationParams.page,
                pageSize = paginationParams.size,
                totalSize = friendsPagingData.totalSize,
            ),
            friends = friendsPagingData.friends.map { it.toDto() },
        )
    }
}
