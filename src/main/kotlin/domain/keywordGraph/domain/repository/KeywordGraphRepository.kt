package com.peekr.domain.keywordGraph.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.keywordGraph.domain.model.SharedKeywordInfo

/**
 * Keyword Graph 리포지토리
 */
interface KeywordGraphRepository {
    /**
     * 같은 키워드를 공유하고 있는 사용자 ID와 키워드 ID 목록을 페이지네이션을 통해 반환한다.
     *
     * 초기 호출 시 커서 값은 `null`이다.
     */
    suspend fun getSharedKeywordInfos(
        userId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<SharedKeywordInfo>
}
