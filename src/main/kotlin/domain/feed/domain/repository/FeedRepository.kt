package com.turnin.domain.feed.domain.repository

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.feed.domain.model.Feed

/**
 * 피드 리포지토리
 */
interface FeedRepository {
    /**
     * # 사용자 맞춤 추천 피드 조회
     *
     * (비활성화 사용자의 키워드 제외, 사용자 탈퇴 시 사용자 키워드도 비활성화 되므로 사용자 키워드의 is_active 필드만 체크함)
     *
     * ## 추천 알고리즘:
     * - 사용자의 최근 관심 키워드 5개의 카테고리를 추출
     * - 친구가 작성한 글은 우선적으로 포함 (friend_pool)
     * - 동일 카테고리의 글을 최신순으로 similarPoolLimit개 수집 (similar_pool)
     * - 위 두 풀을 채우지 못한 경우 최신 글로 보완 (fallback_pool, 최대 fallbackPoolLimit개)
     * - 친구 글에 가중치(+100), 카테고리 일치 글에 고정 유사도(0.5) 부여
     * - 최종 점수 기준 내림차순 정렬 및 커서 페이지네이션 적용
     * - 차단한 사용자의 글 필터링
     *
     * ## 쿼리 구조 (CTE):
     *
     * ### my_top_keywords
     * 현재 사용자의 최근 관심 키워드 5개와 카테고리 조회.
     *
     * ### my_categories
     * my_top_keywords에서 중복 제거한 카테고리 목록. MATERIALIZED로 한 번만 계산.
     *
     * ### friends
     * 양방향 친구 관계를 정규화하여 친구 ID 목록 추출.
     *
     * ### blocked_users
     * 차단/피차단 사용자 ID 목록. MATERIALIZED로 한 번만 계산.
     *
     * ### friend_pool (priority=1)
     * 친구가 작성한 활성 키워드 글 전체 수집. similarity는 0.0으로 고정.
     *
     * ### similar_pool (priority=2)
     * my_categories와 동일 카테고리 키워드 글 수집.
     * similarity는 0.5로 고정. 친구 글 및 차단 사용자 글 제외.
     * 관심 카테고리가 없으면 즉시 0행 반환.
     *
     * ### fallback_pool (priority=3)
     * similar_pool이 부족할 경우를 대비한 최신 글 보완 풀.
     * 카테고리와 무관하게 최신순으로 최대 fallbackPoolLimit개 수집.
     * 친구 글 및 차단 사용자 글 제외.
     *
     * ### scored_pool
     * 세 풀을 UNION ALL 후 중복 제거 및 최종 점수 계산.
     * - DISTINCT ON (uk_id): priority 오름차순 기준으로 1개만 선택 (friend > similar > fallback)
     * - final_score = (유사도 × 50) + (친구 보너스 100)
     *
     * ### result_list
     * 커서 페이지네이션 적용. 커서는 (final_score, uk_created_at, uk_id) 3개 사용.
     *
     * ## 페이지네이션:
     * - 커서 기반 페이지네이션. limit + 1개를 조회하여 다음 페이지 존재 여부 판단.
     *
     * @param userId 조회 대상 사용자 ID
     * @param cursorScore 이전 페이지 마지막 항목의 점수 (초기 조회 시 null)
     * @param cursorUkId 이전 페이지 마지막 항목의 user_keyword ID (초기 조회 시 null)
     * @param limit 조회할 항목 개수
     * @param similarPoolLimit 카테고리 일치 글 최대 수집 개수 (기본값: 20)
     * @param fallbackPoolLimit 폴백 풀 최대 수집 개수 (기본값: 100)
     *
     * @return 추천 피드 항목 리스트
     */
    suspend fun getFeeds(
        userId: UserId,
        cursorScore: Double?,
        cursorUkId: UserKeywordId?,
        limit: Int,
        similarPoolLimit: Int = 500,
        fallbackPoolLimit: Int = 100,
    ): List<Feed>

    /**
     * # 순수 최신순 폴백 피드 조회
     *
     * 관심 키워드가 없거나 similar_pool만으로 피드를 구성하기 어려울 때 사용하는 폴백 전용 쿼리.
     * 유사도 계산 없이 최신 활성 키워드 글을 단순 최신순으로 반환한다.
     *
     * - 자신의 글, 차단 사용자의 글 제외
     * - similarity, final_score는 0.0으로 고정
     * - 커서는 사용자 키워드 ID([cursorUkId]) 단일 값 사용
     *
     * @param userId 조회 대상 사용자 ID
     * @param cursorUkId 이전 페이지 마지막 항목의 user_keyword ID (초기 조회 시 null)
     * @param limit 조회할 항목 개수
     *
     * @return 최신순 피드 항목 리스트
     */
    suspend fun getFallbackFeeds(
        userId: UserId,
        cursorUkId: UserKeywordId?,
        limit: Int,
    ): List<Feed>
}
