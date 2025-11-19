package com.peekr.domain.report.domain.repository

import com.peekr.common.db.schema.ReportReasonId
import com.peekr.common.model.UserId
import com.peekr.domain.report.domain.model.ReportReason

interface ReportRepository {
    /**
     * 신고 사유 목록 조회
     */
    suspend fun getReportReasons(): List<ReportReason>

    /**
     * 신고 사유 생성
     *
     * @param reason 신고 사유
     *
     * @return [ReportReason] 신고 사유
     */
    suspend fun createReportReason(reason: ReportReason): ReportReason?

    /**
     * 신고 생성
     *
     * @param reporterId 신고자 ID
     * @param reportedId 피신고자 ID
     * @param reasonId 신고 사유 ID
     * @param customReason 기타 신고 사유
     *
     * @return 신고 생성에 성공하면 `true`, 실패하면 `false`를 반환한다.
     */
    suspend fun createReport(
        reporterId: UserId,
        reportedId: UserId,
        reasonId: ReportReasonId,
        customReason: String? = null,
    ): Unit
}
