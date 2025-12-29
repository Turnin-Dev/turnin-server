package com.peekr.common.util.pagination.offset

/**
 * 공통 페이징 데이터
 *
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 현재 페이지 크기
 * @property totalSize 전체 크기
 */
open class PagingData(
    val pageNumber: Long,
    val pageSize: Int,
    val totalSize: Long,
) {
    /**
     * 다음 페이지 존재 여부
     */
    val hasNext: Boolean
        get() = (pageNumber * pageSize) < totalSize
}
