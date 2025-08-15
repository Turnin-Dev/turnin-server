package com.peekr.domain.auth.infrastructure.service.impl

import com.auth0.jwt.interfaces.DecodedJWT
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.AppLoggerFactory.debug
import com.peekr.common.util.AppLoggerFactory.error
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.model.RegisterResult
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.AuthService

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) : AuthService {
    override suspend fun login(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): LoginResult? {
        val authUser = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)

        if (authUser == null) {
            LOGGER.debug("authUser is failed, provider: $provider, providerId: $providerId")
            return null
        }

        val payload = JWTTokenPayload(
            userId = authUser.id.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = authUser.displayId,
        )
        val jwtToken = jwtTokenService.generate(payload)

        val loginResult = LoginResult(jwtToken, authUser)
        LOGGER.debug("AuthService(login()) successful")
        return loginResult
    }

    override suspend fun register(authUser: AuthUser): RegisterResult {
        val savedAuthUser = authRepository.save(authUser)

        val payload = JWTTokenPayload(
            userId = savedAuthUser.id.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = savedAuthUser.displayId,
        )

        val jwtToken = jwtTokenService.generate(payload)

        return RegisterResult(jwtToken, savedAuthUser)
    }

    override suspend fun refresh(token: String): JWTToken? = try {
        val decodedRefreshToken = verifyRefreshToken(token)

        val userId = refreshTokenRepository.findUserIDByRefreshToken(token)
        val authUser: AuthUser? = userId?.let {
            authRepository.findUserByUserId(it)
        }

        if (decodedRefreshToken != null &&
            userId != null &&
            authUser != null &&
            userId == authUser.id
        ) {
            val payload = JWTTokenPayload(
                userId = authUser.id.toString(),
                claimName = JWTClaimName.DISPLAY_ID,
                claim = authUser.displayId,
            )
            val jwtToken = jwtTokenService.generate(payload)
            jwtToken
        } else {
            null
        }
    } catch (e: Exception) {
        LOGGER.error(e)
        null
    }

    override suspend fun findUser(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): FindUserResult {
        val result = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)
        return FindUserResult(result != null)
    }

    private fun verifyRefreshToken(token: String): DecodedJWT? = try {
        val verifier = jwtTokenService.createVerifier(JWTTokenType.Refresh)
        verifier.verify(token)
    } catch (e: Exception) {
        LOGGER.error(e)
        null
    }
}

private val LOGGER = AppLoggerFactory.createLogger("AuthServiceImpl")
