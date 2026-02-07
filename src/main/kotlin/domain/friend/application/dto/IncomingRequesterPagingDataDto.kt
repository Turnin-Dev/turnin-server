package com.peekr.domain.friend.application.dto

import com.peekr.common.util.pagination.offset.PagingData

/**
 * 페이지네이션용 받은 친구 요청자 목록 DTO
 *
 * @property pagingData 페이징 데이터
 * @property requesters 요청자 목록
 */
data class IncomingRequesterPagingDataDto(
    val pagingData: PagingData,
    val requesters: List<IncomingRequesterInfoDto>,
)
