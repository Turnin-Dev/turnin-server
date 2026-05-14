package com.turnin.domain.auth.application.usecase

import com.turnin.common.model.Introduce
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.domain.model.AuthUser
import com.turnin.domain.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
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
        } returns TestAuthUser

        // when
        val result = usecase(TestProvider, TEST_PROVIDER_ID)

        // then
        assertTrue(result.exists)
    }

    @Test
    fun `사용자 찾기 실패 테스트`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns null

        // when
        val result = usecase(TestProvider, TEST_PROVIDER_ID)

        // then
        assertFalse(result.exists)
    }

    companion object {
        private val TestProvider = SocialLoginProvider.GOOGLE
        private const val TEST_PROVIDER_ID = "provider-id"
        private val TestAuthUser = AuthUser(
            userId = UserId(1L),
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = TEST_PROVIDER_ID,
            displayId = DisplayId("hong_gd_123"),
            userName = UserName("honggd"),
            profileImageUrl = "http://example.com/profile.jpg",
            introduce = Introduce("Hello!"),
            isActive = true,
            lastLoginAt = Instant.now(),
        )
    }
}
