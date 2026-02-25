package com.peekr.domain.report.infrastructure.repository

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.ReportReasons
import com.peekr.common.db.schema.Reports
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.report.domain.model.ReportDetail
import com.peekr.util.db.TestDatabaseFactory
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.junit.After
import org.junit.Before
import org.junit.Test

class ReportRepositoryImplTest {
    private val repository = ReportRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `신고 사유 생성 성공 테스트`() = runTest {
        // when
        val reportReason = repository.createReportReason(
            code = TEST_REPORT_REASON_CODE,
            description = TEST_REPORT_REASON_DESCRIPTION,
        )

        // then
        assertNotNull(reportReason)
    }

    @Test
    fun `신고 사유 조회 성공 테스트`() = runTest {
        // given
        val expectedCount = 5
        repeat(expectedCount) {
            repository.createReportReason(
                code = TEST_REPORT_REASON_CODE + it,
                description = TEST_REPORT_REASON_DESCRIPTION + it,
            )
        }

        // when
        val reportReasons = repository.getReportReasons()

        // then
        assertEquals(expectedCount, reportReasons.size)
    }

    @Test
    fun `신고 사유가 없는 경우 빈 리스트를 반환한다`() = runTest {
        // when
        val reportReasons = repository.getReportReasons()

        // then
        assertTrue(reportReasons.isEmpty())
    }

    @Test
    fun `신고 생성 성공 테스트`() = runTest {
        // given
        val user1 = insertUserAndReturnId("1")
        val user2 = insertUserAndReturnId("2")
        val reportReason = repository.createReportReason(
            code = TEST_REPORT_REASON_CODE,
            description = TEST_REPORT_REASON_DESCRIPTION,
        )
        assertNotNull(reportReason)

        // when
        val exception = runCatching {
            repository.createReport(
                ReportDetail.create(
                    reporterId = user1,
                    reportedId = user2,
                    reportedUserKeywordId = null,
                    reasonId = reportReason.id,
                    customReason = TEST_CUSTOM_REASON,
                ),
            )
        }.exceptionOrNull()
        val report = TestDatabaseFactory.dbQuery {
            Reports.select(Reports.customReason).map { it[Reports.customReason] }
        }

        // then
        assertNull(exception)
        assertEquals(1, report.size)
        assertEquals(TEST_CUSTOM_REASON, report.first())
    }

    @Test
    fun `사용자 신고 중복 생성 시 예외가 발생한다`() = runTest {
        // given
        val user1 = insertUserAndReturnId("1")
        val user2 = insertUserAndReturnId("2")
        val reportReason = repository.createReportReason(
            code = TEST_REPORT_REASON_CODE,
            description = TEST_REPORT_REASON_DESCRIPTION,
        )
        assertNotNull(reportReason)

        // when: 중복 신고
        val exception = runCatching {
            // 첫 번째 신고는 성공
            repository.createReport(
                ReportDetail.create(
                    reporterId = user1,
                    reportedId = user2,
                    reportedUserKeywordId = null,
                    reasonId = reportReason.id,
                    customReason = TEST_CUSTOM_REASON,
                ),
            )

            // 두 번째 신고는 중복 신고이므로 예외 발생
            repository.createReport(
                ReportDetail.create(
                    reporterId = user1,
                    reportedId = user2,
                    reportedUserKeywordId = null,
                    reasonId = reportReason.id,
                    customReason = TEST_CUSTOM_REASON,
                ),
            )
        }.exceptionOrNull()

        // then
        assertTrue(exception is DatabaseException.DuplicatedDataException)
    }

    @Test
    fun `키워드 신고 중복 생성 시 예외가 발생한다`() = runTest {
        // given
        val user1 = insertUserAndReturnId("1")
        val userKeywordId = insertUserKeywordAndReturnId(user1.value)
        val reportReason = repository.createReportReason(
            code = TEST_REPORT_REASON_CODE,
            description = TEST_REPORT_REASON_DESCRIPTION,
        )
        assertNotNull(reportReason)

        // when: 중복 신고
        val exception = runCatching {
            // 첫 번째 신고는 성공
            repository.createReport(
                ReportDetail.create(
                    reporterId = user1,
                    reportedId = null,
                    reportedUserKeywordId = userKeywordId,
                    reasonId = reportReason.id,
                    customReason = TEST_CUSTOM_REASON,
                ),
            )

            // 두 번째 신고는 중복 신고이므로 예외 발생
            repository.createReport(
                ReportDetail.create(
                    reporterId = user1,
                    reportedId = null,
                    reportedUserKeywordId = userKeywordId,
                    reasonId = reportReason.id,
                    customReason = TEST_CUSTOM_REASON,
                ),
            )
        }.exceptionOrNull()

        // then
        assertTrue(exception is DatabaseException.DuplicatedDataException)
    }

    @Test
    fun `사용자 키워드 ID로 신고 내역이 있는지 확인한다 - 존재하는 경우`() = runTest {
        // given: 사용자 키워드 ID로 신고 내역 추가
        val userId = insertUserAndReturnId("1")
        val userKeywordId = insertUserKeywordAndReturnId(userId.value)
        val reportReason = repository.createReportReason(
            code = TEST_REPORT_REASON_CODE,
            description = TEST_REPORT_REASON_DESCRIPTION,
        )
        TestDatabaseFactory.dbQuery {
            Reports.insert {
                it[reporterId] = EntityID(userId.value, Users)
                it[reportedUserKeywordId] = EntityID(userKeywordId.value, UserKeywords)
                it[reasonId] = EntityID(reportReason.id.value, ReportReasons)
            }
        }

        // when: 존재 여부 확인
        val isExists = repository.existsByUserKeywordId(userKeywordId)

        // then: 존재 여부 검증
        assertTrue(isExists)
    }

    @Test
    fun `사용자 키워드 ID로 신고 내역이 있는지 확인한다 - 존재하지 않는 경우`() = runTest {
        // given: 사용자 키워드만 생성 (신고 내역 없음)
        val userId = insertUserAndReturnId("1")
        val userKeywordId = insertUserKeywordAndReturnId(userId.value)

        // when: 존재 여부 확인
        val isExists = repository.existsByUserKeywordId(userKeywordId)

        // then: 존재 여부 검증
        assertFalse(isExists)
    }

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "honggd"
            this.profileImageUrl = null
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }

        UserId(savedUser.id.value)
    }

    private suspend fun insertUserKeywordAndReturnId(userId: Long): UserKeywordId = TestDatabaseFactory.dbQuery {
        val keywordId = KeywordEntity
            .new {
                this.keyword = "keyword"
                this.embedding = "embedding"
                this.createdBy = EntityID(userId, Users)
            }.id.value

        val savedUserKeyword = UserKeywordEntity.new {
            this.userId = EntityID(userId, Users)
            this.keywordId = EntityID(keywordId, Keywords)
            this.description = "description"
        }

        UserKeywordId(savedUserKeyword.id.value)
    }

    companion object {
        private const val TEST_REPORT_REASON_CODE = "RRC001"
        private const val TEST_REPORT_REASON_DESCRIPTION = "Test Report Reason"
        private const val TEST_CUSTOM_REASON = "custom reason"
    }
}
