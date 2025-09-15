package com.peekr.domain.auth.infrastructure.service.impl

import com.auth0.jwt.interfaces.DecodedJWT
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.model.Register
import com.peekr.domain.auth.domain.model.RegisterResult
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.core.model.DisplayId

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) : AuthService {
    override suspend fun login(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): LoginResult? {
        LOGGER.debug("login service attempt, provider: $provider, providerId: ${providerId.masking()}")
        val authUser = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)
        if (authUser == null) {
            LOGGER.debug("AuthUser not found, provider: $provider, providerId: ${providerId.masking()}")
            return null
        }

        val payload = JWTTokenPayload(
            userId = authUser.userId.value.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = authUser.displayId.value,
        )
        val jwtToken = jwtTokenService.generate(payload)
        val loginResult = LoginResult(jwtToken, authUser)

        authRepository.updateLastLoginAt(authUser.userId)

        LOGGER.debug("login service successful")
        return loginResult
    }

    override suspend fun register(register: Register): RegisterResult {
        LOGGER.debug("register service attempt, displayId: ${register.displayId.value.masking()}")
        val savedAuthUser = authRepository.save(register)

        val payload = JWTTokenPayload(
            userId = savedAuthUser.userId.value.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = savedAuthUser.displayId.value,
        )

        val jwtToken = jwtTokenService.generate(payload)
        val result = RegisterResult(jwtToken, savedAuthUser)

        LOGGER.debug("register service successful, username: ${savedAuthUser.name}")

        return result
    }

    override suspend fun refresh(token: String): JWTToken? = try {
        val decodedRefreshToken = verifyRefreshToken(token)

        if (decodedRefreshToken == null) {
            null
        }

        val userId = refreshTokenRepository.findUserIdByRefreshToken(token)
        val authUser: AuthUser? = userId?.let {
            authRepository.findUserByUserId(it)
        }

        if (userId != null &&
            authUser != null &&
            userId == authUser.userId
        ) {
            val payload = JWTTokenPayload(
                userId = authUser.userId.toString(),
                claimName = JWTClaimName.DISPLAY_ID,
                claim = authUser.displayId.value,
            )
            val jwtToken = jwtTokenService.generate(payload)
            jwtToken
        } else {
            null
        }
    } catch (e: Exception) {
        LOGGER.error(e, e.message)
        null
    }

    override suspend fun findUser(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): FindUserResult {
        val result = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)
        return FindUserResult(result != null)
    }

    override suspend fun existsDisplayId(displayId: DisplayId): Boolean =
        authRepository.existsByDisplayId(displayId)

    private fun verifyRefreshToken(token: String): DecodedJWT? = try {
        val verifier = jwtTokenService.createVerifier(JWTTokenType.Refresh)
        verifier.verify(token)
    } catch (e: Exception) {
        LOGGER.error(e, e.message)
        null
    }
}

private val LOGGER = AppLoggerFactory.createLogger("AuthServiceImpl")
