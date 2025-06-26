package com.peekr.domain.auth.infrastructure.serviceImpl

import com.auth0.jwt.interfaces.DecodedJWT
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.model.value.JWTClaimName
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.infrastructure.JWTConfigFactory
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.SocialLoginProvider
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.AuthService

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
    private val jwtConfigFactory: JWTConfigFactory,
) : AuthService {
    override suspend fun login(
        provider: SocialLoginProvider,
        providerId: String,
    ): JWTToken? {
        val authUser = authRepository.findByProviderAndProviderId(provider, providerId)

        if (authUser == null) return null

        val payload = JWTTokenPayload(
            subject = authUser.id.toString(),
            claimName = JWTClaimName.Name,
            claim = authUser.name,
        )
        return jwtTokenService.generate(payload)
    }

    override suspend fun register(authUser: AuthUser): JWTToken {
        val savedAuthUser = authRepository.save(authUser)

        val payload = JWTTokenPayload(
            subject = savedAuthUser.id.toString(),
            claimName = JWTClaimName.Name,
            claim = savedAuthUser.name,
        )
        return jwtTokenService.generate(payload)
    }

    override suspend fun refresh(token: String): JWTToken? {
        val decodedRefreshToken = verifyRefreshToken(token)
        val persistedName = refreshTokenRepository.findNameByRefreshToken(token)

        return if (decodedRefreshToken != null && persistedName != null) {
            val foundedAuthUser: AuthUser? = authRepository.getUserByName(persistedName)
            val nameFromRefreshToken: String? =
                decodedRefreshToken.getClaim(JWTClaimName.Name.name)?.asString()

            if (foundedAuthUser != null && nameFromRefreshToken == foundedAuthUser.name) {
                val payload = JWTTokenPayload(
                    subject = foundedAuthUser.id.toString(),
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
        val verifier = jwtConfigFactory.createVerifier(jwtTokenService.getVerifierConfig())
        verifier.verify(token)
    } catch (e: Exception) {
        null
    }
}
