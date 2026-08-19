package com.turnin.domain.discover.domain.model

/**
 * 탐색 커서 (다음 페이지 조회를 위한 마지막 항목의 정렬 기준값)
 */
data class DiscoverCursor(
    val snapshotAt: Long,
    val lastScoreChunk: Int,
    val lastShuffleKey: Int,
    val lastUserId: Long,
)
