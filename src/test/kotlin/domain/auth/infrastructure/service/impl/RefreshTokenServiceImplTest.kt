package com.peekr.domain.auth.infrastructure.service.impl

import com.peekr.common.model.UserId
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RefreshTokenServiceImplTest {
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val service = RefreshTokenServiceImpl(refreshTokenRepository)

    @Test
    fun `리프레쉬 토큰 저장 성공 테스트`() = runTest {
        // given
        coEvery { refreshTokenRepository.save(TestUserId, TEST_REFRESH_TOKEN) } returns true

        // when
        val result = service.save(TestUserId, TEST_REFRESH_TOKEN)

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private const val TEST_REFRESH_TOKEN = "aaa.bbb.ccc"
    }
}
