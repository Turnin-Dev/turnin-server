package com.peekr.common.util.pagination

/**
 * 페이지네이션(오프셋 기반) 파라미터
 *
 * @property page 페이지 번호
 * @property size 페이지 크기
 */
data class PaginationParams(
    val page: Long,
    val size: Int,
) {
    /** 오프셋: (페이지 번호 - 1) * 페이지 크기 */
    val offset: Long
        get() = (page - 1) * size
}
