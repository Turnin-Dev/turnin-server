package com.peekr.domain.userKeyword.infrastructure.service.impl

import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before

class UserKeywordServiceImplTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private lateinit var service: UserKeywordServiceImpl

    @Before
    fun setUp() {
        service = UserKeywordServiceImpl(userKeywordRepository)
    }

    @Test
    fun `사용자 키워드 조회 성공 테스트`() = runTest {
        // given
        val itemCount = 2
        coEvery {
            userKeywordRepository.findByUserId(TestUserId)
        } returns List(itemCount) { TestUserKeyword }

        // when
        val userKeywords = service.getKeywords(TestUserId)

        // then
        assertTrue(userKeywords.size == itemCount)
    }

    @Test
    fun `사용자 키워드 생성 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordRepository.create(
                keywordId = TestUserKeyword.keywordId,
                userId = TestUserKeyword.userId,
                offsetX = TestUserKeyword.offsetX,
                offsetY = TestUserKeyword.offsetY,
                description = TestUserKeyword.description,
            )
        } returns TestUserKeyword

        // when
        val userKeyword = service.create(
            keywordId = TestUserKeyword.keywordId,
            userId = TestUserKeyword.userId,
            offsetX = TestUserKeyword.offsetX,
            offsetY = TestUserKeyword.offsetY,
            description = TestUserKeyword.description,
        )

        // then
        assertTrue(userKeyword == TestUserKeyword)
    }

    @Test
    fun `사용자 키워드 수정 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordRepository.update(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestUserKeywordPatch,
            )
        } returns true

        // when
        val result = service.update(
            ownerId = TestUserId,
            userKeywordId = TestUserKeywordId,
            patch = TestUserKeywordPatch,
        )

        // then
        assertTrue(result)
    }

    @Test
    fun `사용자 키워드 삭제 성공 테스트`() = runTest {
        // given
        coEvery {
            userKeywordRepository.delete(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
            )
        } returns true

        // when
        val result = service.delete(
            ownerId = TestUserId,
            userKeywordId = TestUserKeywordId,
        )

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestUserKeyword = UserKeyword(
            id = TestUserKeywordId,
            userId = TestUserId,
            keywordId = TestKeywordId,
            offsetX = 0.0f,
            offsetY = 0.0f,
            description = "",
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestUserKeywordPatch = UserKeywordPatch(
            offsetX = 0.0f,
            offsetY = 0.0f,
            description = null,
        )
    }
}
