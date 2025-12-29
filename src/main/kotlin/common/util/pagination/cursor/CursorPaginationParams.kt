package com.peekr.common.util.pagination.cursor

/**
 * 페이지네이션(커서 기반) 파라미터
 *
 * @property cursor 커서 값
 * @property size 페이지 크기
 */
data class CursorPaginationParams<T>(
    val cursor: T?,
    val size: Int,
)
