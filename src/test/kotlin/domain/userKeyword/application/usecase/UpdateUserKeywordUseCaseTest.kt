package com.peekr.domain.userKeyword.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch
import com.peekr.domain.userKeyword.domain.service.UserKeywordService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before

class UpdateUserKeywordUseCaseTest {
    private val userKeywordService = mockk<UserKeywordService>()
    private lateinit var usecase: UpdateUserKeywordUseCase

    @Before
    fun setUp() {
        usecase = UpdateUserKeywordUseCase(userKeywordService)
    }

    @Test
    fun `사용자 키워드 수정 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordService.update(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestUserKeywordPatch,
            )
        } returns true

        // when
        val result = usecase(
            ownerId = TestUserId,
            userKeywordId = TestUserKeywordId,
            patch = TestUserKeywordPatchDto,
        )

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestUserKeywordPatch = UserKeywordPatch(
            offsetX = 0.0f,
            offsetY = 0.0f,
            description = null,
        )
        private val TestUserKeywordPatchDto = UserKeywordPatchDto(
            offsetX = TestUserKeywordPatch.offsetX,
            offsetY = TestUserKeywordPatch.offsetY,
            description = TestUserKeywordPatch.description,
        )
    }
}
