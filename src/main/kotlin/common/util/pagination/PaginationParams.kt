package com.peekr.common.util.pagination

/**
 * 페이지네이션(오프셋 기반) 파라미터
 *
 * @property offset 오프셋
 * @property size 페이지 크기
 */
data class PaginationParams(
    val offset: Long,
    val size: Int,
)
