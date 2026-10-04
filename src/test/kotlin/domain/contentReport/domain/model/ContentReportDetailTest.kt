package com.turnin.domain.contentReport.domain.model

import com.turnin.common.model.ContentReportType
import com.turnin.common.model.id.ReportReasonId
import com.turnin.common.model.id.UserId
import com.turnin.domain.contentReport.exception.ContentReportException
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class ContentReportDetailTest {
    @Test
    fun `다른 사용자의 콘텐츠를 신고하면 작성자를 피신고자로, 콘텐츠 내용을 스냅샷으로 생성한다`() {
        // given
        val reporterId = UserId(1L)
        val reportedContent = ReportableContent(authorId = UserId(2L), snapshot = "질문 내용")

        // when
        val result = ContentReportDetail.create(
            reporterId = reporterId,
            reportedContent = reportedContent,
            contentType = ContentReportType.PING_PONG_QUESTION,
            contentId = 10L,
            reasonId = ReportReasonId(1L),
            customReason = "기타 사유",
        )

        // then
        val expected = ContentReportDetail(
            reporterId = UserId(1L),
            reportedUserId = UserId(2L),
            contentType = ContentReportType.PING_PONG_QUESTION,
            contentId = 10L,
            contentSnapshot = "질문 내용",
            reasonId = ReportReasonId(1L),
            customReason = "기타 사유",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `본인이 작성한 콘텐츠를 신고하면 본인 콘텐츠 신고 예외가 발생한다`() {
        // given
        val reporterId = UserId(1L)
        val ownContent = ReportableContent(authorId = UserId(1L), snapshot = "질문 내용")

        // when, then
        assertThrows<ContentReportException.CannotReportOwnContent> {
            ContentReportDetail.create(
                reporterId = reporterId,
                reportedContent = ownContent,
                contentType = ContentReportType.PING_PONG_QUESTION,
                contentId = 10L,
                reasonId = ReportReasonId(1L),
            )
        }
    }
}
