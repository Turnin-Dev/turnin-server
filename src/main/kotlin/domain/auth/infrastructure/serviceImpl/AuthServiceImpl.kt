package com.peekr.domain.auth.infrastructure.serviceImpl

import com.auth0.jwt.interfaces.DecodedJWT
import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.model.value.JWTClaimName
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.model.domain.auth.domain.model.RegisterResult
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

    override suspend fun refresh(token: String): JWTToken? {
        val decodedRefreshToken = verifyRefreshToken(token)
        val persistedName = refreshTokenRepository.findNameByRefreshToken(token)

        return try {
            if (decodedRefreshToken != null && persistedName != null) {
                val foundedAuthUser: AuthUser? = authRepository.getUserByName(persistedName)
                val nameFromRefreshToken: String? =
                    decodedRefreshToken.getClaim(JWTClaimName.Name.name)?.asString()

                if (foundedAuthUser != null && nameFromRefreshToken == foundedAuthUser.name) {
                    val payload = JWTTokenPayload(
                        userId = foundedAuthUser.id.toString(),
                        claimName = JWTClaimName.Name,
                        claim = nameFromRefreshToken,
                    )
                    val jwtToken = jwtTokenService.generate(payload)
                    jwtToken
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun verifyRefreshToken(token: String): DecodedJWT? {
        val decodedJWT: DecodedJWT? = getDecodedJWT(token)

        if (decodedJWT == null) return null

        return if (decodedJWT.audience.first() == jwtTokenService.audience &&
            decodedJWT.issuer == jwtTokenService.issuer
        ) {
            decodedJWT
        } else {
            null
        }
    }

    private fun getDecodedJWT(token: String): DecodedJWT? = try {
        val verifier = jwtTokenService.createVerifier()
        verifier.verify(token)
    } catch (e: Exception) {
        null
    }
}
