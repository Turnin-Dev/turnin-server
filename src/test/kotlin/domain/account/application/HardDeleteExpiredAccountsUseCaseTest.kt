package com.turnin.domain.account.application

import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.ReportEntity
import com.turnin.common.db.schema.ReportReasonEntity
import com.turnin.common.db.schema.Reports
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.infrastructure.di.infraModule
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.domain.account.di.accountModule
import com.turnin.domain.keyword.di.keywordModule
import com.turnin.domain.report.di.reportModule
import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import com.turnin.domain.user.di.userModule
import com.turnin.domain.userKeyword.di.userKeywordModule
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

class HardDeleteExpiredAccountsUseCaseIntegrationTest : KoinTest {
    @Before
    fun setUp() {
        TestDatabaseFactory.init()
        startKoin {
            modules(
                userModule,
                keywordModule,
                userKeywordModule,
                reportModule,
                accountModule,
                infraModule,
            )
        }
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
        stopKoin()
    }

    private val hardDeleteExpiredAccountsUseCase: HardDeleteExpiredAccountsUseCase by inject()

    // ================ invoke() - 성공 케이스 ================

    @Test
    fun `invoke - 만료 계정 Hard Delete 성공`() = runTest {
        // given
        val user = insertUser("1")
        val other = insertUser("2")
        insertUserKeyword(user.id.value)
        insertReport(reporter = other.id.value, reportedUser = user.id.value)

        // when
        hardDeleteExpiredAccountsUseCase(user.id.value)

        // then
        val foundUser = findUserByIdForTest(user.id.value)
        assertNull(foundUser) // hard delete 검증

        val userKeywords = findUserKeywordsByUserIdForTest(user.id.value)
        assertTrue(userKeywords.isEmpty()) // hard delete 검증

        val reports = findReportsByReportedUserIdForTest(user.id.value)
        assertTrue(reports.isEmpty()) // hard delete 검증

        // other 사용자는 영향받지 않아야 함
        val otherUser = findUserByIdForTest(other.id.value)
        assertNotNull(otherUser)
    }

    @Test
    fun `invoke - 연관 데이터가 없어도 성공한다`() = runTest {
        // given
        val user = insertUser("1")

        // when
        hardDeleteExpiredAccountsUseCase(user.id.value)

        // then
        val foundUser = findUserByIdForTest(user.id.value)
        assertNull(foundUser)
    }

    // ================ invoke() - 롤백 케이스 ================

    @Test
    fun `invoke - DB 작업 실패 시 롤백된다`() = runTest {
        // given
        val user = insertUser("1")
        val other = insertUser("2")
        insertUserKeyword(user.id.value)
        insertReport(reporter = other.id.value, reportedUser = user.id.value)

        val mockUserDeleteSupportApi = mockk<UserDeletionSupportApi> {
            coEvery { delete(UserId(user.id.value)) } throws RuntimeException("DB connection failed")
        }

        val failingUseCase = HardDeleteExpiredAccountsUseCase(
            userDeleteSupportApi = mockUserDeleteSupportApi,
            userKeywordDeletionSupportApi = get(),
            reportDeletionSupportApi = get(),
        )

        // when & then
        assertThrows<RuntimeException> {
            failingUseCase(user.id.value)
        }

        // then: 롤백 검증
        val foundUser = findUserByIdForTest(user.id.value)
        assertNotNull(foundUser) // 롤백으로 유저 유지

        val userKeywords = findUserKeywordsByUserIdForTest(user.id.value)
        assertFalse(userKeywords.isEmpty()) // 롤백으로 userKeyword 유지

        val reports = findReportsByReportedUserIdForTest(user.id.value)
        assertFalse(reports.isEmpty()) // 롤백으로 report 유지
    }

    // ================ Test Utils ================

    private suspend fun insertUser(uniqueValue: String): UserEntity =
        TestDatabaseFactory.dbQuery {
            UserEntity.new {
                this.role = Role.USER
                this.provider = SocialLoginProvider.GOOGLE
                this.providerId = "pid$uniqueValue"
                this.displayId = "did$uniqueValue"
                this.name = "honggd$uniqueValue"
                this.profileImageUrl = null
                this.introduce = "hello$uniqueValue"
                this.isActive = false // 이미 soft delete된 상태
                this.deletedAt = Instant.now().minus(400.days.toJavaDuration()) // 1년 이상 경과
                this.lastLoginAt = Instant.now()
            }
        }

    private suspend fun insertUserKeyword(userId: Long) = TestDatabaseFactory.dbQuery {
        val keyword = KeywordEntity.new {
            this.keyword = "keyword"
            this.embedding = "[1, 1, 1]"
            this.createdBy = EntityID(userId, Users)
        }
        UserKeywordEntity.new {
            this.userId = EntityID(userId, Users)
            this.keywordId = keyword.id
            this.description = "description"
            this.isActive = false
        }
    }

    private suspend fun insertReport(reporter: Long, reportedUser: Long) = TestDatabaseFactory.dbQuery {
        val reason = ReportReasonEntity.all().first()
        ReportEntity.new {
            this.reporterId = EntityID(reporter, Users)
            this.reportedId = EntityID(reportedUser, Users)
            this.reportedUserKeywordId = null
            this.reasonId = reason.id
            this.customReason = null
        }
    }

    private suspend fun findUserByIdForTest(userId: Long): UserEntity? =
        TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId)
        }

    private suspend fun findUserKeywordsByUserIdForTest(userId: Long): List<UserKeywordEntity> =
        TestDatabaseFactory.dbQuery {
            UserKeywordEntity
                .find { UserKeywords.userId eq userId }
                .toList()
        }

    private suspend fun findReportsByReportedUserIdForTest(userId: Long): List<ReportEntity> =
        TestDatabaseFactory.dbQuery {
            ReportEntity
                .find { Reports.reportedId eq userId }
                .toList()
        }
}
