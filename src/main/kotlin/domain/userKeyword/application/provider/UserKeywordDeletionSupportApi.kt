package com.turnin.domain.userKeyword.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import java.time.Instant

/**
 * 외부에 제공할 UserKeyword 삭제 제공 API
 */
class UserKeywordDeletionSupportApi(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * 사용자의 모든 사용자 키워드를 Soft Delete 한다. (비활성화 + 삭제 시각 기록)
     *
     * ###### 해당 메서드는 [userId]의 모든 데이터를 지우므로 주의해서 사용해야 한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun softDeleteAll(userId: UserId) =
        userKeywordRepository.softDeleteAll(userId)

    /**
     * 사용자의 키워드를 전부 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteByUserId(userId: UserId) =
        userKeywordRepository.deleteByUserId(userId)

    /**
     * 사용자의 키워드 중 신고 내역이 없는 키워드를 전부 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteUnreportedByUserId(userId: UserId) =
        userKeywordRepository.deleteUnreportedByUserId(userId)

    /**
     * 삭제 시각이 [deletedBefore] 이전인 사용자 키워드 ID를 ID 오름차순으로 조회한다.
     *
     * @param deletedBefore 삭제 시각 기준
     * @param limit 조회할 최대 개수
     * @param afterId 이 ID보다 큰 키워드부터 조회, `null`이면 처음부터 조회
     */
    suspend fun findIdsDeletedBefore(
        deletedBefore: Instant,
        limit: Int,
        afterId: Long?,
    ): List<Long> = userKeywordRepository.findIdsDeletedBefore(deletedBefore, limit, afterId)

    /**
     * 사용자 키워드를 ID로 일괄 삭제한다.
     *
     * ###### 신고 내역이 남아있는 키워드는 FK 제약으로 삭제할 수 없으므로, 신고 내역을 먼저 삭제해야 한다.
     *
     * @param userKeywordIds 삭제할 사용자 키워드 ID 목록
     */
    suspend fun deleteByIds(userKeywordIds: List<Long>) =
        userKeywordRepository.deleteByIds(userKeywordIds)
}
