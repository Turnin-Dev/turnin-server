package com.peekr.domain.auth.infrastructure.serviceImpl

import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.model.value.JWTClaimName
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.SocialLoginProvider
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.service.AuthService

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val jwtTokenService: JWTTokenService,
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
}
