package com.peekr.domain.auth.infrastructure.service.impl

import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.MockVerifier
import com.peekr.common.jwt.JWTTestDoubles.REALM
import com.peekr.common.jwt.JWTTestDoubles.getJWTTokenPayload
import com.peekr.common.jwt.JWTTestDoubles.getMockJWTToken
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.domain.auth.AuthTestDoubles.MockAuthUser
import com.peekr.domain.auth.AuthTestDoubles.MockRegister
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.exception.AuthException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before

class AuthServiceImplTest {
    private val authRepository = mockk<AuthRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtTokenService = mockk<JWTTokenService>()
    private lateinit var authService: AuthServiceImpl
    private lateinit var mockJWTToken: JWTToken

    @Before
    fun setup() {
        authService = AuthServiceImpl(authRepository, refreshTokenRepository, jwtTokenService)

        mockJWTToken = getMockJWTToken(getJWTTokenPayload(claim = MockAuthUser.displayId))
        every { jwtTokenService.realm } returns REALM
        every { jwtTokenService.audience } returns AUDIENCE
        every { jwtTokenService.issuer } returns ISSUER
        every { jwtTokenService.generate(any()) } returns mockJWTToken
        every { jwtTokenService.createVerifier(any()) } returns MockVerifier
    }

    @Test
    fun `login 성공 테스트`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns MockAuthUser

        coEvery { authRepository.updateLastLoginAt(any()) } just Runs

        every { jwtTokenService.generate(any()) } returns getMockJWTToken()

        // when
        val loginResult = authService.login(
            provider = SocialLoginProviderForAuth.GOOGLE,
            providerId = "123123",
        )

        // then
        assertNotNull(loginResult)
        assertEquals(loginResult.jwtToken, getMockJWTToken())
    }

    @Test
    fun `login 실패 테스트 - 존재하지 않는 사용자`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns null

        every { jwtTokenService.generate(any()) } returns getMockJWTToken()

        // when
        val token = authService.login(
            provider = SocialLoginProviderForAuth.GOOGLE,
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
        every { jwtTokenService.generate(any()) } returns getMockJWTToken()

        // when
        val token = authService.register(MockRegister).jwtToken

        // then
        assertEquals(token, getMockJWTToken())
    }

    @Test
    fun `register 실패 테스트 - 중복되는 사용자 삽입 시`() = runTest {
        // given
        coEvery {
            authRepository.save(any())
        } throws AuthException.DuplicateUserException("")
        every { jwtTokenService.generate(any()) } returns getMockJWTToken()

        // when & then
        assertFailsWith<AuthException.DuplicateUserException> {
            authService.register(MockRegister)
        }
    }

    @Test
    fun `refresh 성공 테스트`() = runTest {
        // given
        coEvery {
            refreshTokenRepository.findUserIdByRefreshToken(any())
        } returns MockAuthUser.id
        coEvery {
            authRepository.findUserByUserId(any())
        } returns MockAuthUser

        // when
        val token = authService.refresh(mockJWTToken.refreshToken)

        // then
        assertNotNull(token)
        assertEquals(token.accessToken, mockJWTToken.accessToken)
        assertEquals(token.refreshToken, mockJWTToken.refreshToken)
    }

    @Test
    fun `refresh 실패 테스트 - 토큰으로 사용자 ID를 찾지 못하는 경우`() = runTest {
        // given
        coEvery {
            refreshTokenRepository.findUserIdByRefreshToken(any())
        } returns null
        coEvery {
            authRepository.findUserByUserId(any())
        } returns MockAuthUser

        // when
        val token = authService.refresh(mockJWTToken.refreshToken)

        // then
        assertNull(token)
    }

    @Test
    fun `refresh 실패 테스트 - 사용자 ID로 사용자를 찾지 못하는 경우`() = runTest {
        // given
        coEvery {
            refreshTokenRepository.findUserIdByRefreshToken(any())
        } returns MockAuthUser.id
        coEvery {
            authRepository.findUserByUserId(any())
        } returns null

        // when
        val token = authService.refresh(mockJWTToken.refreshToken)

        // then
        assertNull(token)
    }

    @Test
    fun `refresh 실패 테스트 - AuthRepository에서 예외가 발생하는 경우`() = runTest {
        // given
        val expectedException = NullPointerException()
        coEvery {
            refreshTokenRepository.findUserIdByRefreshToken(any())
        } returns MockAuthUser.id
        coEvery {
            authRepository.findUserByUserId(any())
        } throws expectedException

        // when
        val token = authService.refresh(mockJWTToken.refreshToken)

        // then
        assertNull(token)
    }

    @Test
    fun `findUser 성공 테스트 - 사용자가 존재하는 경우`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns MockAuthUser

        // when
        val findUserResult = authService.findUser(SocialLoginProviderForAuth.GOOGLE, "123123")

        // then
        assertTrue(findUserResult.exists)
    }

    @Test
    fun `findUser 성공 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        // given
        coEvery {
            authRepository.findAuthUserByProviderAndProviderId(any(), any())
        } returns null

        // when
        val findUserResult = authService.findUser(SocialLoginProviderForAuth.GOOGLE, "123123")

        // then
        assertFalse(findUserResult.exists)
    }

    @Test
    fun `existsDisplayId 성공 테스트 - 사용자 표시 ID가 존재하는 경우`() = runTest {
        // given
        coEvery { authRepository.existsByDisplayId(any()) } returns true

        // when
        val existsDisplayId = authService.existsDisplayId(MockRegister.displayId)

        // then
        assertTrue(existsDisplayId)
    }

    @Test
    fun `existsDisplayId 성공 테스트 - 사용자 표시 ID가 존재하지 않는 경우`() = runTest {
        // given
        coEvery { authRepository.existsByDisplayId(any()) } returns false

        // when
        val existsDisplayId = authService.existsDisplayId("weird_display_id")

        // then
        assertFalse(existsDisplayId)
    }
}
