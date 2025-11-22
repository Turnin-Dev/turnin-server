package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertNull

class GetDescriptionUseCaseTest {
    private val userKeywordRepository: UserKeywordRepository = mockk()
    private val usecase = GetDescriptionUseCase(userKeywordRepository)

    @Test
    fun `사용자 키워드 설명 조회 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordRepository.findDescriptionById(TestUserId, TestUserKeywordId)
        } returns TestDescription

        // when
        val descriptionDto = usecase(TestUserId, TestUserKeywordId)

        // then
        assertNotNull(descriptionDto)
        assertEquals(TestDescription.value, descriptionDto.value)
    }

    @Test
    fun `사용자 키워드 설명이 등록되지 않은 경우 null을 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.findDescriptionById(TestUserId, TestUserKeywordId)
        } returns null

        // when
        val descriptionDto = usecase(TestUserId, TestUserKeywordId)

        // then
        assertNull(descriptionDto)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserKeywordId = UserKeywordId(1L)
        private val TestDescription = Description("test")
    }
}
