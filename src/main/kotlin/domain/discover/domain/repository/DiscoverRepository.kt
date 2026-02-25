package com.peekr.domain.discover.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.domain.discover.domain.model.SharedUserKeyword

/**
 * 탐색 리포지토리
 */
interface DiscoverRepository {
    /**
     * 유사한 키워드를 공유하고 있는 사용자 ID 목록을 페이지네이션을 통해 조회한다. (비활성화 사용자의 키워드 제외)
     *
     * (사용자 탈퇴 시 사용자 키워드도 비활성화 되므로 사용자 키워드의 is_active 필드만 체크함)
     *
     * 초기 호출 시 커서 값은 `null`이다.
     *
     * 페이지네이션을 위해 실제 조회 개수는 `pageSize + 1`이다.
     *
     * #### 해당 쿼리는 초기 서비스 단계에서만 유효하다. 키워드가 많아지면 '키워드 사전 캐싱', '쿼리 최적화' 등이 필요하다.
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
     *
     * [findUserIdsWithSimilarKeywords]의 후속 호출 메서드이므로 비활성화 사용자/사용자 키워드 필터링이 되어있지 않다.
     * ([findUserIdsWithSimilarKeywords]에서 이미 걸러진다고 가정한다)
     *
     * @param matchedUserIds (나와 유사한 키워드를 공유하고 있는) 사용자 ID 리스트
     */
    suspend fun fetchSharedUserKeywords(
        matchedUserIds: List<UserId>,
    ): List<SharedUserKeyword>
}
