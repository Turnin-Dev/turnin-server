package com.turnin.domain.contentReport.application.usecase.integration

import com.turnin.common.db.schema.ContentReports
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.PingPongs
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.model.ContentReportType
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.contentReport.application.dto.ContentReportDto
import com.turnin.domain.contentReport.application.usecase.CreateContentReportUseCase
import com.turnin.domain.contentReport.exception.ContentReportException
import com.turnin.domain.contentReport.infrastructure.provider.ReportableContentProviderImpl
import com.turnin.domain.contentReport.infrastructure.repository.ContentReportRepositoryImpl
import com.turnin.domain.discover.util.TestVectorFixture
import com.turnin.domain.discover.util.TestVectorFixture.toPgVectorString
import com.turnin.domain.pingPong.application.provider.PingPongContentReportApi
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.infrastructure.repository.PingPongRepositoryImpl
import com.turnin.util.db.PostgresRule
import java.time.Instant
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.Rule
import org.junit.Test

/**
 * 실제 PostgreSQL에서 동시 신고 시 콘텐츠 단위 잠금으로 신고 처리가 직렬화되는지 검증한다.
 */
class CreateContentReportUseCaseIntegrationTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val contentReportRepository = ContentReportRepositoryImpl()
    private val pingPongRepository = PingPongRepositoryImpl()
    private val usecase = CreateContentReportUseCase(
        contentReportRepository,
        ReportableContentProviderImpl(PingPongContentReportApi(pingPongRepository)),
    )

    @Test
    fun `신고가 4건 쌓인 질문에 두 사용자가 동시에 신고하면 하나는 성공해 질문이 숨김 처리되고 다른 하나는 콘텐츠 없음 예외가 발생한다`() =
        runTest {
            // given
            val ownerId = insertUserAndReturnId("owner")
            val questionerId = insertUserAndReturnId("questioner")
            val userKeywordId = insertUserKeywordAndReturnId(ownerId)
            val pingPong = pingPongRepository.createQuestion(userKeywordId, questionerId, PingPongContent("질문 내용"))
            repeat(4) { index ->
                usecase(insertUserAndReturnId("reporter$index"), questionReportDto(pingPong.id))
            }
            val firstReporterId = insertUserAndReturnId("concurrent1")
            val secondReporterId = insertUserAndReturnId("concurrent2")

            // when
            // 테스트 트랜잭션이 잠금을 쥔 동안 두 신고를 시작해, 두 요청이 같은 시점에 경합하도록 만든다.
            val (firstResult, secondResult) = newSuspendedTransaction {
                contentReportRepository.lockContent(ContentReportType.PING_PONG_QUESTION, pingPong.id.value)
                val first = reportInOtherThread(firstReporterId, questionReportDto(pingPong.id))
                val second = reportInOtherThread(secondReporterId, questionReportDto(pingPong.id))
                Thread.sleep(500)
                first to second
            }.let { (first, second) -> first.get() to second.get() }

            // then
            val results = listOf(firstResult, secondResult)
            assertEquals(1, results.count { it.isSuccess })
            assertIs<ContentReportException.ContentNotFound>(results.single { it.isFailure }.exceptionOrNull())
        }

    @Test
    fun `두 사용자가 동시에 5번째 신고를 하면 신고는 5건만 저장되고 질문은 숨김 처리된다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("owner")
        val questionerId = insertUserAndReturnId("questioner")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId)
        val pingPong = pingPongRepository.createQuestion(userKeywordId, questionerId, PingPongContent("질문 내용"))
        repeat(4) { index ->
            usecase(insertUserAndReturnId("reporter$index"), questionReportDto(pingPong.id))
        }
        val firstReporterId = insertUserAndReturnId("concurrent1")
        val secondReporterId = insertUserAndReturnId("concurrent2")

        // when
        newSuspendedTransaction {
            contentReportRepository.lockContent(ContentReportType.PING_PONG_QUESTION, pingPong.id.value)
            val first = reportInOtherThread(firstReporterId, questionReportDto(pingPong.id))
            val second = reportInOtherThread(secondReporterId, questionReportDto(pingPong.id))
            Thread.sleep(500)
            first to second
        }.let { (first, second) -> first.get() to second.get() }

        // then
        val (reportCount, questionHiddenAt) = dbRule.dbQuery {
            ContentReports.selectAll().count() to
                PingPongs.selectAll().where { PingPongs.id eq pingPong.id.value }.single()[PingPongs.questionHiddenAt]
        }
        assertEquals(5L, reportCount)
        assertNotNull(questionHiddenAt)
    }

    private fun questionReportDto(pingPongId: PingPongId) = ContentReportDto(
        contentType = ContentReportType.PING_PONG_QUESTION,
        contentId = pingPongId.value,
        // 기존 initData에서 생성된 신고 사유
        reasonId = 1L,
    )

    /**
     * 별도 스레드(별도 트랜잭션, 별도 커넥션)에서 신고한다.
     */
    private fun reportInOtherThread(
        reporterId: UserId,
        contentReportDto: ContentReportDto,
    ): CompletableFuture<Result<Unit>> = CompletableFuture.supplyAsync {
        // newSuspendedTransaction 내부 예외는 부모 코루틴(runBlocking)까지 취소시키므로, runBlocking 바깥에서 잡는다.
        runCatching { runBlocking { usecase(reporterId, contentReportDto) } }
    }

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

    private suspend fun insertUserKeywordAndReturnId(ownerId: UserId): UserKeywordId = dbRule.dbQuery {
        val owner = UserEntity[ownerId.value]
        val keyword = KeywordEntity.new {
            this.keyword = "keyword"
            this.embedding = TestVectorFixture.orthogonalVector().toPgVectorString()
            this.createdBy = owner.id
        }
        val savedUserKeyword = UserKeywordEntity.new {
            this.userId = owner.id
            this.keywordId = keyword.id
            this.description = "description"
        }

        UserKeywordId(savedUserKeyword.id.value)
    }
}
