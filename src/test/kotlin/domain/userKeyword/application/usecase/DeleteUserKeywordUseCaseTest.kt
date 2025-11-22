package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private lateinit var usecase: DeleteUserKeywordUseCase

    @Before
    fun setUp() {
        usecase = DeleteUserKeywordUseCase(userKeywordRepository)
    }

    @Test
    fun `사용자 키워드 삭제 성공 테스트`() = runTest {
        // given
        coEvery { userKeywordRepository.delete(TestUserId, TestUserKeywordId) } returns true

        // when
        val result = usecase(TestUserId, TestUserKeywordId)

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestUserKeywordId = UserKeywordId(1)
    }
}
