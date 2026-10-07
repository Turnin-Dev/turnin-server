package com.turnin.domain.contentReport.domain.model

import com.turnin.common.model.ContentReportType
import com.turnin.common.model.id.ReportReasonId
import com.turnin.common.model.id.UserId
import com.turnin.domain.contentReport.exception.ContentReportException

/**
 * 콘텐츠 신고 디테일 모델
 *
 * @property reporterId 신고자 ID
 * @property reportedUserId 피신고자(콘텐츠 작성자) ID
 * @property contentType 신고 대상 콘텐츠 유형
 * @property contentId 신고 대상 콘텐츠 ID ([contentType]에 따라 참조 대상이 다르다)
 * @property contentSnapshot 신고 시점의 콘텐츠 내용
 * @property reasonId 신고 사유 ID
 * @property customReason 기타 신고 사유
 */
data class ContentReportDetail(
    val reporterId: UserId,
    val reportedUserId: UserId,
    val contentType: ContentReportType,
    val contentId: Long,
    val contentSnapshot: String,
    val reasonId: ReportReasonId,
    val customReason: String?,
) {
    companion object {
        /**
         * @throws ContentReportException.CannotReportOwnContent 본인이 작성한 콘텐츠를 신고하려는 경우
         */
        fun create(
            reporterId: UserId,
            reportedContent: ReportableContent,
            contentType: ContentReportType,
            contentId: Long,
            reasonId: ReportReasonId,
            customReason: String? = null,
        ): ContentReportDetail {
            if (reporterId == reportedContent.authorId) {
                throw ContentReportException.CannotReportOwnContent()
            }

            return ContentReportDetail(
                reporterId = reporterId,
                reportedUserId = reportedContent.authorId,
                contentType = contentType,
                contentId = contentId,
                contentSnapshot = reportedContent.snapshot,
                reasonId = reasonId,
                customReason = customReason,
            )
        }
    }
}
