package com.turnin.domain.userKeyword.domain.repository

import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserKeyword
import com.turnin.domain.userKeyword.domain.model.UserKeywordDetail
import com.turnin.domain.userKeyword.domain.model.UserKeywordPatch
import java.time.Instant

interface UserKeywordRepository {
    /**
     * 사용자 키워드 ID로 사용자 키워드 조회 (비활성화 사용자 키워드 제외)
     *
     * @param userKeywordId 사용자 키워드 ID
     */
    suspend fun findById(userKeywordId: UserKeywordId): UserKeyword?

    /**
     * 사용자 ID를 통해 사용자별 키워드 리스트를 조회한다. (비활성화 사용자 키워드 제외)
     *
     * @param userId 사용자 ID
     */
    suspend fun findListByUserId(userId: UserId): List<UserKeyword>

    /**
     * 키워드 ID와 사용자 ID를 통해 사용자별 키워드를 찾는다. (비활성화 사용자 키워드 제외)
     *
     * @param keywordId 키워드 ID
     * @param userId 사용자 ID
     *
     * @return 사용자별 키워드를 찾으면 [UserKeyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findByKeywordIdAndUserId(keywordId: KeywordId, userId: UserId): UserKeyword?

    /**
     * 사용자 키워드 ID로 사용자 키워드 상세 정보를 조회한다.
     * (차단된 사용자, 비활성화 사용자/사용자 키워드 제외)
     *
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return [UserKeywordDetail]
     */
    suspend fun getDetailById(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
    ): UserKeywordDetail?

    /**
     * 사용자 ID로 사용자의 키워드 상세 정보 리스트를 조회한다.
     * (차단된 사용자, 비활성화 사용자/사용자 키워드 제외)
     *
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userId 사용자 ID
     */
    suspend fun getDetailsByUserId(
        currentUserId: UserId,
        userId: UserId,
    ): List<UserKeywordDetail>

    /**
     * 사용자 키워드 ID를 통해 사용자 키워드 설명을 조회한다. (비활성화 사용자 키워드 제외)
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return 사용자별 키워드를 찾으면 [Description]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findDescriptionById(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Description?

    /**
     * 사용자 키워드 개수 카운트 (비활성화 사용자 키워드 제외)
     *
     * @param userId 사용자 ID
     */
    suspend fun countByUserId(userId: UserId): Long

    /**
     * 사용자별 키워드를 생성한다.
     *
     * @param keywordId 키워드 ID
     * @param userId 사용자 ID
     * @param description 키워드 개인 설명
     *
     * @return 생성된 [UserKeyword]를 반환한다.
     */
    suspend fun create(
        keywordId: KeywordId,
        userId: UserId,
        description: Description,
    ): UserKeyword

    /**
     * 사용자 키워드를 업데이트한다.
     *
     * @param ownerId 사용자 ID
     * @param patch [UserKeywordPatch]
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun update(
        ownerId: UserId,
        patch: UserKeywordPatch,
    ): Boolean

    /**
     * 사용자 키워드를 삭제한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 삭제 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordId): Boolean

    /**
     * 사용자의 키워드를 전부 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteByUserId(userId: UserId)

    /**
     * 사용자의 키워드 중 신고 내역이 없는 키워드를 전부 삭제한다.
     *
     * 신고 내역이 있는 키워드는 신고 데이터와의 연계를 위해 남겨둔다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteUnreportedByUserId(userId: UserId)

    /**
     * 사용자 키워드를 비활성화한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 비활성화 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun deactivate(ownerId: UserId, userKeywordId: UserKeywordId): Boolean

    /**
     * 작성자가 삭제한 사용자 키워드를 Soft Delete 한다. (비활성화 + 삭제 시각 기록)
     *
     * 신고 내역 때문에 삭제할 수 없는 키워드에 사용하며, 삭제 시각 기준 1년 후 파기된다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return Soft Delete 성공 시 `true`, 대상이 없으면 `false`를 반환한다.
     */
    suspend fun softDelete(ownerId: UserId, userKeywordId: UserKeywordId): Boolean

    /**
     * 사용자의 모든 사용자 키워드를 Soft Delete 한다. (비활성화 + 삭제 시각 기록)
     *
     * 이미 삭제 시각이 기록된 키워드는 기존 삭제 시각을 유지한다.
     *
     * ###### 해당 메서드는 [userId]의 모든 데이터를 지우므로 주의해서 사용해야 한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun softDeleteAll(userId: UserId)

    /**
     * 삭제 시각이 [deletedBefore] 이전인 사용자 키워드 ID를 ID 오름차순으로 조회한다.
     *
     * @param deletedBefore 삭제 시각 기준 (이 시각 이전에 삭제된 키워드만 조회)
     * @param limit 조회할 최대 개수
     * @param afterId 이 ID보다 큰 키워드부터 조회, `null`이면 처음부터 조회
     *
     * @return 사용자 키워드 ID 목록
     */
    suspend fun findIdsDeletedBefore(
        deletedBefore: Instant,
        limit: Int,
        afterId: Long?,
    ): List<Long>

    /**
     * 사용자 키워드를 ID로 일괄 삭제한다.
     *
     * @param userKeywordIds 삭제할 사용자 키워드 ID 목록
     */
    suspend fun deleteByIds(userKeywordIds: List<Long>)
}
