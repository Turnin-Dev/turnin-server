package com.turnin.domain.account.application

import com.turnin.domain.report.application.provider.ReportDeletionSupportApi
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class HardDeleteExpiredUserKeywordsUseCaseTest {
    private val reportDeletionSupportApi = mockk<ReportDeletionSupportApi>()
    private val userKeywordDeletionSupportApi = mockk<UserKeywordDeletionSupportApi>()
    private val usecase = HardDeleteExpiredUserKeywordsUseCase(
        reportDeletionSupportApi = reportDeletionSupportApi,
        userKeywordDeletionSupportApi = userKeywordDeletionSupportApi,
    )

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `키워드의 신고 내역을 먼저 삭제한 뒤 키워드를 삭제한다`() = runTest {
        // given
        val userKeywordIds = listOf(1L, 2L)
        coEvery { reportDeletionSupportApi.deleteByUserKeywordIds(userKeywordIds) } just runs
        coEvery { userKeywordDeletionSupportApi.deleteByIds(userKeywordIds) } just runs

        // when
        usecase(userKeywordIds)

        // then
        coVerifyOrder {
            reportDeletionSupportApi.deleteByUserKeywordIds(userKeywordIds)
            userKeywordDeletionSupportApi.deleteByIds(userKeywordIds)
        }
    }

    @Test
    fun `삭제할 키워드가 없으면 아무것도 삭제하지 않는다`() = runTest {
        // when
        usecase(emptyList())

        // then
        coVerify(exactly = 0) { reportDeletionSupportApi.deleteByUserKeywordIds(any()) }
        coVerify(exactly = 0) { userKeywordDeletionSupportApi.deleteByIds(any()) }
    }
}
