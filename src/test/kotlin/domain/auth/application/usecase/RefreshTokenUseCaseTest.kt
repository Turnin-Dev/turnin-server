package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.model.UserId
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class RefreshTokenUseCaseTest {
    private val jwtTokenService = mockk<JWTTokenService>()
    private val authService = mockk<AuthService>()
    private val refreshTokenService = mockk<RefreshTokenService>()
    private val usecase = RefreshTokenUseCase(jwtTokenService, authService, refreshTokenService)

    @Test
    fun `리프레쉬 토큰 갱신 성공 테스트`() = runTest {
        // given
        val userId = UserId(TEST_SUBJECT.toLong())
        coEvery {
            jwtTokenService.extractSubjectWithToken(any(), any())
        } returns TEST_SUBJECT
        coEvery { authService.refresh(any()) } returns TestJWTToken
        coEvery { refreshTokenService.save(userId, any()) } returns true

        // when
        val jwtTokenDto = usecase("aaa.bbb.ccc")

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
        coEvery { authService.refresh(any()) } returns TestJWTToken

        // when
        val jwtTokenDto = usecase("aaa.bbb.ccc")

        // then
        assertNull(jwtTokenDto)
    }

    @Test
    fun `리프레쉬 토큰 갱신에 실패하면 null을 반환한다`() = runTest {
        // given
        val userId = UserId(TEST_SUBJECT.toLong())
        coEvery {
            jwtTokenService.extractSubjectWithToken(any(), any())
        } returns TEST_SUBJECT
        coEvery { authService.refresh(any()) } returns null
        coEvery { refreshTokenService.save(userId, any()) } returns true

        // when
        val jwtTokenDto = usecase("aaa.bbb.ccc")

        // then
        assertNull(jwtTokenDto)
    }

    companion object {
        private const val TEST_SUBJECT = "1"
        private val TestJWTToken = JWTToken(
            accessToken = "aaa.bbb.ccc",
            refreshToken = "ddd.eee.fff",
        )
    }
}
