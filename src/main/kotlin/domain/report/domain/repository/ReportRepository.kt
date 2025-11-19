package com.peekr.domain.report.domain.repository

import com.peekr.domain.report.domain.model.Report
import com.peekr.domain.report.domain.model.ReportReason

interface ReportRepository {
    /**
     * 신고 사유 목록 조회
     */
    suspend fun getReportReasons(): List<ReportReason>

    /**
     * 신고 사유 생성
     *
     * @param code 신고 사유 코드
     * @param description 신고 사유 설명
     *
     * @return [ReportReason] 신고 사유
     */
    suspend fun createReportReason(
        code: String,
        description: String,
    ): ReportReason?

    /**
     * 신고 생성
     *
     * @param report 신고 도메인 모델
     *
     * @return 신고 생성에 성공하면 `true`, 실패하면 `false`를 반환한다.
     */
    suspend fun createReport(report: Report)
}
