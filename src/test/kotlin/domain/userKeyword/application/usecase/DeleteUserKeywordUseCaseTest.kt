package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.provider.PingPongProvider
import com.turnin.domain.userKeyword.domain.provider.ReportProvider
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeleteUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val reportProvider = mockk<ReportProvider>()
    private val pingPongProvider = mockk<PingPongProvider>()
    private lateinit var usecase: DeleteUserKeywordUseCase

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        usecase = DeleteUserKeywordUseCase(userKeywordRepository, reportProvider, pingPongProvider)
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `사용자 키워드 삭제 성공 테스트 - 해당 키워드가 신고 내역에 존재하면 Soft Delete를 수행한다`() = runTest {
        // given: 신고 내역에 존재하도록 설정
        coEvery { reportProvider.existsByUserKeywordId(TestUserKeywordId) } returns true
        coEvery { userKeywordRepository.deactivate(TestUserId, TestUserKeywordId) } returns true
        coEvery { pingPongProvider.deleteAllByUserKeywordId(TestUserKeywordId) } just Runs

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

    @Test
    fun `신고 내역이 있는 게시물이 비활성화되면 게시물의 핑퐁을 삭제한다`() = runTest {
        // given
        coEvery { reportProvider.existsByUserKeywordId(TestUserKeywordId) } returns true
        coEvery { userKeywordRepository.deactivate(TestUserId, TestUserKeywordId) } returns true
        coEvery { pingPongProvider.deleteAllByUserKeywordId(TestUserKeywordId) } just Runs

        // when
        usecase(TestUserId, TestUserKeywordId)

        // then
        coVerify(exactly = 1) { pingPongProvider.deleteAllByUserKeywordId(TestUserKeywordId) }
    }

    @Test
    fun `신고 내역이 있는 게시물의 비활성화에 실패하면 핑퐁을 삭제하지 않고 false를 반환한다`() = runTest {
        // given: 본인 게시물이 아니거나 존재하지 않아 비활성화되지 않는 경우
        coEvery { reportProvider.existsByUserKeywordId(TestUserKeywordId) } returns true
        coEvery { userKeywordRepository.deactivate(TestUserId, TestUserKeywordId) } returns false

        // when
        val result = usecase(TestUserId, TestUserKeywordId)

        // then
        assertFalse(result)
        coVerify(exactly = 0) { pingPongProvider.deleteAllByUserKeywordId(any()) }
    }

    @Test
    fun `신고 내역이 없는 게시물을 삭제하면 핑퐁을 별도로 삭제하지 않는다`() = runTest {
        // given: 핑퐁은 게시물 삭제 시 CASCADE로 함께 삭제된다.
        coEvery { reportProvider.existsByUserKeywordId(TestUserKeywordId) } returns false
        coEvery { userKeywordRepository.delete(TestUserId, TestUserKeywordId) } returns true

        // when
        usecase(TestUserId, TestUserKeywordId)

        // then
        coVerify(exactly = 0) { pingPongProvider.deleteAllByUserKeywordId(any()) }
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestUserKeywordId = UserKeywordId(1)
    }
}
