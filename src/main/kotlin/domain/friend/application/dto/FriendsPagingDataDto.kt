package com.turnin.domain.friend.application.dto

import com.turnin.common.util.pagination.offset.PagingData

/**
 * 페이지네이션용 친구 목록 DTO
 *
 * @property pagingData 페이징 데이터
 * @property friends 친구 목록
 */
data class FriendsPagingDataDto(
    val pagingData: PagingData,
    val friends: List<FriendInfoDto>,
)
