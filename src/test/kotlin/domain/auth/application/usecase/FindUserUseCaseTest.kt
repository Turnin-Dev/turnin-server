package com.peekr.domain.auth.application.usecase

import com.peekr.common.model.SocialLoginProvider
import com.peekr.domain.auth.AuthTestDoubles
import com.peekr.domain.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class FindUserUseCaseTest {
    private val authRepository = mockk<AuthRepository>()
    private val usecase = FindUserUseCase(authRepository)

    @Test
    fun `사용자 찾기 성공 테스트`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns AuthTestDoubles.MockAuthUser.copy(
            provider = TestProvider,
            providerId = TEST_PROVIDER_ID,
        )

        // when
        val result = usecase(TestProvider, TEST_PROVIDER_ID)

        // then
        assertTrue(result.exists)
    }

    companion object {
        private val TestProvider = SocialLoginProvider.GOOGLE
        private const val TEST_PROVIDER_ID = "provider-id"
    }
}
