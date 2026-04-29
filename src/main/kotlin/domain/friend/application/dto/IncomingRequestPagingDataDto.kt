package com.turnin.domain.friend.application.dto

import com.turnin.common.util.pagination.offset.PagingData

/**
 * 나에게 들어온 친구 요청 페이징 데이터 DTO
 *
 * @property pagingData 페이징 데이터
 * @property requests 친구 요청 목록
 */
data class IncomingRequestPagingDataDto(
    val pagingData: PagingData,
    val requests: List<IncomingRequestInfoDto>,
)
