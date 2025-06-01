package com.peekr.domain.repository.user

import domain.model.user.User

interface UserRepository {
    suspend fun findByProviderAndProviderId(
        provider: String,
        providerId: String,
    ): User?

    suspend fun save(user: User): User
}
