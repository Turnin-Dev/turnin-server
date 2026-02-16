package com.peekr.domain.block.application.dto

import com.peekr.common.util.pagination.offset.SimplePagingData

/**
 * 페이지네이션용 차단 사용자 목록 DTO
 */
data class BlockedUsersPagingDataDto(
    val pagingData: SimplePagingData,
    val blockUsers: List<BlockedUserDto>,
)
