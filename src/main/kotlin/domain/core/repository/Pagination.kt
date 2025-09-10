package com.peekr.domain.core.repository

// TODO: 임시

/** 페이지네이션 데이터 클래스 */
data class Pagination(
    val itemsPerPage: Int,
    val page: Int,
)

open class PaginationResult<T : Any>(
    val items: List<T>,
    val total: Long,
)
