package com.peekr.domain.repository.auth

import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.domain.model.value.auth.SocialLoginProvider

interface AuthRepository {
    suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser?

    suspend fun save(authUser: AuthUser): AuthUser
}
