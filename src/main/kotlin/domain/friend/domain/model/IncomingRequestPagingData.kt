package com.peekr.domain.friend.domain.model

/**
 * 나에게 들어온 친구 요청 페이징 데이터
 *
 * @property totalSize 전체 크기
 * @property requests 친구 요청 목록
 */
data class IncomingRequestPagingData(
    val totalSize: Long,
    val requests: List<IncomingRequest>,
)
