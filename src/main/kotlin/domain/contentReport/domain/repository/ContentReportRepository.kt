package com.turnin.domain.contentReport.domain.repository

import com.turnin.common.db.DatabaseException.DuplicatedDataException
import com.turnin.common.db.DatabaseException.ForeignKeyViolationException
import com.turnin.common.model.ContentReportType
import com.turnin.domain.contentReport.domain.model.ContentReportDetail

interface ContentReportRepository {
    /**
     * 콘텐츠 신고를 생성한다.
     *
     * @param contentReportDetail 콘텐츠 신고 디테일
     *
     * @exception DuplicatedDataException 동일 신고자가 동일 콘텐츠를 이미 신고한 경우
     * @exception ForeignKeyViolationException 존재하지 않는 신고 사유인 경우
     */
    suspend fun create(contentReportDetail: ContentReportDetail)

    /**
     * 콘텐츠의 신고 누적 횟수를 조회한다.
     *
     * @param contentType 콘텐츠 유형
     * @param contentId 콘텐츠 ID
     */
    suspend fun countByContent(
        contentType: ContentReportType,
        contentId: Long,
    ): Long
}
