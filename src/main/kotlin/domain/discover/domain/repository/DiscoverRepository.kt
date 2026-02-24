package com.peekr.domain.discover.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.domain.discover.domain.model.SharedUserKeyword

/**
 * 탐색 리포지토리
 */
interface DiscoverRepository {
    /**
     * 유사한 키워드를 공유하고 있는 사용자 ID 목록을 페이지네이션을 통해 조회한다. (비활성화 사용자 키워드 제외)
     *
     * 초기 호출 시 커서 값은 `null`이다.
     *
     * 페이지네이션을 위해 실제 조회 개수는 `pageSize + 1`이다.
     *
     * @param targetUserId 조회할 사용자 ID
     * @param cursor 커서 (사용자 ID)
     * @param pageSize 페이지 크기
     */
    suspend fun findUserIdsWithSimilarKeywords(
        targetUserId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): List<UserId>

    /**
     * 사용자 ID 리스트를 통해 (나와 유사한 키워드를 공유하고 있는) 사용자 키워드 상세정보를 조회한다.
     * (비활성화 사용자, 비활성화 사용자 키워드 제외)
     *
     * @param matchedUserIds (나와 유사한 키워드를 공유하고 있는) 사용자 ID 리스트
     */
    suspend fun fetchSharedUserKeywords(
        matchedUserIds: List<UserId>,
    ): List<SharedUserKeyword>
}
