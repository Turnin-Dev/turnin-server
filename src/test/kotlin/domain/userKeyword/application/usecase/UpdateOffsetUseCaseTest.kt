package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.Offset
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Before

class UpdateOffsetUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private lateinit var usecase: UpdateOffsetUseCase

    @Before
    fun setUp() {
        usecase = UpdateOffsetUseCase(userKeywordRepository)
    }

    @Test
    fun `사용자 키워드 오프셋 수정 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordRepository.updateOffset(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestOffset,
            )
        } returns true

        // when
        val result = usecase(
            ownerId = TestUserId,
            userKeywordId = TestUserKeywordId,
            patch = TestOffset.toDto(),
        )

        // then
        assertNotNull(result)
        assertEquals(TestOffset.x, result.x)
    }

    companion object {
        private val TestUserId = UserId.Companion(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestOffset = Offset(
            x = 0.0f,
            y = 0.0f,
        )
    }
}
