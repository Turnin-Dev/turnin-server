package com.peekr.domain.auth.infrastructure.serviceImpl

import com.auth0.jwt.interfaces.DecodedJWT
import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.model.RegisterResult
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.AuthService

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) : AuthService {
    override suspend fun login(
        provider: SocialLoginProvider,
        providerId: String,
    ): LoginResult? {
        val authUser = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)

        if (authUser == null) return null

        val payload = JWTTokenPayload(
            userId = authUser.id.toString(),
            claimName = JWTClaimName.Name,
            claim = authUser.name,
        )
        val jwtToken = jwtTokenService.generate(payload)

        val loginResult = LoginResult(jwtToken, authUser)
        return loginResult
    }

    override suspend fun register(authUser: AuthUser): RegisterResult {
        val savedAuthUser = authRepository.save(authUser)

        val payload = JWTTokenPayload(
            userId = savedAuthUser.id.toString(),
            claimName = JWTClaimName.Name,
            claim = savedAuthUser.name,
        )

        val jwtToken = jwtTokenService.generate(payload)

        return RegisterResult(jwtToken, savedAuthUser)
    }

    override suspend fun refresh(token: String): JWTToken? = try {
        val decodedRefreshToken = verifyRefreshToken(token)

        val name = refreshTokenRepository.findNameByRefreshToken(token)
        val authUser: AuthUser? = name?.let {
            authRepository.getUserByDisplayId(name)
        }

        if (decodedRefreshToken != null &&
            name != null &&
            authUser != null &&
            name == authUser.name
        ) {
            val payload = JWTTokenPayload(
                userId = authUser.id.toString(),
                claimName = JWTClaimName.Name,
                claim = authUser.name,
            )
            val jwtToken = jwtTokenService.generate(payload)
            jwtToken
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    override suspend fun findUser(
        provider: SocialLoginProvider,
        providerId: String,
    ): FindUserResult {
        val result = authRepository.findAuthUserByProviderAndProviderId(provider, providerId)
        return FindUserResult(result != null)
    }

    private fun verifyRefreshToken(token: String): DecodedJWT? = try {
        val verifier = jwtTokenService.createVerifier(JWTTokenType.Refresh)
        verifier.verify(token)
    } catch (e: Exception) {
        null
    }
}
