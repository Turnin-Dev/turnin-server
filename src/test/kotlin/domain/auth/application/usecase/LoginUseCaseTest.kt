package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserId
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService
import com.peekr.domain.auth.exception.AuthException
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class LoginUseCaseTest {
    private val authService = mockk<AuthService>()
    private val refreshTokenService = mockk<RefreshTokenService>()
    private val usecase = LoginUseCase(authService, refreshTokenService)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `로그인 성공 테스트`() = runTest {
        // given
        coEvery { authService.login(any(), any()) } returns TestLoginResult
        coEvery {
            refreshTokenService.save(TestLoginResult.authUser.userId, TestLoginResult.jwtToken.refreshToken)
        } returns true

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
        coEvery { authService.login(any(), any()) } returns null

        // when
        val loginResultDto = usecase(TestLoginDto)

        // then
        assertNull(loginResultDto)
    }

    @Test
    fun `리프레쉬 토큰 저장 중 실패하면 예외가 발생한다`() = runTest {
        // given
        coEvery { authService.login(any(), any()) } returns TestLoginResult
        coEvery {
            refreshTokenService.save(TestLoginResult.authUser.userId, TestLoginResult.jwtToken.refreshToken)
        } throws AuthException.CannotSaveRefreshTokenException()

        // when, then
        assertThrows<AuthException.CannotSaveRefreshTokenException> {
            usecase(TestLoginDto)
        }
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
