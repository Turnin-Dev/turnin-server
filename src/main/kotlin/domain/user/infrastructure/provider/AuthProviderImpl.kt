package com.turnin.domain.user.infrastructure.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.application.provider.AuthProviderApi
import com.turnin.domain.user.domain.provider.AuthProvider

class AuthProviderImpl(private val authProviderApi: AuthProviderApi) : AuthProvider {
    override suspend fun deleteRefreshToken(userId: UserId) =
        authProviderApi.deleteRefreshToken(userId)
}
