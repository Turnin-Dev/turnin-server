package com.peekr.domain.report.infrastructure.repository

import com.peekr.common.db.schema.Reports
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserId
import com.peekr.domain.report.domain.model.ReportDetail
import com.peekr.util.DatabaseTestRule
import com.peekr.util.TestDatabaseFactory
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReportRepositoryImplTest {
    @get:Rule
    val databaseTestRule = DatabaseTestRule()

    private val repository = ReportRepositoryImpl()

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
                ReportDetail(
                    reporterId = user1,
                    reportedId = user2,
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

    companion object {
        private const val TEST_REPORT_REASON_CODE = "RRC001"
        private const val TEST_REPORT_REASON_DESCRIPTION = "Test Report Reason"
        private const val TEST_CUSTOM_REASON = "custom reason"
    }
}
