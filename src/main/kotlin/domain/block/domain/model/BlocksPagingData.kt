package com.peekr.domain.block.domain.model

/**
 * 페이지네이션용 차단 목록
 *
 * @property totalSize 전체 크기
 * @property blocks 차단 목록
 */
data class BlocksPagingData(
    val totalSize: Long,
    val blocks: List<Block>,
)
