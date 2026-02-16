package com.peekr.domain.block.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.common.util.pagination.offset.SimplePagingData
import com.peekr.domain.block.application.dto.BlockUsersPagingDataDto
import com.peekr.domain.block.application.dto.toDto
import com.peekr.domain.block.domain.repository.BlockRepository

/**
 * 차단 사용자 목록 조회
 *
 * @see invoke
 */
class GetBlockUsersUseCase(private val blockRepository: BlockRepository) {
    /**
     * 차단 사용자 목록을 조회한다. (페이지네이션)
     *
     * @param userId 조회할 사용자 ID
     * @param paginationParams 페이지네이션 파라미터
     *
     * @return [BlockUsersPagingDataDto] 차단 목록 페이징 데이터 DTO
     */
    suspend operator fun invoke(
        userId: Long,
        paginationParams: PaginationParams,
    ): BlockUsersPagingDataDto {
        val userIdVO = UserId(userId)
        val blocksPagingData = blockRepository.getBlockUsersById(
            userId = userIdVO,
            offset = paginationParams.offset,
            size = paginationParams.size,
        )
        return BlockUsersPagingDataDto(
            pagingData = SimplePagingData(
                pageNumber = paginationParams.page,
                pageSize = paginationParams.size,
                hasNext = blocksPagingData.hasNext,
            ),
            blockUsers = blocksPagingData.blockUsers.map { it.toDto() },
        )
    }
}
