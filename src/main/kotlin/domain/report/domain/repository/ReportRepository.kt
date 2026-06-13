package com.turnin.domain.report.domain.repository

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.report.domain.model.ReportDetail
import com.turnin.domain.report.domain.model.ReportReason

interface ReportRepository {
    /**
     * 사용자 키워드 ID가 신고 내역에 존재하는지 확인한다.
     *
     * @param userKeywordId 사용자 키워드 ID
     */
    suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean

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

    /**
     * 신고 삭제
     *
     * 사용자의 모든 신고 관계를 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteByUserId(userId: UserId)
}
