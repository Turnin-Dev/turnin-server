package com.peekr.common.util.pagination

/**
 * 페이지네이션(오프셋 기반) 파라미터
 *
 * @property page 페이지 번호
 * @property pageSize 페이지 크기
 * @property offset 오프셋
 */
data class PaginationParams(
    val page: Long,
    val pageSize: Long,
    val offset: Long,
)
