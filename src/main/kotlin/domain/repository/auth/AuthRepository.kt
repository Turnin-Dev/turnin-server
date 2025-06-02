package com.peekr.domain.repository.auth

import domain.model.entity.auth.AuthUser

interface UserRepository {
    suspend fun findByProviderAndProviderId(
        provider: String,
        providerId: String,
    ): AuthUser?

    suspend fun save(authUser: AuthUser): AuthUser
}
