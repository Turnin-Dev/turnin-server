package com.peekr.domain.block.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.common.util.pagination.offset.PagingData
import com.peekr.domain.block.application.dto.BlocksPagingDataDto
import com.peekr.domain.block.application.dto.toDto
import com.peekr.domain.block.domain.repository.BlockRepository

/**
 * 차단 목록 조회
 *
 * @see invoke
 */
class GetBlocksUseCase(private val blockRepository: BlockRepository) {
    /**
     * 차단 목록을 조회한다. (페이지네이션)
     *
     * @param userId 조회할 사용자 ID
     * @param paginationParams 페이지네이션 파라미터
     *
     * @return [BlocksPagingDataDto] 차단 목록 페이징 데이터 DTO
     */
    suspend operator fun invoke(
        userId: Long,
        paginationParams: PaginationParams,
    ): BlocksPagingDataDto {
        val userIdVO = UserId(userId)
        val blocksPagingData = blockRepository.getBlocksById(
            userId = userIdVO,
            offset = paginationParams.offset,
            size = paginationParams.size,
        )
        return BlocksPagingDataDto(
            pagingData = PagingData(
                pageNumber = paginationParams.page,
                pageSize = paginationParams.size,
                totalSize = blocksPagingData.totalSize,
            ),
            blocks = blocksPagingData.blocks.map { it.toDto() },
        )
    }
}
