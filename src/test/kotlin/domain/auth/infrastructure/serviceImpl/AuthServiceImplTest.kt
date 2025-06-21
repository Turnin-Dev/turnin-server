package com.peekr.domain.auth.infrastructure.serviceImpl

import com.peekr.common.jwt.JWTTestDoubles.MockJWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.domain.auth.AuthTestDoubles.MockAuthUser
import com.peekr.domain.auth.domain.model.SocialLoginProvider
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.exception.AuthException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before

class AuthServiceImplTest {
    private val authRepository = mockk<AuthRepository>()
    private val jwtTokenService = mockk<JWTTokenService>()
    private lateinit var authService: AuthServiceImpl

    @Before
    fun setup() {
        authService = AuthServiceImpl(authRepository, jwtTokenService)
    }

    @Test
    fun `login 성공 테스트`() = runTest {
        // given
        coEvery {
            authRepository.findByProviderAndProviderId(any(), any())
        } returns MockAuthUser

        every { jwtTokenService.generate(any()) } returns MockJWTToken

        // when
        val token = authService.login(
            provider = SocialLoginProvider.Google,
            providerId = "123123",
        )

        // then
        assertEquals(token, MockJWTToken)
    }

    @Test
    fun `login 실패 테스트 - 존재하지 않는 사용자`() = runTest {
        // given
        coEvery {
            authRepository.findByProviderAndProviderId(any(), any())
        } returns null

        every { jwtTokenService.generate(any()) } returns MockJWTToken

        // when
        val token = authService.login(
            provider = SocialLoginProvider.Google,
            providerId = "123123",
        )

        // then
        assertNull(token)
    }

    @Test
    fun `register 성공 테스트`() = runTest {
        // given
        coEvery {
            authRepository.save(any())
        } returns MockAuthUser
        every { jwtTokenService.generate(any()) } returns MockJWTToken

        // when
        val token = authService.register(MockAuthUser)

        // then
        assertEquals(token, MockJWTToken)
    }

    @Test
    fun `register 실패 테스트 - 중복되는 사용자 삽입 시`() = runTest {
        // given
        coEvery {
            authRepository.save(any())
        } throws AuthException.DuplicateUserException("")
        every { jwtTokenService.generate(any()) } returns MockJWTToken

        // when & then
        assertFailsWith<AuthException.DuplicateUserException> {
            authService.register(MockAuthUser)
        }
    }
}
