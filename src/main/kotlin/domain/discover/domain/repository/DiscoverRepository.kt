package com.peekr.domain.discover.domain.repository

import com.peekr.common.model.KeywordSimilarityValues
import com.peekr.common.model.id.UserId
import com.peekr.domain.discover.domain.model.SharedUserKeyword

/**
 * 탐색 리포지토리
 */
interface DiscoverRepository {
    /**
     * 유사한 키워드를 공유하고 있는 사용자 ID 목록을 페이지네이션을 통해 조회한다.
     *
     * (사용자 탈퇴 시 사용자 키워드도 비활성화 되므로 사용자 키워드의 is_active 필드만 체크함)
     *
     * ### 쿼리 구조
     * 1. my_keywords: 내 최근 키워드 최대 5개를 시드로 사용
     * 2. similar_ids: 시드 키워드와 유사도 [KeywordSimilarityValues.HIGH_THRESHOLD] 이상인 키워드 ID 수집
     * 3. matched_users: 유사 키워드를 보유한 사용자 ID 수집 (비활성화 키워드, 차단 관계 제외)
     *
     * ### 페이지네이션
     * - 커서: 사용자 ID (초기 호출 시 `null`)
     * - 실제 조회 개수: `pageSize` (다음 페이지 존재 여부 확인용)
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
