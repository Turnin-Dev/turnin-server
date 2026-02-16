package com.peekr.domain.block.domain.model

/**
 * 페이지네이션용 차단 사용자 목록
 *
 * @property hasNext 다음 페이지 존재 여부
 * @property blockedUsers 차단 사용자 목록
 */
data class BlockedUsersPagingData(
    val hasNext: Boolean,
    val blockedUsers: List<BlockedUser>,
)
