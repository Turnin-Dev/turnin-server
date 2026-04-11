package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.Introduce
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before

class RefreshTokenUseCaseTest {
    private val jwtTokenService = mockk<JWTTokenService>()
    private val authRepository = mockk<AuthRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val usecase = RefreshTokenUseCase(authRepository, refreshTokenRepository, jwtTokenService)

    @Before
    fun setUp() {
        coEvery {
            jwtTokenService.extractSubjectWithToken(any(), any())
        } returns TEST_SUBJECT
        coEvery {
            jwtTokenService.createVerifier(any())
        } returns JWTTestDoubles.MockRefreshTokenVerifier
        coEvery {
            jwtTokenService.generate(any())
        } returns TestJWTToken
        coEvery {
            authRepository.findUserByUserId(TestUserId)
        } returns TestAuthUser
        coEvery {
            refreshTokenRepository.findUserIdByRefreshToken(any())
        } returns TestUserId
        coEvery {
            refreshTokenRepository.save(TestUserId, any())
        } returns true
    }

    @Test
    fun `리프레쉬 토큰 갱신 성공 테스트`() = runTest {
        // given
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())

        // when
        val jwtTokenDto = usecase(token.refreshToken)

        // then
        assertNotNull(jwtTokenDto)
        assert(jwtTokenDto.accessToken == TestJWTToken.accessToken)
        assert(jwtTokenDto.refreshToken == TestJWTToken.refreshToken)
    }

    @Test
    fun `토큰에서 Subject(사용자 ID)를 추출에 실패하면 null을 반환한다`() = runTest {
        // given
        coEvery {
            jwtTokenService.extractSubjectWithToken(any(), any())
        } returns null

        // when
        val jwtTokenDto = usecase("aaa.bbb.ccc")

        // then
        assertNull(jwtTokenDto)
    }

    @Test
    fun `리프레쉬 토큰 갱신 과정에서 토큰 검증에 실패하면 null을 반환한다`() = runTest {
        // given
        coEvery { jwtTokenService.createVerifier(any()) } throws Exception()

        // when
        val jwtTokenDto = usecase("aaa.bbb.ccc")

        // then
        assertNull(jwtTokenDto)
    }

    @Test
    fun `리프레쉬 토큰 갱신 과정에서 토큰 생성에 실패하면 null을 반환한다`() = runTest {
        // given
        coEvery { jwtTokenService.generate(any()) } throws Exception()

        // when
        val jwtTokenDto = usecase("aaa.bbb.ccc")

        // then
        assertNull(jwtTokenDto)
    }

    @Test
    fun `리프레쉬 토큰 저장 실패 시 null을 반환한다`() = runTest {
        // given
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            refreshTokenRepository.save(TestUserId, any())
        } returns false

        // when
        val jwtTokenDto = usecase(token.refreshToken)

        // then
        assertNull(jwtTokenDto)
    }

    companion object {
        private const val TEST_SUBJECT = "1"
        private val TestUserId = UserId(TEST_SUBJECT.toLong())
        private val TestJWTToken = JWTToken(
            accessToken = "aaa.bbb.ccc",
            refreshToken = "ddd.eee.fff",
        )
        val TestAuthUser = AuthUser(
            userId = TestUserId,
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
            displayId = DisplayId("hong_gd_123"),
            userName = UserName("honggd"),
            profileImageUrl = "http://example.com/profile.jpg",
            introduce = Introduce("Hello!"),
            isActive = true,
            lastLoginAt = Instant.now(),
        )
    }
}
