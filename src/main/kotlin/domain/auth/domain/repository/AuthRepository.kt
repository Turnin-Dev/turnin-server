package com.peekr.domain.auth.domain.repository

import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider

interface AuthRepository {
    suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser?

    suspend fun save(authUser: AuthUser): AuthUser
}
