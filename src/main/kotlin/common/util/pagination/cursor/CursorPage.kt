package com.peekr.common.util.pagination.cursor

import kotlinx.serialization.Serializable

/**
 * 커서 페이지네이션을 사용할 때 사용하는 커서 페이지 클래스
 *
 * @property items 데이터 목록
 * @property nextCursor 다음 커서, 다음 데이터가 없으면 `null`
 */
@Serializable
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: Long?,
)

fun <T, R> CursorPage<T>.toResponse(
    mapper: (T) -> R,
): CursorPage<R> = CursorPage(items.map(mapper), nextCursor)
