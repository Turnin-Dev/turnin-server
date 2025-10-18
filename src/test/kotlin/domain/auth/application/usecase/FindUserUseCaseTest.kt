package com.peekr.domain.auth.application.usecase

import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.service.AuthService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class FindUserUseCaseTest {
    private val authService = mockk<AuthService>()
    private val usecase = FindUserUseCase(authService)

    @Test
    fun `사용자 찾기 성공 테스트`() = runTest {
        // given
        coEvery {
            authService.findUser(any(), any())
        } returns FindUserResult(true)

        // when
        val result = usecase(TestProvider, TEST_PROVIDER_ID)

        // then
        assertTrue(result.exists)
    }

    companion object {
        private val TestProvider = SocialLoginProviderForAuth.GOOGLE
        private const val TEST_PROVIDER_ID = "provider-id"
    }
}
