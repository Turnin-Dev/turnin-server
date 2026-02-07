package com.peekr.domain.friend.domain.model

/**
 * 페이지네이션용 받은 친구 요청자 목록
 *
 * @property totalSize 전체 크기
 * @property requesters 요청자 목록
 */
data class IncomingRequesterPagingData(
    val totalSize: Long,
    val requesters: List<IncomingRequester>,
)
