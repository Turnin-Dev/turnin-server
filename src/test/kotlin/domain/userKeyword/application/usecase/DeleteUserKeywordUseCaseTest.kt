package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.provider.ReportProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val reportProvider = mockk<ReportProvider>()
    private lateinit var usecase: DeleteUserKeywordUseCase

    @Before
    fun setUp() {
        usecase = DeleteUserKeywordUseCase(userKeywordRepository, reportProvider)
    }

    @Test
    fun `사용자 키워드 삭제 성공 테스트 - 해당 키워드가 신고 내역에 존재하면 Soft Delete를 수행한다`() = runTest {
        // given: 신고 내역에 존재하도록 설정
        coEvery { reportProvider.existsByUserKeywordId(TestUserKeywordId) } returns true
        coEvery { userKeywordRepository.deactivate(TestUserId, TestUserKeywordId) } returns true

        // when
        val result = usecase(TestUserId, TestUserKeywordId)

        // then: 삭제 결과는 성공, Soft Delete만 수행되어야 한다.
        assertTrue(result)
        coVerify(exactly = 1) { userKeywordRepository.deactivate(TestUserId, TestUserKeywordId) }
        coVerify(exactly = 0) { userKeywordRepository.delete(TestUserId, TestUserKeywordId) }
    }

    @Test
    fun `사용자 키워드 삭제 성공 테스트 - 해당 키워드가 신고 내역에 존재하지 않으면 Hard Delete를 수행한다`() = runTest {
        // given: 신고 내역에 존재하지 않도록 설정
        coEvery { reportProvider.existsByUserKeywordId(TestUserKeywordId) } returns false
        coEvery { userKeywordRepository.delete(TestUserId, TestUserKeywordId) } returns true

        // when
        val result = usecase(TestUserId, TestUserKeywordId)

        // then: 삭제 결과는 성공, Hard Delete만 수행되어야 한다.
        assertTrue(result)
        coVerify(exactly = 0) { userKeywordRepository.deactivate(TestUserId, TestUserKeywordId) }
        coVerify(exactly = 1) { userKeywordRepository.delete(TestUserId, TestUserKeywordId) }
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestUserKeywordId = UserKeywordId(1)
    }
}
