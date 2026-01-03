package com.peekr.domain.discover.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.discover.domain.model.SharedKeywords

/**
 * 탐색 리포지토리
 */
interface DiscoverRepository {
    /**
     * 같은 키워드를 공유하고 있는 사용자 ID와 키워드 ID 목록을 페이지네이션을 통해 조회한다.
     *
     * 초기 호출 시 커서 값은 `null`이다.
     *
     * @param userId 사용자 ID
     * @param cursor 커서 (사용자 ID)
     * @param pageSize 페이지 크기
     */
    suspend fun getSharedKeywords(
        userId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<SharedKeywords>
}
