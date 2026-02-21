package com.peekr.domain.user.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.auth.application.provider.AuthProviderApi
import com.peekr.domain.user.domain.provider.AuthProvider

class AuthProviderImpl(private val authProviderApi: AuthProviderApi) : AuthProvider {
    override suspend fun deleteRefreshToken(userId: UserId): Boolean =
        authProviderApi.deleteRefreshToken(userId)
}
