package com.turnin.domain.discover.domain.repository

import com.turnin.common.model.id.UserId
import com.turnin.domain.discover.domain.model.DiscoverCursor
import com.turnin.domain.discover.domain.model.SharedUserKeyword

/**
 * 탐색 결과 항목 (사용자 ID + 다음 페이지 커서 계산용 정렬 기준값)
 */
data class DiscoveredResult(
    val userId: UserId,
    val matchScore: Double,
    val shuffleKey: Int,
    val scoreChunk: Int,
)

/**
 * 탐색 리포지토리
 */
interface DiscoverRepository {
    /**
     * 나(또는 지정된 사용자)의 최근 키워드와 임베딩 코사인 유사도가 높은 키워드를
     * 보유한 사용자 ID 목록을 페이지네이션을 통해 조회한다.
     *
     * (사용자 탈퇴 시 사용자 키워드도 비활성화 되므로 사용자 키워드의 is_active 필드만 체크함)
     *
     * ### 쿼리 구조
     * 1. my_keywords: 대상 사용자의 최근 활성 키워드 최대 5개 + 임베딩 값 조회
     * 2. similar_keywords: my_keywords 각각에 대해 HNSW(pgvector) 기반 최근접 15개 후보 키워드를
     *    탐색하고, 유사도가 [similarityThreshold] 이상인 것만 채택
     * 3. candidate_scores: 유사 키워드를 보유한 사용자별로 최고 유사도(MAX)를 매칭 점수로 산출.
     *    동점자 처리를 위한 시드 기반 셔플 키도 함께 계산.
     *    [snapshotAt] 이후 변경된(updated_at 기준) 사용자 키워드는 제외하여, 세션 내
     *    페이지 이동 중 후보 풀 구성이 흔들리지 않도록 고정한다
     * 4. scored_chunks: candidate_scores의 match_score를 기준으로 NTILE(5)를 적용해
     *    상위(1)~하위(5) 5개 구간으로 균등 분할. 절대 점수 대신 상대적 순위 구간으로 노출해
     *    특정 인원에게 결과가 편중되는 것을 완화한다
     * 5. blocked_users: 차단 관계(양방향) 사용자 제외
     *
     * ### 정렬 및 페이지네이션
     * - 정렬: score_chunk ASC, shuffle_key DESC, user_id DESC
     *   (구간이 낮을수록/앞설수록 상위이므로 score_chunk만 오름차순, 나머지는 3중 정렬로 동점자까지 완전한 순서 보장)
     * - 커서: 이전 페이지 마지막 항목의 (score_chunk, shuffle_key, user_id) 3개 값 (초기 호출 시 `null`)
     *
     * ### 스냅샷 필터에 대하여
     * NTILE은 매 쿼리 실행 시점의 후보 전체를 기준으로 구간을 다시 계산하는 함수이므로,
     * [snapshotAt]으로 후보 풀(candidate_scores 대상 user_keyword 집합)을 첫 페이지 조회
     * 시점 기준으로 고정하지 않으면 페이지 이동 중 동일 사용자의 score_chunk가 바뀌어
     * 결과 중복/누락이 발생할 수 있다. 최초 호출 시 [snapshotAt]을 현재 시각으로 발급하고,
     * 이후 페이지 호출부터는 최초 발급값을 그대로 재사용해야 한다 (호출부 책임)
     *
     * #### 해당 쿼리는 초기 서비스 단계에서만 유효하다. 후보 규모가 커지면 인덱스/쿼리 재검토가 필요하다.
     *
     * @param targetUserId 매칭 기준이 되는 사용자 ID (재탐색 시 다른 사용자 ID로 대체 가능)
     * @param viewerUserId 현재 로그인한 사용자 ID (결과에서 본인 제외용, 없으면 null)
     * @param seed 셔플 키 생성을 위한 시드값
     * @param similarityThreshold 후보로 채택할 최소 코사인 유사도
     * @param snapshotAt 후보 풀 고정 기준 시각(epoch millis). 이 시각 이후 변경된(updated_at)
     *   사용자 키워드는 후보에서 제외한다. 최초 페이지 호출 시 발급하고 이후 페이지는 재사용해야 한다
     * @param cursor 이전 페이지의 마지막 항목 커서 (초기 호출 시 null)
     * @param pageSize 페이지 크기
     */
    suspend fun findUserIdsWithSimilarKeywords(
        targetUserId: UserId,
        viewerUserId: UserId?,
        seed: String,
        similarityThreshold: Double,
        snapshotAt: Long,
        cursor: DiscoverCursor?,
        pageSize: Int,
    ): List<DiscoveredResult>

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
