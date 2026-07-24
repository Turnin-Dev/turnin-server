package com.turnin.domain.feed.domain.repository

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.feed.domain.model.FeedWindowResult

/**
 * 피드 리포지토리
 */
interface FeedRepository {
    /**
     * # 친구 피드 조회 (청크 셔플)
     *
     * (비활성화 사용자의 키워드 제외, 사용자 탈퇴 시 사용자 키워드도 비활성화 되므로 사용자 키워드의 is_active 필드만 체크함)
     *
     * ## 알고리즘:
     * - 친구가 작성한 활성 키워드 글만 대상으로 함
     * - sessionMaxId(스크롤 세션 시작 시점의 최신 uk_id 스냅샷)보다 크게 작성된 글은 조회 대상에서 제외하여,
     *   세션 도중 새 글이 올라와도 이미 계산된 청크 순서가 흔들리지 않도록 함
     * - 최신 uk_id 기준으로 windowSize개씩 "청크" 단위로 잘라서 조회 (최신 청크 → 과거 청크 순으로 소진)
     * - 청크 내부에서만 seed 기반 해시값으로 순서를 섞음 (청크 간 시간 순서는 항상 보장됨)
     * - 차단한 사용자의 글 필터링
     *
     * ## 쿼리 구조 (CTE):
     *
     * ### friends
     * 양방향 친구 관계를 정규화하여 친구 ID 목록 추출.
     *
     * ### blocked_users
     * 차단/피차단 사용자 ID 목록. MATERIALIZED로 한 번만 계산.
     *
     * ### params
     * sessionMaxId 확정. 커서에 값이 있으면 그대로 쓰고, 첫 조회(null)면 현재 MAX(uk_id)를 계산해
     * 이후 페이지에 고정 전달될 값을 만듦.
     *
     * ### window_pool
     * windowAnchorId보다 작고 sessionMaxId 이하인 uk_id 중 친구가 작성한 활성 글을
     * uk_id 내림차순으로 최대 windowSize개 조회.
     * 인덱스를 활용한 조기 종료(Index Scan Backward + Limit)가 가능하도록 정렬 기준을 uk_id로 고정.
     *
     * ### shuffled
     * window_pool(청크 전체)에 대해 hashtext(seed + uk_id) 기반 shuffle_key를 계산하고,
     * window_min_uk_id(다음 청크 anchor) / window_fetched_count(청크 전체 크기)를 윈도우 함수로 함께 산출.
     * 커서 필터링보다 먼저(하위 CTE에서) 계산해 두어야 이 값들이 페이지 단위가 아닌 청크 전체 기준을 유지함.
     *
     * ### 최종 SELECT
     * shuffled 결과에서 (shuffle_key, uk_id) 튜플이 커서(lastShuffleKey, lastUkId)보다 큰 행만 남겨
     * shuffle_key, uk_id 순으로 정렬 후 limit만큼 반환.
     * offset이 아닌 값 기반 커서라 청크 내부 멤버십이 바뀌어도(비활성화, 차단 등) 중복 조회가 발생하지 않음.
     *
     * ## 페이지네이션:
     * - 청크(windowSize) 단위 커서 + 청크 내부 (shuffle_key, uk_id) 값 커서, 2단계 구조.
     * - offset은 사용하지 않음 (중간에 pool 멤버십이 바뀌면 offset이 밀리면서 중복/누락이 생기는 문제를 값 기반 커서로 회피).
     * - 상위 계층(UseCase)에서 반환된 행 수가 limit보다 적으면 현재 청크가 소진된 것으로 보고
     *   windowAnchorId를 windowMinUkId로 옮기며 청크 내부 커서(lastShuffleKey, lastUkId)를 리셋함.
     *
     * @param userId 조회 대상 사용자 ID
     * @param seed 청크 내부 셔플에 사용되는 시드값. 하나의 스크롤 세션 동안 고정되어야 함
     * @param sessionMaxId 스크롤 세션 시작 시점의 최신 uk_id 스냅샷. 이 값보다 큰(= 세션 시작 이후 새로 작성된) 글은 조회에서 제외됨.
     *   초기 조회 시 null이며, 이 경우 서버가 현재 MAX(uk_id)를 계산해 결과에 실어 반환하므로 이후 요청부터는 그 값을 그대로 전달해야 함
     * @param windowAnchorId 현재 청크의 시작 기준이 되는 uk_id (이 값보다 작은 글부터 조회, 초기 조회 시 null)
     * @param lastShuffleKey 청크 내부 페이지네이션 커서: 마지막으로 반환된 행의 shuffle_key (초기 조회 시 null)
     * @param lastUkId 청크 내부 페이지네이션 커서: 마지막으로 반환된 행의 uk_id, shuffle_key 동점자 처리용 (초기 조회 시 null)
     * @param windowSize 한 번에 가져올 청크의 최대 크기
     * @param limit 조회할 항목 개수
     *
     * @return 친구 피드 청크 조회 결과 (항목 리스트 + 다음 청크/페이지 전환에 필요한 메타데이터)
     */
    suspend fun getFriendFeeds(
        userId: UserId,
        seed: String,
        sessionMaxId: Long?,
        windowAnchorId: UserKeywordId?,
        lastShuffleKey: Int?,
        lastUkId: Long?,
        windowSize: Int,
        limit: Int,
    ): FeedWindowResult

    /**
     * # 전체 피드 조회 (청크 셔플)
     *
     * (비활성화 사용자의 키워드 제외, 사용자 탈퇴 시 사용자 키워드도 비활성화 되므로 사용자 키워드의 is_active 필드만 체크함)
     *
     * ## 알고리즘:
     * - 친구 여부, 카테고리 관계없이 전체 활성 키워드 글을 대상으로 함
     * - sessionMaxId(스크롤 세션 시작 시점의 최신 uk_id 스냅샷)보다 크게 작성된 글은 조회 대상에서 제외하여,
     *   세션 도중 새 글이 올라와도 이미 계산된 청크 순서가 흔들리지 않도록 함
     * - 최신 uk_id 기준으로 windowSize개씩 "청크" 단위로 잘라서 조회 (최신 청크 → 과거 청크 순으로 소진)
     * - 청크 내부에서만 seed 기반 해시값으로 순서를 섞음 (청크 간 시간 순서는 항상 보장됨)
     * - 자신의 글, 차단한 사용자의 글 필터링
     *
     * ## 쿼리 구조 (CTE):
     *
     * ### blocked_users
     * 차단/피차단 사용자 ID 목록. MATERIALIZED로 한 번만 계산.
     *
     * ### params
     * sessionMaxId 확정. 커서에 값이 있으면 그대로 쓰고, 첫 조회(null)면 현재 MAX(uk_id)를 계산해
     * 이후 페이지에 고정 전달될 값을 만듦.
     *
     * ### window_pool
     * windowAnchorId보다 작고 sessionMaxId 이하인 uk_id 중 (자신 제외) 활성 글을
     * uk_id 내림차순으로 최대 windowSize개 조회.
     * 인덱스를 활용한 조기 종료(Index Scan Backward + Limit)가 가능하도록 정렬 기준을 uk_id로 고정.
     *
     * ### shuffled
     * window_pool(청크 전체)에 대해 hashtext(seed + uk_id) 기반 shuffle_key를 계산하고,
     * window_min_uk_id(다음 청크 anchor) / window_fetched_count(청크 전체 크기)를 윈도우 함수로 함께 산출.
     * 커서 필터링보다 먼저(하위 CTE에서) 계산해 두어야 이 값들이 페이지 단위가 아닌 청크 전체 기준을 유지함.
     *
     * ### 최종 SELECT
     * shuffled 결과에서 (shuffle_key, uk_id) 튜플이 커서(lastShuffleKey, lastUkId)보다 큰 행만 남겨
     * shuffle_key, uk_id 순으로 정렬 후 limit만큼 반환.
     * offset이 아닌 값 기반 커서라 청크 내부 멤버십이 바뀌어도(비활성화, 차단 등) 중복 조회가 발생하지 않음.
     *
     * ## 페이지네이션:
     * - 청크(windowSize) 단위 커서 + 청크 내부 (shuffle_key, uk_id) 값 커서, 2단계 구조.
     * - 상위 계층(UseCase)에서 반환된 행 수가 limit보다 적으면 현재 청크가 소진된 것으로 보고
     *   windowAnchorId를 windowMinUkId로 옮기며 청크 내부 커서(lastShuffleKey, lastUkId)를 리셋함.
     *
     * @param userId 조회 대상 사용자 ID
     * @param seed 청크 내부 셔플에 사용되는 시드값. 하나의 스크롤 세션 동안 고정되어야 함
     * @param sessionMaxId 스크롤 세션 시작 시점의 최신 uk_id 스냅샷. 이 값보다 큰(= 세션 시작 이후 새로 작성된) 글은 조회에서 제외됨.
     *   초기 조회 시 null이며, 이 경우 서버가 현재 MAX(uk_id)를 계산해 결과에 실어 반환하므로 이후 요청부터는 그 값을 그대로 전달해야 함
     * @param windowAnchorId 현재 청크의 시작 기준이 되는 uk_id (이 값보다 작은 글부터 조회, 초기 조회 시 null)
     * @param lastShuffleKey 청크 내부 페이지네이션 커서: 마지막으로 반환된 행의 shuffle_key (초기 조회 시 null)
     * @param lastUkId 청크 내부 페이지네이션 커서: 마지막으로 반환된 행의 uk_id, shuffle_key 동점자 처리용 (초기 조회 시 null)
     * @param windowSize 한 번에 가져올 청크의 최대 크기
     * @param limit 조회할 항목 개수
     *
     * @return 전체 피드 청크 조회 결과 (항목 리스트 + 다음 청크/페이지 전환에 필요한 메타데이터)
     */
    suspend fun getAllFeeds(
        userId: UserId,
        seed: String,
        sessionMaxId: Long?,
        windowAnchorId: UserKeywordId?,
        lastShuffleKey: Int?,
        lastUkId: Long?,
        windowSize: Int,
        limit: Int,
    ): FeedWindowResult
}
