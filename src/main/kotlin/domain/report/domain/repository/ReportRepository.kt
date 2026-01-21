package com.peekr.domain.report.domain.repository

import com.peekr.domain.report.domain.model.ReportDetail
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
     * @return [ReportReason]를 반환한다.
     */
    suspend fun createReportReason(
        code: String,
        description: String,
    ): ReportReason

    /**
     * 신고 생성
     *
     * @param reportDetail 신고 디테일 도메인 모델
     */
    suspend fun createReport(reportDetail: ReportDetail)
}
