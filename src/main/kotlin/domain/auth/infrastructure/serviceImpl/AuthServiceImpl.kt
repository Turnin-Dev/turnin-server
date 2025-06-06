package com.peekr.domain.auth.infrastructure.serviceImpl

import com.peekr.common.jwt.domain.model.entity.JwtToken
import com.peekr.common.jwt.domain.service.JwtTokenProvider
import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.service.AuthService

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) : AuthService {
    override suspend fun login(
        provider: SocialLoginProvider,
        providerId: String,
    ): JwtToken? {
        val authUser = authRepository.findByProviderAndProviderId(provider, providerId)

        if (authUser == null) return null

        return jwtTokenProvider.generate(authUser)
    }

    override suspend fun register(authUser: AuthUser): JwtToken {
        val savedAuthUser = authRepository.save(authUser)

        return jwtTokenProvider.generate(savedAuthUser)
    }
}
