package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.log.AppLoggerFactory
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.common.util.pagination.offset.PagingData
import com.peekr.domain.friend.application.dto.IncomingRequestInfoDto
import com.peekr.domain.friend.application.dto.IncomingRequestPagingDataDto
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException

/**
 * 나에게 들어온 친구 요청 목록 페이지네이션 조회
 *
 * @see invoke
 */
class GetIncomingRequestsUseCase(private val repository: FriendRepository) {
    /**
     * 나에게 들어온 친구 요청 목록을 페이지네이션을 사용하여 조회한다.
     *
     * @param userId 사용자 ID
     * @param paginationParams 페이지네이션 파라미터
     *
     * @throws FriendException.UserNotFoundException 친구 요청 목록을 조회하는 찰나의 순간에 사용자 정보가 사라지는 경우 예외가 발생한다.
     */
    suspend operator fun invoke(
        userId: Long,
        paginationParams: PaginationParams,
    ): IncomingRequestPagingDataDto {
        val userIdVO = UserId(userId)

        // 1) 나에게 들어온 친구 요청 목록 페이지네이션 조회
        val incomingRequestPagingData = repository.getIncomingRequests(
            userId = userIdVO,
            offset = paginationParams.offset,
            size = paginationParams.size,
        )
        if (incomingRequestPagingData.requests.isEmpty()) {
            return IncomingRequestPagingDataDto(
                pagingData = PagingData(
                    pageNumber = paginationParams.page,
                    pageSize = paginationParams.size,
                    totalSize = incomingRequestPagingData.totalSize,
                ),
                requests = emptyList(),
            )
        }

        // 2) 요청한 친구 정보 조회
        val requesterId = incomingRequestPagingData.requests.map { it.requesterId }
        val requesterInfoMap = repository
            .getUserInfos(requesterId)
            .associateBy { it.userId }

        // 3) IncomingRequestInfoDto 목록 생성
        val requests = incomingRequestPagingData.requests.mapNotNull { request ->
            val requesterInfo = requesterInfoMap[request.requesterId]

            // 목록에는 있지만 정보를 못 찾아온 경우 (데이터 불일치, 사용자 탈퇴 등)
            // 만약 아래 if 식을 타게 되면, 전체 크기 데이터 정합성에 문제가 생기지만 크게 중요하지 않다고 판단.
            if (requesterInfo == null) {
                LOGGER.error(
                    "requesterInfo corresponding to (${request.requesterId}) is missing, requester: $request",
                )
                return@mapNotNull null
            }

            IncomingRequestInfoDto(
                id = request.id.value,
                userId = requesterInfo.userId.value,
                displayId = requesterInfo.displayId.value,
                name = requesterInfo.userName.value,
                profileImageUrl = requesterInfo.profileImageUrl,
                respondedAt = request.respondedAt,
                createdAt = request.createdAt,
                updatedAt = request.updatedAt,
            )
        }

        // 4) 최종 반환
        return IncomingRequestPagingDataDto(
            pagingData = PagingData(
                pageNumber = paginationParams.page,
                pageSize = paginationParams.size,
                totalSize = incomingRequestPagingData.totalSize,
            ),
            requests = requests,
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetIncomingRequestsUseCase>()
