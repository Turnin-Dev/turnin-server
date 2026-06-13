package com.turnin.domain.auth.application.usecase

import com.turnin.common.jwt.domain.model.JWTToken
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.model.Introduce
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.DisplayIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.application.dto.RegisterDto
import com.turnin.domain.auth.domain.model.AuthUser
import com.turnin.domain.auth.domain.model.RegisterResult
import com.turnin.domain.auth.domain.repository.AuthRepository
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository
import com.turnin.domain.auth.exception.AuthException
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class RegisterUseCaseTest {
    private val authRepository = mockk<AuthRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtTokenService = mockk<JWTTokenService>()
    private val usecase = RegisterUseCase(authRepository, refreshTokenRepository, jwtTokenService, Role.USER)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        coEvery {
            refreshTokenRepository.save(TestUserId, TestRegisterResult.jwtToken.refreshToken)
        } returns true
        coEvery {
            authRepository.save(any(), any())
        } returns TestAuthUser
        coEvery {
            jwtTokenService.generate(any())
        } returns TestJwtToken
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `회원가입 성공 테스트`() = runTest {
        // when
        val registerResultDto = usecase(TestRegisterDto)

        // then
        assertEquals(TestUserId, registerResultDto.userId)
    }

    @Test
    fun `사용자 표시 ID 유효성 검사 실패 시 예외가 발생한다`() = runTest {
        // given
        val invalidDisplayId = "a".repeat(DisplayId.MAX_LENGTH + 1)

        // when, then
        assertThrows<DisplayIdValidationException> {
            usecase(TestRegisterDto.copy(displayId = invalidDisplayId))
        }
    }

    @Test
    fun `리프레쉬 토큰 저장 실패 시 예외가 발생한다`() = runTest {
        // given: 리프레쉬 토큰 저장 실패 설정
        coEvery {
            refreshTokenRepository.save(TestUserId, TestRegisterResult.jwtToken.refreshToken)
        } returns false

        // when, then
        assertThrows<AuthException.RefreshTokenSaveFailed> {
            usecase(TestRegisterDto)
        }
    }

    companion object {
        private val TestProvider = SocialLoginProvider.GOOGLE
        private const val TEST_PROVIDER_ID = "provider-id"
        private val TestUserId = UserId(1L)
        private val TestJwtToken = JWTToken(
            accessToken = "aaa.bbb.ccc",
            refreshToken = "aaa.bbb.ccc",
        )
        private val TestAuthUser = AuthUser(
            userId = TestUserId,
            role = Role.USER,
            provider = TestProvider,
            providerId = TEST_PROVIDER_ID,
            displayId = DisplayId("id"),
            userName = UserName("name"),
            profileImageUrl = "profileImageUrl",
            introduce = Introduce("introduce"),
            isActive = true,
            lastLoginAt = Instant.now(),
        )
        private val TestRegisterDto = RegisterDto(
            provider = TestProvider,
            providerId = TEST_PROVIDER_ID,
            displayId = "id",
            name = "name",
            profileImageUrl = "profileImageUrl",
            introduce = "introduce",
        )
        private val TestRegisterResult = RegisterResult(
            jwtToken = TestJwtToken,
            authUser = TestAuthUser,
        )
    }
}
