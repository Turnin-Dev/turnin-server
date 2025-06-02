package com.peekr.infrastructure.serviceImpl

import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.domain.model.entity.auth.JwtToken
import com.peekr.domain.model.value.auth.SocialLoginProvider
import com.peekr.domain.repository.auth.AuthRepository
import com.peekr.domain.service.auth.AuthService
import com.peekr.domain.service.auth.JwtTokenProvider

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
