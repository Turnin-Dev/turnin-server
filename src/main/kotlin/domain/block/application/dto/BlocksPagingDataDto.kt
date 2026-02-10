package com.peekr.domain.block.application.dto

import com.peekr.common.util.pagination.offset.PagingData

/**
 * 페이지네이션용 차단 목록 DTO
 */
data class BlocksPagingDataDto(
    val pagingData: PagingData,
    val blocks: List<BlockDto>,
)
