package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.common.util.pagination.PagingData
import com.peekr.domain.friend.application.dto.FriendInfoDto
import com.peekr.domain.friend.application.dto.FriendsPagingDataDto
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException

/**
 * 친구 목록 페이지네이션 조회
 *
 * @see invoke
 */
class GetFriendsPaginationUseCase(
    private val friendRepository: FriendRepository,
    private val userProvider: UserProvider,
) {
    private val logger = AppLoggerFactory.createLogger(this::class.java.simpleName)

    /**
     * 친구 목록을 페이지네이션을 사용하여 조회한다.
     *
     * @param userId 사용자 ID
     * @param paginationParams 페이지네이션 파라미터
     *
     * @return [FriendsPagingDataDto] 친구 목록 페이지네이션 데이터 DTO
     *
     * @throws FriendException.UserNotFoundException 사용자를 찾지 못하는 경우
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
        if (friendIds.isEmpty()) {
            return FriendsPagingDataDto(
                pagingData = PagingData(
                    pageNumber = paginationParams.page,
                    pageSize = paginationParams.size,
                    totalSize = friendsPagingData.totalSize,
                ),
                friends = emptyList(),
            )
        }
        val friendInfoMap = userProvider
            .getUserInfos(friendIds)
            .associateBy { it.id }

        // 3) FriendInfoDto 목록 생성
        val friends = friendsPagingData.friends.map { friend ->
            val targetId = if (friend.requesterId == userIdVO) friend.receiverId else friend.requesterId
            val friendInfo = friendInfoMap[targetId]
            // 만약 친구 목록에는 있지만, UserProvider 에서 정보를 못 찾아온 경우 (데이터 불일치, 사용자 탈퇴 등)
            if (friendInfo == null) {
                logger.error("friendInfo corresponding to ($targetId) is missing, friend: $friend")
                throw FriendException.UserNotFoundException()
            }

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
