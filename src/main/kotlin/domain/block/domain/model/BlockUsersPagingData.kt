package com.peekr.domain.block.domain.model

/**
 * 페이지네이션용 차단 사용자 목록
 *
 * @property hasNext 다음 페이지 존재 여부
 * @property blockUsers 차단 사용자 목록
 */
data class BlockUsersPagingData(
    val hasNext: Boolean,
    val blockUsers: List<BlockUser>,
)
