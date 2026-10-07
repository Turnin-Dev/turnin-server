package com.turnin.domain.report.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.report.domain.repository.ReportRepository

/**
 * 외부에 제공할 Report 삭제 제공 API
 */
class ReportDeletionSupportApi(private val reportRepository: ReportRepository) {
    /**
     * 신고 삭제
     *
     * 사용자의 모든 신고 관계를 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteByUserId(userId: UserId) =
        reportRepository.deleteByUserId(userId)

    /**
     * 사용자 키워드를 대상으로 한 신고를 삭제한다. (사용자 신고가 함께 담긴 신고 행도 통째로 삭제)
     *
     * @param userKeywordIds 사용자 키워드 ID 목록
     */
    suspend fun deleteByUserKeywordIds(userKeywordIds: List<Long>) =
        reportRepository.deleteByUserKeywordIds(userKeywordIds)
}
