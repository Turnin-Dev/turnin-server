package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.common.util.pagination.offset.PagingData
import com.peekr.domain.friend.application.dto.IncomingRequesterInfoDto
import com.peekr.domain.friend.application.dto.IncomingRequesterPagingDataDto
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException

/**
 * 받은 친구 요청 목록 페이지네이션 조회
 *
 * @see invoke
 */
class GetIncomingRequestersUseCase(
    private val repository: FriendRepository,
    private val userProvider: UserProvider,
) {
    /**
     * 받은 친구 요청 목록을 페이지네이션을 사용하여 조회한다.
     *
     * @param userId 사용자 ID
     * @param paginationParams 페이지네이션 파라미터
     *
     * @throws FriendException.UserNotFoundException 요청자 목록을 조회하는 찰나의 순간에 사용자 정보가 사라지는 경우 예외가 발생한다.
     */
    suspend operator fun invoke(
        userId: Long,
        paginationParams: PaginationParams,
    ): IncomingRequesterPagingDataDto {
        val userIdVO = UserId(userId)

        // 1) 받은 친구 요청 목록 페이지네이션 조회
        val incomingRequesterPagingData = repository.getIncomingRequesters(
            userId = userIdVO,
            offset = paginationParams.offset,
            size = paginationParams.size,
        )
        if (incomingRequesterPagingData.requesters.isEmpty()) {
            return IncomingRequesterPagingDataDto(
                pagingData = PagingData(
                    pageNumber = paginationParams.page,
                    pageSize = paginationParams.size,
                    totalSize = incomingRequesterPagingData.totalSize,
                ),
                requesters = emptyList(),
            )
        }

        // 2) 요청한 친구 정보 조회
        val requesterIds = incomingRequesterPagingData.requesters.map { it.requesterId }
        if (requesterIds.isEmpty()) {
            return IncomingRequesterPagingDataDto(
                pagingData = PagingData(
                    pageNumber = paginationParams.page,
                    pageSize = paginationParams.size,
                    totalSize = incomingRequesterPagingData.totalSize,
                ),
                requesters = emptyList(),
            )
        }
        val requesterInfoMap = userProvider
            .getUserInfos(requesterIds)
            .associateBy { it.userId }

        // 3) FriendInfoDto 목록 생성
        val requesterInfos = incomingRequesterPagingData.requesters.map { requester ->
            val requesterInfo = requesterInfoMap[requester.requesterId]
            if (requesterInfo == null) {
                LOGGER.error(
                    "requesterInfo corresponding to ($${requester.requesterId}) is missing, requester: $requester",
                )
                throw FriendException.UserNotFoundException()
            }

            IncomingRequesterInfoDto(
                id = requester.id.value,
                userId = requesterInfo.userId.value,
                displayId = requesterInfo.displayId.value,
                name = requesterInfo.userName.value,
                profileImageUrl = requesterInfo.profileImageUrl,
                respondedAt = requester.respondedAt,
                createdAt = requester.createdAt,
                updatedAt = requester.updatedAt,
            )
        }

        // 4) 최종 반환
        return IncomingRequesterPagingDataDto(
            pagingData = PagingData(
                pageNumber = paginationParams.page,
                pageSize = paginationParams.size,
                totalSize = incomingRequesterPagingData.totalSize,
            ),
            requesters = requesterInfos,
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetIncomingRequestersUseCase>()
