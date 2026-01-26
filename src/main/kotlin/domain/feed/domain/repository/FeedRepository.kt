package com.peekr.domain.feed.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.feed.domain.model.Feed

/**
 * 피드 리포지토리
 */
interface FeedRepository {
    /**
     * # 사용자 맞춤 추천 피드 조회
     *
     * ## 추천 알고리즘:
     * - 사용자의 최근 관심 키워드 5개를 추출
     * - 각 키워드별로 벡터 유사도 기반 유사 글 20개씩 수집
     * - 친구가 작성한 글에 가중치(+100) 부여
     * - 최종 점수 기준 내림차순 정렬 및 커서 페이지네이션 적용
     * - 차단한 사용자의 글 필터링
     *
     * ## 쿼리 구조 (CTE):
     *
     * ### my_top_keywords
     * 현재 사용자의 최근 관심 키워드 5개와 임베딩 벡터 조회.
     * 추천의 기준(seed)이 되는 데이터.
     *
     * ### candidate_pool
     * 각 키워드별로 LATERAL JOIN을 통해 벡터 유사도 검색 수행.
     *
     * 키워드마다 가장 유사한 사용자 글 20개씩 수집하여 후보 풀을 구성.
     * (자신의 글은 제외됨)
     *
     * 총 5개의 키워드 * 20개씩 수집 -> 총 100개의 후보풀 구성
     *
     * ### friends
     * 양방향 친구 관계를 정규화하여 친구 ID 목록 추출.
     *
     * ### scored_pool
     * 후보 풀에서 중복 제거 및 최종 점수 계산.
     *    - DISTINCT ON (uk_id): 여러 키워드에 매칭된 글은 가장 높은 유사도 기준으로 1개만 선택
     *    - final_score = (유사도 × 50) + (친구 보너스 100)
     *    - 차단한 사용자의 글은 WHERE 절에서 제외
     *
     * ### result_list
     * 최종 결과 값을 커서 페이지네이션을 이용하여 계산한다.
     *
     * 커서 값은 점수(final_score), 생성일(uk_created_at), 사용자 키워드 ID(uk_id) 이렇게 3개를 사용한다.
     *
     * ## 페이지네이션:
     * - 커서 기반 페이지네이션 사용. 이전 페이지의 마지막 항목 정보(점수, 생성일, ID)를
     *  기준으로 다음 페이지 조회. 중복 없이 안정적인 페이징 보장.
     * - 커서 페이지네이션을 사용하기 위해 **`limit + 1`개를 조회**한다.
     *
     * @param userId 조회 대상 사용자 ID
     * @param cursorScore 이전 페이지 마지막 항목의 점수 (초기 조회 시 null)
     * @param cursorCreatedAt 이전 페이지 마지막 항목의 생성일시 (초기 조회 시 null)
     * @param cursorUkId 이전 페이지 마지막 항목의 user_keyword ID (초기 조회 시 null)
     * @param limit 조회할 항목 개수 (실제로는 limit + 1개를 조회)
     *
     * @return 추천 피드 항목 리스트
     */
    suspend fun getFeeds(
        userId: UserId,
        cursorScore: Double?,
        cursorCreatedAt: Long?,
        cursorUkId: UserKeywordId?,
        limit: Int,
    ): List<Feed>
}
