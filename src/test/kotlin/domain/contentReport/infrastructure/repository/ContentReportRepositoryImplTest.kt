package com.turnin.domain.contentReport.infrastructure.repository

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.ContentReports
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.model.ContentReportType
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.ReportReasonId
import com.turnin.common.model.id.UserId
import com.turnin.domain.contentReport.domain.model.ContentReportDetail
import com.turnin.util.db.PostgresRule
import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.selectAll
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows

/**
 * `content_type`이 PostgreSQL enum 타입이므로, enum 바인딩/비교를 실제 PostgreSQL에서 검증한다.
 */
class ContentReportRepositoryImplTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val repository = ContentReportRepositoryImpl()

    @Test
    fun `콘텐츠 신고 생성 시 입력한 값으로 신고가 저장된다`() = runTest {
        // given
        val reporterId = insertUserAndReturnId("1")
        val reportedUserId = insertUserAndReturnId("2")
        val detail = contentReportDetail(
            reporterId = reporterId,
            reportedUserId = reportedUserId,
            contentType = ContentReportType.PING_PONG_ANSWER,
            contentId = 20L,
            contentSnapshot = "답변 내용",
            customReason = "기타 사유",
        )

        // when
        repository.create(detail)

        // then
        val savedRow = dbRule.dbQuery {
            ContentReports.selectAll().single()
        }
        assertEquals(reporterId.value, savedRow[ContentReports.reporterId].value)
        assertEquals(reportedUserId.value, savedRow[ContentReports.reportedUserId].value)
        assertEquals(ContentReportType.PING_PONG_ANSWER, savedRow[ContentReports.contentType])
        assertEquals(20L, savedRow[ContentReports.contentId])
        assertEquals("답변 내용", savedRow[ContentReports.contentSnapshot])
        assertEquals(1L, savedRow[ContentReports.reasonId].value)
        assertEquals("기타 사유", savedRow[ContentReports.customReason])
    }

    @Test
    fun `같은 신고자가 같은 콘텐츠를 다시 신고하면 중복 데이터 예외가 발생한다`() = runTest {
        // given
        val reporterId = insertUserAndReturnId("1")
        val reportedUserId = insertUserAndReturnId("2")
        repository.create(contentReportDetail(reporterId, reportedUserId))

        // when, then
        assertThrows<DatabaseException.DuplicatedDataException> {
            repository.create(contentReportDetail(reporterId, reportedUserId))
        }
    }

    @Test
    fun `존재하지 않는 신고 사유로 신고하면 외래키 제약 위반 예외가 발생한다`() = runTest {
        // given
        val reporterId = insertUserAndReturnId("1")
        val reportedUserId = insertUserAndReturnId("2")
        val detail = contentReportDetail(reporterId, reportedUserId, reasonId = ReportReasonId(999L))

        // when, then
        assertThrows<DatabaseException.ForeignKeyViolationException> {
            repository.create(detail)
        }
    }

    @Test
    fun `신고 수 조회 시 같은 콘텐츠의 신고 수를 반환한다`() = runTest {
        // given
        val reportedUserId = insertUserAndReturnId("0")
        repeat(3) { index ->
            val reporterId = insertUserAndReturnId("reporter$index")
            repository.create(contentReportDetail(reporterId, reportedUserId, contentId = 10L))
        }

        // when
        val result = repository.countByContent(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        assertEquals(3L, result)
    }

    @Test
    fun `신고 수 조회 시 다른 콘텐츠 ID의 신고는 세지 않는다`() = runTest {
        // given
        val reporterId = insertUserAndReturnId("1")
        val reportedUserId = insertUserAndReturnId("2")
        repository.create(contentReportDetail(reporterId, reportedUserId, contentId = 11L))

        // when
        val result = repository.countByContent(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        assertEquals(0L, result)
    }

    @Test
    fun `신고 수 조회 시 같은 ID라도 다른 콘텐츠 유형의 신고는 세지 않는다`() = runTest {
        // given
        val reporterId = insertUserAndReturnId("1")
        val reportedUserId = insertUserAndReturnId("2")
        repository.create(
            contentReportDetail(
                reporterId,
                reportedUserId,
                contentType = ContentReportType.PING_PONG_ANSWER,
                contentId = 10L,
            ),
        )

        // when
        val result = repository.countByContent(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        assertEquals(0L, result)
    }

    @Test
    fun `신고 수 조회 시 신고가 없으면 0을 반환한다`() = runTest {
        // given
        val notReportedContentId = 10L

        // when
        val result = repository.countByContent(ContentReportType.PING_PONG_QUESTION, notReportedContentId)

        // then
        assertEquals(0L, result)
    }

    private fun contentReportDetail(
        reporterId: UserId,
        reportedUserId: UserId,
        contentType: ContentReportType = ContentReportType.PING_PONG_QUESTION,
        contentId: Long = 10L,
        contentSnapshot: String = "질문 내용",
        // 기존 initData에서 생성된 신고 사유
        reasonId: ReportReasonId = ReportReasonId(1L),
        customReason: String? = null,
    ) = ContentReportDetail(
        reporterId = reporterId,
        reportedUserId = reportedUserId,
        contentType = contentType,
        contentId = contentId,
        contentSnapshot = contentSnapshot,
        reasonId = reasonId,
        customReason = customReason,
    )

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = dbRule.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "honggd"
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }

        UserId(savedUser.id.value)
    }
}
