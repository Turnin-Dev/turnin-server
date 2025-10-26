package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Before

class UpdateDescriptionUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private lateinit var usecase: UpdateDescriptionUseCase

    @Before
    fun setUp() {
        usecase = UpdateDescriptionUseCase(userKeywordRepository)
    }

    @Test
    fun `사용자 키워드 설명 수정 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordRepository.updateDescription(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestDescription,
            )
        } returns true

        // when
        val result = usecase(
            ownerId = TestUserId,
            userKeywordId = TestUserKeywordId,
            patch = TestDescription.toDto(),
        )

        // then
        assertNotNull(result)
        assertEquals(TestDescription.value, result.value)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestDescription = Description(value = "hello")
    }
}
