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
}
