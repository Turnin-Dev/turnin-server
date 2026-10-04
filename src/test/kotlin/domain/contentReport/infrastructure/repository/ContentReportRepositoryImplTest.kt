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
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException

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

    @Test
    fun `같은 콘텐츠의 잠금을 다른 트랜잭션이 보유 중이면 잠금을 획득하지 못하고 대기한다`() = runTest {
        // given
        val result = newSuspendedTransaction {
            repository.lockContent(ContentReportType.PING_PONG_QUESTION, 10L)

            // when
            lockContentInOtherTransaction(ContentReportType.PING_PONG_QUESTION, 10L)
        }

        // then
        // 커넥션 오류 등 다른 DB 예외와 구분하기 위해, lock_timeout으로 인한 실패(SQLState 55P03)인지 확인한다.
        val rootCause = generateSequence(result.exceptionOrNull()) { it.cause }.last()
        assertIs<PSQLException>(rootCause)
        assertEquals("55P03", rootCause.sqlState)
    }

    @Test
    fun `다른 콘텐츠 ID의 잠금은 대기 없이 획득한다`() = runTest {
        // given
        val result = newSuspendedTransaction {
            repository.lockContent(ContentReportType.PING_PONG_QUESTION, 10L)

            // when
            lockContentInOtherTransaction(ContentReportType.PING_PONG_QUESTION, 11L)
        }

        // then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `같은 ID라도 다른 콘텐츠 유형의 잠금은 대기 없이 획득한다`() = runTest {
        // given
        val result = newSuspendedTransaction {
            repository.lockContent(ContentReportType.PING_PONG_QUESTION, 10L)

            // when
            lockContentInOtherTransaction(ContentReportType.PING_PONG_ANSWER, 10L)
        }

        // then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `잠금을 보유한 트랜잭션이 끝나면 같은 콘텐츠의 잠금을 획득한다`() = runTest {
        // given
        newSuspendedTransaction {
            repository.lockContent(ContentReportType.PING_PONG_QUESTION, 10L)
        }

        // when
        val result = lockContentInOtherTransaction(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        assertTrue(result.isSuccess)
    }

    /**
     * 별도 스레드의 새 트랜잭션(별도 커넥션)에서 잠금을 획득한다.
     *
     * 잠금 대기가 무한히 이어지지 않도록 `lock_timeout`을 걸어, 대기 시 예외로 끝나도록 한다.
     */
    private fun lockContentInOtherTransaction(
        contentType: ContentReportType,
        contentId: Long,
    ): Result<Unit> = CompletableFuture.supplyAsync {
        // newSuspendedTransaction 내부 예외는 부모 코루틴(runBlocking)까지 취소시키므로, runBlocking 바깥에서 잡는다.
        runCatching {
            runBlocking {
                newSuspendedTransaction {
                    exec("SET LOCAL lock_timeout = '100ms'")
                    repository.lockContent(contentType, contentId)
                }
            }
        }
    }.get()

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
