package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserId
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.RegisterResult
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

class RegisterUseCaseTest {
    private val authRepository = mockk<AuthRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtTokenService = mockk<JWTTokenService>()
    private val usecase = RegisterUseCase(authRepository, refreshTokenRepository, jwtTokenService)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        coEvery {
            refreshTokenRepository.save(TestUserId, TestRegisterResult.jwtToken.refreshToken)
        } returns true
        coEvery {
            authRepository.save(any())
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
            name = Name("name"),
            profileImageUrl = "profileImageUrl",
            introduce = Introduce("introduce"),
            isActive = true,
            lastLoginAt = null,
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
