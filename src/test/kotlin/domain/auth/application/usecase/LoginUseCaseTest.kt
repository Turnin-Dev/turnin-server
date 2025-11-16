package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserId
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.util.TestDatabaseFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

class LoginUseCaseTest {
    private val authRepository = mockk<AuthRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtTokenService = mockk<JWTTokenService>()
    private val usecase = LoginUseCase(authRepository, refreshTokenRepository, jwtTokenService)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns TestAuthUser
        coEvery {
            refreshTokenRepository.save(TestLoginResult.authUser.userId, TestLoginResult.jwtToken.refreshToken)
        } returns true
        coEvery {
            jwtTokenService.generate(any())
        } returns TestJwtToken
        coEvery {
            authRepository.updateLastLoginAt(TestAuthUser.userId)
        } just Runs
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `로그인 성공 테스트`() = runTest {
        // when
        val loginResultDto = usecase(TestLoginDto)

        // then
        assertNotNull(loginResultDto)
        assertEquals(loginResultDto.userId, TestLoginResult.authUser.userId)
        assertEquals(loginResultDto.jwtTokenDto.accessToken, TestLoginResult.jwtToken.accessToken)
    }

    @Test
    fun `로그인 실패 시 null을 반환한다`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns null

        // when
        val loginResultDto = usecase(TestLoginDto)

        // then
        assertNull(loginResultDto)
    }

    companion object {
        private val TestProvider = SocialLoginProvider.GOOGLE
        private const val TEST_PROVIDER_ID = "provider-id"
        private val TestLoginDto = LoginDto(
            provider = TestProvider,
            providerId = TEST_PROVIDER_ID,
        )
        private val TestJwtToken = JWTToken(
            accessToken = "aaa.bbb.ccc",
            refreshToken = "aaa.bbb.ccc",
        )
        private val TestAuthUser = AuthUser(
            userId = UserId(1L),
            role = Role.USER,
            provider = TestProvider,
            providerId = TEST_PROVIDER_ID,
            displayId = DisplayId("id"),
            name = Name("name"),
            profileImageUrl = "profileImageUrl",
            introduce = Introduce("introduce"),
            isActive = true,
            lastLoginAt = null,
        )
        private val TestLoginResult = LoginResult(
            jwtToken = TestJwtToken,
            authUser = TestAuthUser,
        )
    }
}
