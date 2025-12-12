package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.common.util.pagination.PagingData
import com.peekr.domain.friend.application.dto.FriendInfoDto
import com.peekr.domain.friend.application.dto.FriendsPagingDataDto
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository

/**
 * 친구 목록 페이지네이션 조회
 */
class GetFriendsPaginationUseCase(
    private val friendRepository: FriendRepository,
    private val userProvider: UserProvider,
) {
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

        // 1) 친구 목록 페이지네이션 조회
        val friendsPagingData = friendRepository.getFriendsPagination(
            userId = userIdVO,
            offset = paginationParams.offset,
            size = paginationParams.size,
        )

        // 2) 친구 정보 조회
        val friendIds = friendsPagingData.friends.map {
            if (it.requesterId == userIdVO) it.receiverId else it.requesterId
        }
        val friendInfos = userProvider.getUserInfos(friendIds)

        // 3) FriendInfoDto 목록 생성
        val friends = friendsPagingData.friends.zip(friendInfos).map { (friend, friendInfo) ->
            FriendInfoDto(
                id = friend.id.value,
                userId = friendInfo.id.value,
                name = friendInfo.name.value,
                profileImageUrl = friendInfo.profileImageUrl,
                respondedAt = friend.respondedAt,
                createdAt = friend.createdAt,
                updatedAt = friend.updatedAt,
            )
        }

        // 4) 최종 반환
        return FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = paginationParams.page,
                pageSize = paginationParams.size,
                totalSize = friendsPagingData.totalSize,
            ),
            friends = friends,
        )
    }
}
