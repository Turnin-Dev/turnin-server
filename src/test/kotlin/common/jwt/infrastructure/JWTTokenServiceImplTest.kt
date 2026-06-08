package com.turnin.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.turnin.common.jwt.JWTTestDoubles
import com.turnin.common.jwt.domain.model.JWTClaimName
import com.turnin.common.jwt.domain.model.JWTTokenType
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.util.config.AppConfig
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class JWTTokenServiceImplTest {
    private val appConfigManager: AppConfig = mockk()
    private lateinit var jwtTokenService: JWTTokenService

    @Before
    fun setup() {
        every { appConfigManager.get(any()) } answers {
            val key = firstArg<String>()
            when (key) {
                "ktor.security.jwt.realm" -> JWTTestDoubles.REALM
                "ktor.security.jwt.issuer" -> JWTTestDoubles.ISSUER
                "ktor.security.jwt.audience" -> JWTTestDoubles.AUDIENCE
                "ktor.security.jwt.secret" -> JWTTestDoubles.SECRET
                "ktor.security.jwt.accessTokenExpiresIn" -> JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN.toString()
                "ktor.security.jwt.refreshTokenExpiresIn" -> JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN.toString()
                else -> "default-value"
            }
        }

        every { appConfigManager.getOrDefault(any(), any()) } answers {
            val key = firstArg<String>()
            val defaultValue = secondArg<String>()
            when (key) {
                "ktor.security.jwt.realm" -> JWTTestDoubles.REALM
                "ktor.security.jwt.issuer" -> JWTTestDoubles.ISSUER
                "ktor.security.jwt.audience" -> JWTTestDoubles.AUDIENCE
                "ktor.security.jwt.secret" -> JWTTestDoubles.SECRET
                "ktor.security.jwt.accessTokenExpiresIn" -> JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN.toString()
                "ktor.security.jwt.refreshTokenExpiresIn" -> JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN.toString()
                else -> defaultValue
            }
        }

        jwtTokenService = JWTTokenServiceImpl(appConfigManager, JWTTestDoubles.SECRET)
    }

    // ==================== generate ====================

    @Test
    fun `유효한 액세스 토큰, 리프레쉬 토큰을 생성한다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()
        val accessTokenVerifier = jwtTokenService.createVerifier(JWTTokenType.Access)
        val refreshTokenVerifier = jwtTokenService.createVerifier(JWTTokenType.Refresh)

        // when
        val token = jwtTokenService.generate(payload)

        // then
        accessTokenVerifier.verify(token.accessToken)
        refreshTokenVerifier.verify(token.refreshToken)

        val decodedAccessToken = JWT.decode(token.accessToken)
        val decodedRefreshToken = JWT.decode(token.refreshToken)

        assertEquals(payload.userId, decodedAccessToken.subject)
        assertEquals(JWTTestDoubles.ISSUER, decodedAccessToken.issuer)
        assert(JWTTestDoubles.AUDIENCE in decodedAccessToken.audience)
        assertEquals(payload.userId, decodedRefreshToken.subject)
        assertEquals(
            payload.claims[JWTClaimName.DISPLAY_ID],
            decodedAccessToken.getClaim(JWTClaimName.DISPLAY_ID.key).asString(),
        )
    }

    @Test
    fun `액세스 토큰의 만료 시간이 올바르게 설정된다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()

        // when
        val token = jwtTokenService.generate(payload)

        // then
        val decoded = JWT.decode(token.accessToken)
        val issuedAt = decoded.issuedAt.toInstant().truncatedTo(ChronoUnit.SECONDS)
        val expiresAt = decoded.expiresAt.toInstant()
        val expectedExpiresAt = issuedAt.plusMillis(JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN)

        assertEquals(expectedExpiresAt, expiresAt)
    }

    @Test
    fun `리프레쉬 토큰의 만료 시간이 올바르게 설정된다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()

        // when
        val token = jwtTokenService.generate(payload)

        // then
        val decoded = JWT.decode(token.refreshToken)
        val issuedAt = decoded.issuedAt.toInstant().truncatedTo(ChronoUnit.SECONDS)
        val expiresAt = decoded.expiresAt.toInstant()
        val expectedExpiresAt = issuedAt.plusMillis(JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN)

        assertEquals(expectedExpiresAt, expiresAt)
    }

    @Test
    fun `액세스 토큰과 리프레쉬 토큰 모두 jti가 포함된다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()

        // when
        val token = jwtTokenService.generate(payload)

        // then
        val decodedAccessToken = JWT.decode(token.accessToken)
        val decodedRefreshToken = JWT.decode(token.refreshToken)

        assertNotNull(decodedAccessToken.id)
        assertNotNull(decodedRefreshToken.id)
    }

    @Test
    fun `generate를 여러 번 호출하면 매번 다른 jti를 가진 토큰을 생성한다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()

        // when
        val token1 = jwtTokenService.generate(payload)
        val token2 = jwtTokenService.generate(payload)

        // then
        val jti1 = JWT.decode(token1.accessToken).id
        val jti2 = JWT.decode(token2.accessToken).id
        assertNotEquals(jti1, jti2)
    }

    // ==================== createVerifier ====================

    @Test
    fun `Access Verifier는 audience가 다른 토큰 검증 시 실패한다`() {
        // given
        val tokenWithWrongAudience = JWT
            .create()
            .withAudience("wrong-audience")
            .withIssuer(JWTTestDoubles.ISSUER)
            .withSubject("user123")
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusMillis(JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN)))
            .sign(JWTTestDoubles.MockAlgorithm)

        val verifier = jwtTokenService.createVerifier(JWTTokenType.Access)

        // when, then
        assertThrows<JWTVerificationException> {
            verifier.verify(tokenWithWrongAudience)
        }
    }

    @Test
    fun `Access Verifier는 issuer가 다른 토큰 검증 시 실패한다`() {
        // given
        val tokenWithWrongIssuer = JWT
            .create()
            .withAudience(JWTTestDoubles.AUDIENCE)
            .withIssuer("wrong-issuer")
            .withSubject("user123")
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusMillis(JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN)))
            .sign(JWTTestDoubles.MockAlgorithm)

        val verifier = jwtTokenService.createVerifier(JWTTokenType.Access)

        // when, then
        assertThrows<JWTVerificationException> {
            verifier.verify(tokenWithWrongIssuer)
        }
    }

    @Test
    fun `Refresh 토큰은 audience와 issuer 없이 발급되므로 Refresh Verifier는 이를 검증하지 않는다`() {
        // given: audience, issuer 없이 생성된 토큰
        val tokenWithoutAudienceIssuer = JWT
            .create()
            .withSubject("user123")
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusMillis(JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN)))
            .sign(JWTTestDoubles.MockAlgorithm)

        val verifier = jwtTokenService.createVerifier(JWTTokenType.Refresh)

        // when, then (예외 없이 통과해야 함)
        assertDoesNotThrow {
            verifier.verify(tokenWithoutAudienceIssuer)
        }
    }

    // ==================== extractSubjectWithToken ====================

    @Test
    fun `만료된 토큰에서 Subject 추출 시 TokenExpiredException을 던진다`() {
        // given
        val expiredToken = JWTTestDoubles.getExpiredRefreshToken()

        // when, then
        assertThrows<TokenException.TokenExpiredException> {
            jwtTokenService.extractSubjectWithToken(expiredToken, JWTTokenType.Refresh)
        }
    }

    @Test
    fun `잘못된 형식의 토큰에서 Subject 추출 시 CannotDecodedException을 던진다`() {
        // when, then
        assertThrows<TokenException.CannotDecodedException> {
            jwtTokenService.extractSubjectWithToken("invalid.token", JWTTokenType.Refresh)
        }
    }

    @Test
    fun `서명이 다른 토큰에서 Subject 추출 시 VerificationFailedException을 던진다`() {
        // given
        val tamperedToken = JWTTestDoubles.getTamperedRefreshToken()

        // when, then
        assertThrows<TokenException.VerificationFailedException> {
            jwtTokenService.extractSubjectWithToken(tamperedToken, JWTTokenType.Refresh)
        }
    }

    @Test
    fun `유효한 액세스 토큰에서 Subject를 추출한다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()
        val token = jwtTokenService.generate(payload)

        // when
        val subject = jwtTokenService.extractSubjectWithToken(token.accessToken, JWTTokenType.Access)

        // then
        assertEquals(payload.userId, subject)
    }

    @Test
    fun `유효한 리프레쉬 토큰에서 Subject를 추출한다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()
        val token = jwtTokenService.generate(payload)

        // when
        val subject = jwtTokenService.extractSubjectWithToken(token.refreshToken, JWTTokenType.Refresh)

        // then
        assertEquals(payload.userId, subject)
    }

    @Test
    fun `만료된 액세스 토큰에서 Subject 추출 시 TokenExpiredException을 던진다`() {
        // given
        val expiredToken = JWTTestDoubles.getExpiredAccessToken()

        // when, then
        assertThrows<TokenException.TokenExpiredException> {
            jwtTokenService.extractSubjectWithToken(expiredToken, JWTTokenType.Access)
        }
    }

    // ==================== verify ====================

    @Test
    fun `유효한 액세스 토큰 검증 시 DecodedJWT를 반환한다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()
        val token = jwtTokenService.generate(payload)

        // when
        val result = jwtTokenService.verify(token.accessToken, JWTTokenType.Access)

        // then
        assertNotNull(result)
        assertEquals(payload.userId, result!!.subject)
        assertEquals(
            payload.claims[JWTClaimName.DISPLAY_ID],
            result.getClaim(JWTClaimName.DISPLAY_ID.key).asString(),
        )
    }

    @Test
    fun `유효한 리프레쉬 토큰 검증 시 DecodedJWT를 반환한다`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()
        val token = jwtTokenService.generate(payload)

        // when
        val result = jwtTokenService.verify(token.refreshToken, JWTTokenType.Refresh)

        // then
        assertNotNull(result)
        assertEquals(payload.userId, result!!.subject)
    }

    @Test
    fun `만료된 토큰 검증 시 TokenExpiredException을 던진다`() {
        // given
        val expiredToken = JWTTestDoubles.getExpiredRefreshToken()

        // when, then
        assertThrows<TokenException.TokenExpiredException> {
            jwtTokenService.verify(expiredToken, JWTTokenType.Refresh)
        }
    }

    @Test
    fun `잘못된 형식의 토큰 검증 시 CannotDecodedException을 던진다`() {
        // when, then
        assertThrows<TokenException.CannotDecodedException> {
            jwtTokenService.verify("invalid.token", JWTTokenType.Refresh)
        }
    }

    @Test
    fun `서명이 다른 토큰 검증 시 VerificationFailedException을 던진다`() {
        // given
        val tamperedToken = JWTTestDoubles.getTamperedRefreshToken()

        // when, then
        assertThrows<TokenException.VerificationFailedException> {
            jwtTokenService.verify(tamperedToken, JWTTokenType.Refresh)
        }
    }

    @Test
    fun `리프레쉬 토큰을 Access Verifier로 검증 시 VerificationFailedException을 던진다`() {
        // given: Refresh 토큰은 audience/issuer가 없으므로 Access Verifier 검증 실패
        val payload = JWTTestDoubles.getJWTTokenPayload()
        val token = jwtTokenService.generate(payload)

        // when, then
        assertThrows<TokenException.VerificationFailedException> {
            jwtTokenService.verify(token.refreshToken, JWTTokenType.Access)
        }
    }
}
