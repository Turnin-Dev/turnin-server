package com.turnin.domain.contentReport.application.usecase

import com.turnin.common.db.DatabaseException
import com.turnin.common.model.ContentReportType
import com.turnin.common.model.id.ReportReasonId
import com.turnin.common.model.id.ReportReasonIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.domain.contentReport.application.dto.ContentReportDto
import com.turnin.domain.contentReport.domain.model.ContentReportDetail
import com.turnin.domain.contentReport.domain.model.ReportableContent
import com.turnin.domain.contentReport.domain.provider.ReportableContentProvider
import com.turnin.domain.contentReport.domain.repository.ContentReportRepository
import com.turnin.domain.contentReport.exception.ContentReportException
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class CreateContentReportUseCaseTest {
    private val contentReportRepository = mockk<ContentReportRepository>()
    private val reportableContentProvider = mockk<ReportableContentProvider>()
    private val usecase = CreateContentReportUseCase(contentReportRepository, reportableContentProvider)

    @Before
    fun setUp() {
        // 유스케이스가 직접 트랜잭션을 열기 때문에 DB 연결이 필요하다.
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
        clearAllMocks()
    }

    @Test
    fun `신고 시 콘텐츠 작성자를 피신고자로, 콘텐츠 내용을 스냅샷으로 저장한다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_ANSWER, 20L)
        } returns ReportableContent(authorId = UserId(2L), snapshot = "답변 내용")
        val savedDetail = slot<ContentReportDetail>()
        coEvery { contentReportRepository.create(capture(savedDetail)) } just Runs
        coEvery { contentReportRepository.countByContent(ContentReportType.PING_PONG_ANSWER, 20L) } returns 1L

        // when
        usecase(
            UserId(1L),
            ContentReportDto(
                contentType = ContentReportType.PING_PONG_ANSWER,
                contentId = 20L,
                reasonId = 3L,
                customReason = "기타 사유",
            ),
        )

        // then
        val expected = ContentReportDetail(
            reporterId = UserId(1L),
            reportedUserId = UserId(2L),
            contentType = ContentReportType.PING_PONG_ANSWER,
            contentId = 20L,
            contentSnapshot = "답변 내용",
            reasonId = ReportReasonId(3L),
            customReason = "기타 사유",
        )
        assertEquals(expected, savedDetail.captured)
    }

    @Test
    fun `신고 누적 횟수가 5회가 되면 콘텐츠를 숨김 처리한다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)
        } returns TestReportableContent
        coEvery { contentReportRepository.create(any()) } just Runs
        coEvery { contentReportRepository.countByContent(ContentReportType.PING_PONG_QUESTION, 10L) } returns 5L
        coEvery { reportableContentProvider.hide(ContentReportType.PING_PONG_QUESTION, 10L) } just Runs

        // when
        usecase(UserId(1L), TestContentReportDto)

        // then
        coVerify(exactly = 1) { reportableContentProvider.hide(ContentReportType.PING_PONG_QUESTION, 10L) }
    }

    @Test
    fun `신고 누적 횟수가 4회이면 콘텐츠를 숨김 처리하지 않는다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)
        } returns TestReportableContent
        coEvery { contentReportRepository.create(any()) } just Runs
        coEvery { contentReportRepository.countByContent(ContentReportType.PING_PONG_QUESTION, 10L) } returns 4L

        // when
        usecase(UserId(1L), TestContentReportDto)

        // then
        coVerify(exactly = 0) { reportableContentProvider.hide(any(), any()) }
    }

    @Test
    fun `신고할 콘텐츠가 없으면 콘텐츠 없음 예외가 발생하고 신고를 저장하지 않는다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)
        } returns null

        // when, then
        assertThrows<ContentReportException.ContentNotFound> {
            usecase(UserId(1L), TestContentReportDto)
        }
        coVerify(exactly = 0) { contentReportRepository.create(any()) }
    }

    @Test
    fun `본인이 작성한 콘텐츠를 신고하면 본인 콘텐츠 신고 예외가 발생하고 신고를 저장하지 않는다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)
        } returns ReportableContent(authorId = UserId(1L), snapshot = "질문 내용")

        // when, then
        assertThrows<ContentReportException.CannotReportOwnContent> {
            usecase(UserId(1L), TestContentReportDto)
        }
        coVerify(exactly = 0) { contentReportRepository.create(any()) }
    }

    @Test
    fun `이미 신고한 콘텐츠이면 중복 신고 예외가 발생하고 숨김 처리하지 않는다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)
        } returns TestReportableContent
        coEvery {
            contentReportRepository.create(any())
        } throws DatabaseException.DuplicatedDataException(SQLException("duplicate key"))

        // when, then
        assertThrows<ContentReportException.AlreadyReported> {
            usecase(UserId(1L), TestContentReportDto)
        }
        coVerify(exactly = 0) { reportableContentProvider.hide(any(), any()) }
    }

    @Test
    fun `존재하지 않는 신고 사유이면 신고 사유 오류 예외가 발생한다`() = runTest {
        // given
        coEvery {
            reportableContentProvider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)
        } returns TestReportableContent
        coEvery {
            contentReportRepository.create(any())
        } throws DatabaseException.ForeignKeyViolationException(SQLException("foreign key constraint"))

        // when, then
        assertThrows<ContentReportException.InvalidReportReason> {
            usecase(UserId(1L), TestContentReportDto)
        }
    }

    @Test
    fun `신고 사유 ID가 0 이하이면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val invalidReasonDto = TestContentReportDto.copy(reasonId = 0L)

        // when, then
        assertThrows<ReportReasonIdValidationException> {
            usecase(UserId(1L), invalidReasonDto)
        }
    }

    companion object {
        private val TestReportableContent = ReportableContent(authorId = UserId(2L), snapshot = "질문 내용")
        private val TestContentReportDto = ContentReportDto(
            contentType = ContentReportType.PING_PONG_QUESTION,
            contentId = 10L,
            reasonId = 1L,
        )
    }
}
